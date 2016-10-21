package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataAccess;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataQuery;

/**
 * Query can be created by {@link cz.cuni.mff.xrg.odcs.commons.app.dao.DataQueryBuilder} and used in {@link DataAccess}.
 * 
 * @author Petyr
 * @param <T>
 */
public class DbQuery<T extends DataObject> implements DataQuery<T> {

    protected final TypedQuery<T> query;

    /**
     * Create new query.
     * 
     * @param query
     *            Inner query.
     */
    protected DbQuery(TypedQuery<T> query) {
        this.query = query;
    }

    TypedQuery<T> getQuery() {
        return query;
    }

    /**
     * Set limits for this query.
     * 
     * @param first
     *            Index of first required object.
     * @param count
     *            How many objects to return.
     * @return This database query.
     */
    public DbQuery<T> limit(int first, int count) {
        query.setFirstResult(first);
        query.setMaxResults(count);
        return this;
    }

}
