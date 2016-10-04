package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.Predicate;

import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessReadBase;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQuery;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryCount;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.FilterExplanation;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.FilterTranslator;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter.Compare;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Implementation of {@link DbLogRead}. Provide support for usage of {@link Log#relativeId} for paging.
 *
 * @author Petyr
 */
class DbLogReadImpl extends DbAccessReadBase<Log> implements DbLogRead {

    public DbLogReadImpl() {
        super(Log.class);
    }

    @Transactional
    @Override
    public void prune(Date date) {
        Query query = em.createQuery(
                "DELETE FROM Log l WHERE l.timestamp < :time");
        query.setParameter("time", date.getTime()).executeUpdate();
    }

    @Transactional(readOnly = true)
    @Override
    public Long getLastRelativeIndex(Long executionId) {
        // we use directly the original builder
        DbQueryBuilder<Log> builder = super.createQueryBuilder();

        builder.addFilter(Compare.equal("execution", executionId));
        builder.sort("id", false);

        Log lastLog = execute(builder.getQuery().limit(0, 1));
        return lastLog == null ? null : lastLog.getRelativeId();
    }

    @Override
    public DbQueryBuilder<Log> createQueryBuilder() {
        return new DbLogQueryBuilder();
    }

    @Transactional(readOnly = true)
    @Override
    public Log execute(DbQuery<Log> query) {
        if (query instanceof DbLogQuery) {
            // we modify the query here
            DbLogQuery logQuery = (DbLogQuery) query;
            DbQuery newQuery = createQuery(logQuery.filters,
                    logQuery.fetchList, logQuery.sortProperty, logQuery.sortAsc,
                    logQuery.first, logQuery.count);
            // execute query
            return super.execute(newQuery);
        } else {
            // use standart execution
            return super.execute(query);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Log> executeList(DbQuery<Log> query) {
        if (query instanceof DbLogQuery) {
            // we modify the query here
            DbLogQuery logQuery = (DbLogQuery) query;
            DbQuery newQuery = createQuery(logQuery.filters,
                    logQuery.fetchList, logQuery.sortProperty, logQuery.sortAsc,
                    logQuery.first, logQuery.count);
            // execute query
            return super.executeList(newQuery);
        } else {
            return super.executeList(query);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public long executeSize(DbQueryCount<Log> query) {
        if (query instanceof DbLogQueryCount) {
            DbLogQueryCount logQuery = (DbLogQueryCount) query;
            // check if we can use faster approach
            if (logQuery.fetchList.isEmpty() && logQuery.filters.size() == 1) {
                // check that the only filter is equals for execution
                // in such case we can use getLastRelativeId
                Long execution = checkFilter(logQuery.filters.get(0));
                // we also need to get the value of execution
                if (execution != null) {
                    Long size = getLastRelativeIndex(execution);
                    if (size == null) {
                        // no execution exists, return null
                        return 0;
                    } else {
                        return size;
                    }
                }
            }

            // create standart query and use it
            DbQueryBuilder<Log> builder = super.createQueryBuilder();
            for (Object filter : logQuery.filters) {
                builder.addFilter(filter);
            }
            for (String toFetch : logQuery.fetchList) {
                builder.addFetch(toFetch);
            }
            return super.executeSize(builder.getCountQuery());
        } else {
            return super.executeSize(query);
        }
    }

    protected DbQuery<Log> createQuery(List<Object> filters,
            List<String> fetchList, String sortProperty, Boolean sortAsc,
            Integer start, Integer count) {
        boolean setLimits = true;
        if (start != null && count != null) {
            // use limits, check if we can use relativeIds to speed up
            // query
            if (sortProperty == null && filters.size() == 1 &&
                    checkFilter(filters.get(0)) != null) {
                // ok, we may translate limits into where condition
                filters.add(Compare.greater("relativeId", start));
                filters.add(Compare.lessEqual("relativeId", start + count));
                // and we do not need to set the limits
                setLimits = false;
            }
        } else {
            // no values for limit, we don't need to set it
            setLimits = false;
        }

        // create standart query and use it
        DbQueryBuilder<Log> builder = super.createQueryBuilder();
        for (Object filter : filters) {
            builder.addFilter(filter);
        }
        for (String toFetch : fetchList) {
            builder.addFetch(toFetch);
        }
        if (sortProperty != null) {
            builder.sort(sortProperty, sortAsc);
        }

        DbQuery<Log> query = builder.getQuery();
        if (setLimits) {
            query.limit(start, count);
        }
        return query;
    }

    /**
     * If the given filter can be explained as a filter {@link Log#execution} is equal to some value.
     *
     * @param filter
     * @return Value from filter or null in case of wrong filter.
     */
    private Long checkFilter(Object filter) {
        if (filter instanceof Predicate) {
            // we can use this directly
            return null;
        }
        if (translators == null) {
            return null;
        }

        // try to translate it
        FilterExplanation explanation = null;
        for (FilterTranslator translator : translators) {
            explanation = translator.explain(filter);
            if (explanation != null) {
                break;
            }
        }

        if (explanation == null) {
            return null;
        }

        if (explanation.getPropertyName().equals("execution") &&
                explanation.getOperation().equals("==")) {
            // it's the filter we are looking for
            return (Long) explanation.getValue();
        }

        return null;
    }

    @Transactional
    @Override
    public void delete(PipelineExecution execution) {
        Query query = em.createQuery(
                "DELETE FROM Log l WHERE l.execution = :execution");
        query.setParameter("execution", execution.getId()).executeUpdate();
    }
}
