package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;

/**
 * Implementation for accessing {@link DPUInstanceRecord} data objects.
 * 
 * @author Jan Vojt
 * @author petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
public class DbDPUInstanceRecordImpl extends DbAccessBase<DPUInstanceRecord>
        implements DbDPUInstanceRecord {

    public DbDPUInstanceRecordImpl() {
        super(DPUInstanceRecord.class);
    }

    @Override
    public List<DPUInstanceRecord> getAll() {
        final String queryStr = "SELECT e FROM DPUInstanceRecord e";
        return executeList(queryStr);
    }

}
