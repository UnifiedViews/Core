package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

/**
 * Interface providing access to {@link DPUInstanceRecord} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbDPUInstanceRecord extends DbAccess<DPUInstanceRecord> {

    /**
     * Returns list of all DPUInstanceRecord currently persisted in database.
     * 
     * @return DPUInstance list
     */
    public List<DPUInstanceRecord> getAll();

}
