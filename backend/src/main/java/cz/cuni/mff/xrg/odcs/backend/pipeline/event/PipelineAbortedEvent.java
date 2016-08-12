package cz.cuni.mff.xrg.odcs.backend.pipeline.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Event published during the pipeline execution termination on user request.
 * 
 * @author Petyr
 */
public final class PipelineAbortedEvent extends PipelineEvent {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineAbortedEvent.class);

    public PipelineAbortedEvent(PipelineExecution pipelineExec, Object source) {
        super(null, pipelineExec, source);

        LOG.info("Pipeline aborted on user request.");
    }

    @Override
    public MessageRecord getRecord() {
        return new MessageRecord(time, MessageRecordType.PIPELINE_INFO,
                dpuInstance, execution,
                Messages.getString("PipelineAbortedEvent.execution.aborted"),
                Messages.getString("PipelineAbortedEvent.execution.aborted.detail"));
    }
}
