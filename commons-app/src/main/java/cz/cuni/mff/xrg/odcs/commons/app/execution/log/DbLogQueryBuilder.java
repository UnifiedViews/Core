package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQuery;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryCount;

/**
 * Special query builder for logs.
 * 
 * @author Škoda Petr <skodapetr@gmail.com>
 */
class DbLogQueryBuilder implements DbQueryBuilder<Log> {

    /**
     * Holds information about sorting.
     */
    private class SortInformation {

        String propertyName = null;

        boolean asc = true;

    }

    /**
     * Filters that should be used in query.
     */
    private final List<Object> filters = new LinkedList<>();

    /**
     * Store information about sorting for this query builder.
     */
    private final SortInformation sortInfo = new SortInformation();

    /**
     * List or properties to fetch.
     */
    private final Set<String> fetchList = new HashSet<>();

    DbLogQueryBuilder() {

    }

    @Override
    public DbQuery<Log> getQuery() {
        return new DbLogQuery(filters, fetchList, sortInfo.propertyName,
                sortInfo.asc);
    }

    @Override
    public DbQueryCount<Log> getCountQuery() {
        return new DbLogQueryCount(filters, fetchList);
    }

    @Override
    public DataQueryBuilder<Log, DbQuery<Log>, DbQueryCount<Log>> claerFilters() {
        filters.clear();
        return this;
    }

    @Override
    public DataQueryBuilder<Log, DbQuery<Log>, DbQueryCount<Log>> addFilter(Object filter) {
        filters.add(filter);
        return this;
    }

    @Override
    public DataQueryBuilder<Log, DbQuery<Log>, DbQueryCount<Log>> sort(String propertyName, boolean asc) {
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

}
