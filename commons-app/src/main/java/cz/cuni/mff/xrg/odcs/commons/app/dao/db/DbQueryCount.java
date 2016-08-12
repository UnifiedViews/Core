package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

import javax.persistence.TypedQuery;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataQueryCount;

/**
 * Query for number of records.
 * 
 * @author Petyr
 * @param <T>
 */
public class DbQueryCount<T extends DataObject> implements DataQueryCount<T> {

    private final TypedQuery<Number> query;

    /**
     * Create new query for count.
     * 
     * @param query
     *            Inner query.
     */
    protected DbQueryCount(TypedQuery<Number> query) {
        this.query = query;
    }

    TypedQuery<Number> getQuery() {
        return query;
    }

}
