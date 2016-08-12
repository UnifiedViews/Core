package cz.cuni.mff.xrg.odcs.commons.app.dao;

import java.util.List;

/**
 * Read only access to data of given type.
 * 
 * @author Petyr
 * @param <T>
 *            Data object.
 * @param <BUILDER>
 *            Query builder.
 * @param <QUERY>
 *            Query for list or single item.
 * @param <QUERY_SIZE>
 *            Query used for size.
 */
public interface DataAccessRead<T extends DataObject, BUILDER extends DataQueryBuilder<T, QUERY, QUERY_SIZE>, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>> {

    /**
     * Return fully loaded instance of object with given id. This operation may
     * be expensive! Use {@link #getLightInstance(long)} if you need data
     * directly only from the class itself.
     * 
     * @param id
     * @return a single data object with given ID, or null if no such object is
     *         found
     */
    public T getInstance(long id);

    /**
     * Return light instance of given class. Only data from single table that
     * corresponds for given class-object are loaded. The subobjects of given
     * object will not be loaded. Use {@link #getInstance(long)} to get fully
     * loaded object.
     * 
     * @param id
     * @return a single data object with given ID, or null if no such object is
     *         found
     */
    public T getLightInstance(long id);

    /**
     * Execute given query, interpret given result as single object and return
     * it.
     * 
     * @param query
     * @return a single data object selected by query, or null if empty result
     */
    public T execute(QUERY query);

    /**
     * Execute given query and return result as list of objects.
     * 
     * @param query
     * @return a list of data objects selected by given query, or empty list if
     *         empty result
     */
    public List<T> executeList(QUERY query);

    /**
     * Execute count query and return result.
     * 
     * @param query
     * @return number returned by given query
     */
    public long executeSize(QUERY_SIZE query);

    /**
     * Create query builder that can be used to create query for this access.
     * 
     * @return query builder
     */
    public BUILDER createQueryBuilder();

}
