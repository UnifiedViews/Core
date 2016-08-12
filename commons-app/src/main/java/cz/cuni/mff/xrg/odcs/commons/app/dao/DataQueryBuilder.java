package cz.cuni.mff.xrg.odcs.commons.app.dao;

/**
 * Builder for {@link DataQuery} and {@link DataQueryCount}.
 * 
 * @author Petyr
 * @param <T>
 * @param <QUERY>
 * @param <QUERY_SIZE>
 */
public interface DataQueryBuilder<T extends DataObject, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>> {

    /**
     * Provide methods that can be used to apply sort in {@link DataQuery}
     * 
     * @param <T>
     * @param <QUERY>
     * @param <QUERY_SIZE>
     */
    public interface Sortable<T extends DataObject, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>> {

        /**
         * Remove previously applied sort and set new.
         * 
         * @param propertyName
         *            Set to null to remove sorting.
         * @param asc
         *            True to sort as ASC false to sort as DESC.
         * @return query builder
         */
        DataQueryBuilder<T, QUERY, QUERY_SIZE> sort(String propertyName, boolean asc);

    }

    /**
     * Provide possibility to filter data in {@link DataQuery}
     * 
     * @param <T>
     * @param <QUERY>
     * @param <QUERY_SIZE>
     */
    public interface Filterable<T extends DataObject, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>> {

        /**
         * Remove all user applied filters.
         * 
         * @return query builder
         */
        DataQueryBuilder<T, QUERY, QUERY_SIZE> claerFilters();

        /**
         * Add given filter as AND to existing filters.
         * 
         * @param filter
         * @return query builder
         */
        DataQueryBuilder<T, QUERY, QUERY_SIZE> addFilter(Object filter);

    }

    /**
     * @return query
     */
    QUERY getQuery();

    /**
     * Generate query that can be used to obtain size of result data.
     * 
     * @return count query
     */
    QUERY_SIZE getCountQuery();

}
