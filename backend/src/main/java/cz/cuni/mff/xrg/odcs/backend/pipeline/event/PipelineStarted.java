package cz.cuni.mff.xrg.odcs.backend.pipeline.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

public final class PipelineStarted extends PipelineInfo {

    private static final Logger LOG = LoggerFactory
            .getLogger(PipelineStarted.class);

    public PipelineStarted(PipelineExecution execution, Object source) {
        super(execution, source,
                Messages.getString("PipelineInfo.starting", execution.getId()),
                Messages.getString("PipelineInfo.starting.detail", execution.getId(), execution.getPipeline().getName()));

        LOG.info("Execution #{} for pipeline {} started", execution.getId(), execution.getPipeline().getName());
    }

    public PipelineExecution getExecution() {
        return this.execution;
    }

}
