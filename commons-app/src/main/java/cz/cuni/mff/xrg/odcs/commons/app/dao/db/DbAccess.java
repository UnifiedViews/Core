package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataAccess;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Specialized {@link DataAccess} interface for databases.
 * 
 * @author Petyr
 * @param <T>
 */
public interface DbAccess<T extends DataObject>
        extends DataAccess<T, DbQueryBuilder<T>, DbQuery<T>, DbQueryCount<T>>,
        DbAccessRead<T> {

}
