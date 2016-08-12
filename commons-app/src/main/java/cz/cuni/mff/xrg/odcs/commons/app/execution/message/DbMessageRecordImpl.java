package cz.cuni.mff.xrg.odcs.commons.app.execution.message;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Implementation providing access to {@link MessageRecord} data objects.
 * 
 * @author Jan Vojt
 * @author Petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
public class DbMessageRecordImpl extends DbAccessBase<MessageRecord>
        implements DbMessageRecord {

    public DbMessageRecordImpl() {
        super(MessageRecord.class);
    }

    @Override
    public List<MessageRecord> getAll(PipelineExecution pipelineExec) {
        final String stringQuery = "SELECT r FROM MessageRecord r WHERE r.execution = :ins";

        TypedQuery<MessageRecord> query = createTypedQuery(stringQuery);
        query.setParameter("ins", pipelineExec);

        return executeList(query);
    }

}
