package cz.cuni.mff.xrg.odcs.backend.pipeline.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Report pipeline restart. Use if backend crash or has been shutdown.
 * 
 * @author Petyr
 */
public final class PipelineSanitized extends PipelineEvent {

    private static final Logger LOG = LoggerFactory
            .getLogger(PipelineRestart.class);

    public PipelineSanitized(PipelineExecution pipelineExec, Object source) {
        super(null, pipelineExec, source);

        LOG.info("Pipeline has been cancelled by sanitizer.");
    }

    @Override
    public MessageRecord getRecord() {
        return new MessageRecord(time, MessageRecordType.PIPELINE_INFO, null,
                execution, Messages.getString("PipelineSanitized.canceled"), "");
    }

}
