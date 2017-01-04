package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Partial implementation of {@link DbAccessRead} interface.
 * 
 * @author Petyr
 * @param <T>
 */
public class DbAccessReadBase<T extends DataObject> implements DbAccessRead<T> {

    @PersistenceContext
    protected EntityManager em;

    @Autowired(required = false)
    protected DbAuthorizator authorizator;

    @Autowired(required = false)
    protected List<FilterTranslator> translators;

    protected final Class<T> entityClass;

    public DbAccessReadBase(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Transactional(readOnly = true)
    @Override
    public T getInstance(long id) {
        return em.find(entityClass, id);
    }

    @Transactional(readOnly = true)
    @Override
    public T getLightInstance(long id) {
        return em.find(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    public T execute(DbQuery<T> query) {
        // set max count of results
        query.getQuery().setMaxResults(1);
        return execute(query.getQuery());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    public List<T> executeList(DbQuery<T> query) {
        return executeList(query.getQuery());
    }

    @Transactional(readOnly = true)
    @Override
    public long executeSize(DbQueryCount<T> query) {
        Number result = query.getQuery().getSingleResult();
        return result.longValue();
    }

    @Override
    public DbQueryBuilder<T> createQueryBuilder() {
        return new DbQueryBuilderImpl<>(em, entityClass,
                authorizator, translators);
    }

    /**
     * Create count typed query
     * 
     * @param stringCountQuery
     * @return typed JPA query for count
     */
    protected TypedQuery<Long> createCountTypedQuery(String stringCountQuery) {
		return em.createQuery(stringCountQuery, Long.class);
	}
    
    /**
     * Create typed query from given string.
     * 
     * @param stringQuery
     * @return typed JPA query
     */
    protected TypedQuery<T> createTypedQuery(String stringQuery) {
        return em.createQuery(stringQuery, entityClass);
    }

    /**
     * Execute the given string query and return the results. No filters are
     * applied.
     * 
     * @param stringQuery
     * @return list of query results
     */
    protected List<T> executeList(String stringQuery) {
        return executeList(createTypedQuery(stringQuery));
    }

    /**
     * Execute given typed query and return the results. No filters are
     * applied.
     * 
     * @param typedQuery
     * @return list of query results
     */
    protected List<T> executeList(TypedQuery<T> typedQuery) {
        return Collections.checkedList(typedQuery.getResultList(), entityClass);
    }

    /**
     * Execute given typed query and return the result. No filters are
     * applied.
     * 
     * @param typedQuery
     * @return query result
     */
    protected T execute(TypedQuery<T> typedQuery) {
        try {
            // set max result for sure .. 
            typedQuery.setMaxResults(1);
            T result = (T) typedQuery.getSingleResult();
            return result;
        } catch (EmptyResultDataAccessException e) {
            // getSingleResult throws if it has no results 
            return null;
        }
    }

}
