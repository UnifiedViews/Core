package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataQueryBuilder;

class DbQueryBuilderImpl<T extends DataObject> implements DbQueryBuilder<T> {

    /**
     * Holds information about sorting.
     */
    private class SortInformation {

        String propertyName = null;

        boolean asc = true;

    }

    /**
     * Entity manager used to create query.
     */
    private final EntityManager entityManager;

    /**
     * The main class we are querying on.
     */
    private final Class<T> entityClass;

    /**
     * Filters that should be used in query.
     */
    private final List<Object> filters = new LinkedList<>();

    /**
     * Store information about sorting for this query builder.
     */
    private final SortInformation sortInfo = new SortInformation();

    /**
     * DbAuthorizator.
     */
    private final DbAuthorizator authorizator;

    /**
     * Translator that can be used to translate given filter.
     */
    private final List<FilterTranslator> filterTranslators;

    /**
     * List or properties to fetch.
     */
    private final Set<String> fetchList = new HashSet<>();

    DbQueryBuilderImpl(EntityManager entityManager, Class<T> entityClass,
            DbAuthorizator authorizator, List<FilterTranslator> filterTranslators) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.authorizator = authorizator;
        this.filterTranslators = filterTranslators;
    }

    @Override
    public DbQuery<T> getQuery() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(entityClass);
        final Root<T> root = cq.from(entityClass);

        cq.select(root);

        setFetch(root);
        setWhereCriteria(cb, cq, root);
        setOrderClause(cb, cq, root);

        TypedQuery<T> query = entityManager.createQuery(cq);
        return new DbQuery(query);
    }

    @Override
    public DbQueryCount<T> getCountQuery() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        // We need to use abstract Number class here, because Virtuoso seems
        // to return arbitrary instances of Number for INTEGER data type
        // (Short, Long). See GH-745.
        final CriteriaQuery<Number> cq = cb.createQuery(Number.class);
        final Root<T> root = cq.from(entityClass);

        cq.select(cb.count(root));

        // we just set where criteria
        setWhereCriteria(cb, cq, root);

        TypedQuery<Number> query = entityManager.createQuery(cq);
        return new DbQueryCount(query);
    }

    @Override
    public DataQueryBuilder<T, DbQuery<T>, DbQueryCount<T>> claerFilters() {
        filters.clear();
        return this;
    }

    @Override
    public DataQueryBuilder<T, DbQuery<T>, DbQueryCount<T>> addFilter(Object filter) {
        filters.add(filter);
        return this;
    }

    @Override
    public DataQueryBuilder<T, DbQuery<T>, DbQueryCount<T>> sort(String propertyName, boolean asc) {
        sortInfo.propertyName = propertyName;
        sortInfo.asc = asc;
        return this;
    }

    @Override
    public void addFetch(String propertyName) {
        fetchList.add(propertyName);
    }

    @Override
    public void removeFetch(String propertyName) {
        fetchList.remove(propertyName);
    }

    @Override
    public void clearFetch() {
        fetchList.clear();
    }

    private void setFetch(final Root<T> root) {
        for (String propertyName : fetchList) {
            root.fetch(propertyName);
        }
    }

    private <E> void setWhereCriteria(final CriteriaBuilder cb, final CriteriaQuery<E> cq, Root<T> root) {
        // here we use the authentication predicate
        Predicate predicate = null;
        if (authorizator != null) {
            predicate = authorizator.getAuthorizationPredicate(cb, root, entityClass);
        }

        for (Object filter : filters) {
            Predicate filterPredicate = null;
            if (filter instanceof Predicate) {
                // we can use this directly
                filterPredicate = (Predicate) filter;
            } else if (filterTranslators != null) {
                // try to translate it
                for (FilterTranslator translator : filterTranslators) {
                    filterPredicate = translator.translate(filter, cb, root);
                    if (filterPredicate != null) {
                        break;
                    }
                }
            }
            // has been the filter translated ?
            if (filterPredicate == null) {
                throw new UnsupportedOperationException("Filter: " + filter.getClass().getName() + " is not supported.");
            }
            // add to our predicate
            if (predicate == null) {
                predicate = filterPredicate;
            } else {
                predicate = cb.and(predicate, filterPredicate);
            }
        }

        // apply filters
        if (predicate != null) {
            cq.where(predicate);
        } else {
            // nothing to set
        }
    }

    private <E> void setOrderClause(final CriteriaBuilder cb, final CriteriaQuery<E> cq, final Root<T> root) {
        if (sortInfo.propertyName == null) {
            return;
        }
        final Expression expr = (Expression) getPropertyPath(root,
                sortInfo.propertyName);

        // we sort only accroding to one column
        if (sortInfo.asc) {
            cq.orderBy(cb.asc(expr));
        } else {
            cq.orderBy(cb.desc(expr));
        }
    }

    /**
     * Gets property path.
     * 
     * @param root
     *            the root where path starts form
     * @param propertyId
     *            the property ID
     * @return the path to property
     */
    private Path<Object> getPropertyPath(final Root<?> root, final Object propertyId) {
        final String[] propertyIdParts = ((String) propertyId).split("\\.");

        Path<Object> path = null;
        for (final String part : propertyIdParts) {
            if (path == null) {
                path = root.get(part);
            } else {
                path = path.get(part);
            }
        }
        return path;
    }
}
