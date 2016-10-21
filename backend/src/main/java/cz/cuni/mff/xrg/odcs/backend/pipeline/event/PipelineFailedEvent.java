package cz.cuni.mff.xrg.odcs.backend.pipeline.event;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.backend.context.ContextException;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.StructureException;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Event is published if the pipeline is terminated due the error. To create the
 * event use static methods.
 * 
 * @author Petyr
 */
public final class PipelineFailedEvent extends PipelineEvent {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineFailedEvent.class);

    private final String shortMessage;

    private final String longMessage;

    protected PipelineFailedEvent(String shortMessage,
            String longMessage,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {
        super(dpuInstance, pipelineExec, source);
        this.shortMessage = shortMessage;
        this.longMessage = longMessage;
    }

    protected PipelineFailedEvent(String shortMessage,
            Throwable exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {
        super(dpuInstance, pipelineExec, source);
        this.shortMessage = shortMessage;
        // transform stack trace into string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        this.longMessage = sw.toString();
    }

    protected PipelineFailedEvent(String shortMessage,
            String longMessagePrefix,
            Throwable exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {
        super(dpuInstance, pipelineExec, source);
        this.shortMessage = shortMessage;
        // transform stack trace into string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        this.longMessage = longMessagePrefix + sw.toString();
    }

    public static PipelineFailedEvent create(String shortMessage,
            String longMessage,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU '{}', ShortMsg: {} LongMsg: {}",
                dpuName, shortMessage, longMessage);

        return new PipelineFailedEvent(shortMessage, longMessage, dpuInstance,
                pipelineExec, source);
    }

    public static PipelineFailedEvent create(Throwable exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU '{}' because of the exception",
                dpuName, exception);

        return new PipelineFailedEvent(Messages.getString("PipelineFailedEvent.execution.failed"),
                Messages.getString("PipelineFailedEvent.execution.failed.detail") + exception.getMessage(),
                exception, dpuInstance, pipelineExec, source);
    }

    public static PipelineFailedEvent create(Error error,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {
        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU: '{}' because of the error",
                dpuName, error);

        return new PipelineFailedEvent(Messages.getString("PipelineFailedEvent.pipeline.failed"),
                Messages.getString("PipelineFailedEvent.pipeline.failed.detail") + error.getMessage(), error,
                dpuInstance, pipelineExec, source);
    }

    public static PipelineFailedEvent create(ContextException exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU '{}' because of the context exception",
                dpuName, exception);

        return new PipelineFailedEvent(
                Messages.getString("PipelineFailedEvent.pipeline.failed"),
                Messages.getString("PipelineFailedEvent.pipeline.failed.context"),
                exception, dpuInstance, pipelineExec, source);
    }

    public static PipelineFailedEvent create(ModuleException exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU '{}' because of the module exception",
                dpuName, exception);

        return new PipelineFailedEvent(
                Messages.getString("PipelineFailedEvent.pipeline.failed"),
                Messages.getString("PipelineFailedEvent.pipeline.failed.implementation"),
                exception, dpuInstance, pipelineExec, source);
    }

    public static PipelineFailedEvent create(StructureException exception,
            DPUInstanceRecord dpuInstance,
            PipelineExecution pipelineExec,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
        }
        LOG.error("Pipeline failed on DPU '{}' because of the structure exception",
                dpuName, exception);

        return new PipelineFailedEvent(Messages.getString("PipelineFailedEvent.pipeline.failed"),
                Messages.getString("PipelineFailedEvent.pipeline.failed.root"), exception, dpuInstance,
                pipelineExec, source);
    }

    /**
     * Create event which indicate that there is no jar-file for DPU to execute.
     * 
     * @param dpuInstance
     * @param execution
     * @param source
     * @return Event representing given situation.
     */
    public static PipelineFailedEvent createMissingFile(DPUInstanceRecord dpuInstance,
            PipelineExecution execution,
            Object source) {

        String dpuName = Messages.getString("PipelineFailedEvent.unknown");
        String jarFile = Messages.getString("PipelineFailedEvent.unknown");
        if (dpuInstance != null) {
            dpuName = dpuInstance.getName();
            jarFile = dpuInstance.getJarPath();
        }
        LOG.error("Pipeline failed as there is not jar-file for DPU '{}' ({})",
                dpuName, jarFile);

        return new PipelineFailedEvent(Messages.getString("PipelineFailedEvent.missing.dpu"),
                Messages.getString("PipelineFailedEvent.missing.jarFile", dpuName), dpuInstance, execution, source);
    }

    @Override
    public MessageRecord getRecord() {
        return new MessageRecord(time, MessageRecordType.PIPELINE_ERROR,
                dpuInstance, execution, shortMessage, longMessage);
    }
}
