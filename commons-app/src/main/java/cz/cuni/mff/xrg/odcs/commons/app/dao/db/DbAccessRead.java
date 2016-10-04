package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataAccessRead;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Specialized {@link DataAccessRead} interface for databases.
 * 
 * @author Petyr
 * @param <T>
 */
public interface DbAccessRead<T extends DataObject>
        extends DataAccessRead<T, DbQueryBuilder<T>, DbQuery<T>, DbQueryCount<T>> {

    /**
     * Database use special query builder.
     * 
     * @return query builder
     */
    @Override
    public DbQueryBuilder<T> createQueryBuilder();

}
