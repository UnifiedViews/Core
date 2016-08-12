package cz.cuni.mff.xrg.odcs.commons.app.dao;

/**
 * Provide read and write access for given object type.
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
public interface DataAccess<T extends DataObject, BUILDER extends DataQueryBuilder<T, QUERY, QUERY_SIZE>, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>>
        extends DataAccessRead<T, BUILDER, QUERY, QUERY_SIZE> {

    /**
     * Persist given object into database.
     * 
     * @param object
     */
    public void save(T object);

    /**
     * Delete given object from database.
     * 
     * @param object
     */
    public void delete(T object);

}
