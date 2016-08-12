package cz.cuni.mff.xrg.odcs.frontend.container;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.FilterExplanation;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.FilterTranslator;

/**
 * Translate Vaadin's filters into {@link javax.persistence.criteria.Predicate}.
 * 
 * @author Petyr
 * @author bogo
 */
class VaadinFilterTranslator implements FilterTranslator {

    /**
     * SQL syntax for LIKE in WHERE clause specific for given platform.
     */
    private static String SQL_LIKE_ARGUMENT = "%%%s%%";

    @Autowired
    private AppConfig config;

    @Override
    public Predicate translate(Object filter, CriteriaBuilder cb, Root<?> root) {

        if (filter instanceof Container.Filter) {
            // ok continue
        } else {
            // not our job
            return null;
        }

        if (filter instanceof And) {
            final And and = (And) filter;
            final List<Container.Filter> filters = new ArrayList<>(and
                    .getFilters());

            Predicate predicate = cb.and(translate(filters.remove(0), cb, root),
                    translate(filters.remove(0), cb, root));

            while (filters.size() > 0) {
                predicate = cb.and(predicate, translate(filters.remove(0), cb,
                        root));
            }

            return predicate;
        }

        if (filter instanceof Or) {
            final Or or = (Or) filter;
            final List<Container.Filter> filters = new ArrayList<>(or
                    .getFilters());

            Predicate predicate = cb.or(translate(filters.remove(0), cb, root),
                    translate(filters.remove(0), cb, root));

            while (filters.size() > 0) {
                predicate = cb.or(predicate, translate(filters.remove(0), cb,
                        root));
            }

            return predicate;
        }

        if (filter instanceof Not) {
            final Not not = (Not) filter;
            return cb.not(translate(not.getFilter(), cb, root));
        }

        if (filter instanceof Between) {
            final Between between = (Between) filter;
            final Expression property = (Expression) getPropertyPath(root,
                    between.getPropertyId());
            return cb.between(property, (Comparable) between.getStartValue(),
                    (Comparable) between.getEndValue());
        }

        if (filter instanceof Compare) {
            final Compare compare = (Compare) filter;
            final Expression<Comparable> property = (Expression) getPropertyPath(
                    root, compare.getPropertyId());
            switch (compare.getOperation()) {
                case EQUAL:
                    return cb.equal(property, compare.getValue());
                case GREATER:
                    return cb.greaterThan(property, (Comparable) compare
                            .getValue());
                case GREATER_OR_EQUAL:
                    return cb.greaterThanOrEqualTo(property,
                            (Comparable) compare.getValue());
                case LESS:
                    return cb
                            .lessThan(property, (Comparable) compare.getValue());
                case LESS_OR_EQUAL:
                    return cb.lessThanOrEqualTo(property, (Comparable) compare
                            .getValue());
                default:
            }
        }

        if (filter instanceof IsNull) {
            final IsNull isNull = (IsNull) filter;
            return cb.isNull((Expression) getPropertyPath(root, isNull
                    .getPropertyId()));
        }

        if (filter instanceof Like) {
            final Like like = (Like) filter;
            if (like.isCaseSensitive()) {
                return cb.like((Expression) getPropertyPath(root, like
                        .getPropertyId()), like.getValue());
            } else {
                return cb.like(cb.lower((Expression) getPropertyPath(root, like
                        .getPropertyId())),
                        like.getValue().toLowerCase());
            }
        }

        if (filter instanceof SimpleStringFilter) {
            final SimpleStringFilter simpleStringFilter = (SimpleStringFilter) filter;
            Expression<String> property = (Expression) getPropertyPath(
                    root, simpleStringFilter.getPropertyId());
            if (simpleStringFilter.isIgnoreCase()) {
                property = cb.lower(property);
            }
            return cb.like(property, String.format(
                    SQL_LIKE_ARGUMENT,
                    simpleStringFilter.getFilterString()
                    ));
        }

        return null;
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
    private Path<Object> getPropertyPath(final Root<?> root,
            final Object propertyId) {
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

    @Override
    public FilterExplanation explain(Object filter) {
        if (filter instanceof Compare) {
            final Compare compare = (Compare) filter;
            String operation;
            switch (compare.getOperation()) {
                case EQUAL:
                    operation = "==";
                    break;
                case GREATER:
                    operation = ">";
                    break;
                case GREATER_OR_EQUAL:
                    operation = ">=";
                    break;
                case LESS:
                    operation = "<";
                    break;
                case LESS_OR_EQUAL:
                    operation = "<=";
                    break;
                default:
                    return null;
            }

            return new FilterExplanation(compare.getPropertyId().toString(),
                    operation,
                    compare.getValue());
        }
        // TODO: Add support for more filters
        return null;
    }

}
