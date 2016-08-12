package cz.cuni.mff.xrg.odcs.commons.app.execution.message;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Interface providing access to {@link MessageRecord} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbMessageRecord extends DbAccess<MessageRecord> {

    /**
     * @param pipelineExec
     * @return all {@link MessageRecord} emitted by given PipelineExecution.
     */
    public List<MessageRecord> getAll(PipelineExecution pipelineExec);

}
