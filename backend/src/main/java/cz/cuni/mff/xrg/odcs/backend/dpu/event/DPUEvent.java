package cz.cuni.mff.xrg.odcs.backend.dpu.event;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPostExecutor;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPreExecutor;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Base abstract class for the DPURecord event.
 * Also log the events into DataBase. All class that inherit from this
 * should also log when created.
 * 
 * @author Petyr
 */
public class DPUEvent extends ApplicationEvent {

    private static final Logger LOG = LoggerFactory.getLogger(DPUEvent.class);

    /**
     * Time of creation.
     */
    protected Date time;

    /**
     * Related context. Identify {@link PipelineExecution} as well as {@link DPUInstanceRecord}
     */
    protected Context context;

    /**
     * Message type.
     */
    protected MessageRecordType type;

    /**
     * Short event's message.
     */
    protected String shortMessage;

    /**
     * Long event's message.
     */
    protected String longMessage;

    protected DPUEvent(Context context,
            Object source,
            MessageRecordType type,
            String shortMessage,
            Throwable throwable) {
        super(source);
        this.time = new Date();
        this.context = context;
        this.type = type;
        this.shortMessage = shortMessage + throwable.getMessage();
        // transform stack trace into string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        this.longMessage = sw.toString();
    }

    protected DPUEvent(Context context,
            Object source,
            MessageRecordType type,
            String shortMessage,
            String longMessage) {
        super(source);
        this.time = new Date();
        this.context = context;
        this.type = type;
        this.shortMessage = shortMessage;
        this.longMessage = longMessage;
    }

    protected DPUEvent(Context context,
            Object source,
            MessageRecordType type,
            String shortMessage,
            String longMessage,
            Throwable throwable) {
        super(source);
        this.time = new Date();
        this.context = context;
        this.type = type;
        this.shortMessage = shortMessage;
        // transform stack trace into string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        // prepare long message		
        this.longMessage = longMessage + "<br/> stack trace:" + pw.toString();
    }

    /**
     * Create event which announce that DPU execution started.
     * 
     * @param context
     * @param source
     * @return Instance of dpu event.
     */
    public static DPUEvent createStart(Context context, Object source) {
        final DPUInstanceRecord dpu = context.getDPU();

        //final String msg = String.format("Starting DPU developer's code {}", dpu.getId());

        final String msg = Messages.getString("DPUEvent.starting", dpu.getId());

        LOG.info(msg);
        return new DPUEvent(context, source, MessageRecordType.DPU_INFO, msg, "");
    }

    /**
     * Create event for DPU successful execution.
     * 
     * @param context
     * @param source
     * @return Instance of dpu event.
     */
    public static DPUEvent createComplete(Context context, Object source) {
        final DPUInstanceRecord dpu = context.getDPU();
        final String msg = Messages.getString("DPUEvent.completed", dpu.getId());

        LOG.info(msg);
        return new DPUEvent(context, source, MessageRecordType.DPU_INFO, msg, "");
    }

    /**
     * Create event which warn about missing DPU's output dataUnits.
     * 
     * @param context
     * @param source
     * @return Instance of dpu event.
     */
    public static DPUEvent createNoOutputWarning(Context context, Object source) {
        LOG.warn("Missing outpuds for '{}'", context.getDPU().getName());

        return new DPUEvent(context, source, MessageRecordType.DPU_WARNING,
                Messages.getString("DPUEvent.missing.output"), "");
    }

    /**
     * Create event that announce wrong DPU state before start of the execution.
     * 
     * @param context
     * @param source
     * @return Instance of dpu event.
     */
    public static DPUEvent createWrongState(Context context, Object source) {
        LOG.error("DPU '{}' has wrong state at the beggining of the execution.",
                context.getDPU().getName());

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.unexpected.state"), "");
    }

    /**
     * Create event which indicate that there has been an error in executing
     * single DPU's preprocessor.
     * 
     * @param context
     * @param source
     * @param longMessage
     *            Description of the error.
     * @return Instance of dpu event.
     */
    public static DPUEvent createPreExecutorFailed(Context context,
            DPUPreExecutor source,
            String longMessage) {
        LOG.error("Pre-executor '{}' failed for DPU '{}' with message: '{}'",
                source.getClass().getName(),
                context.getDPU().getName(),
                longMessage);

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.pre.executor.failed"), longMessage);
    }

    /**
     * Create event which indicate that there has been an error in executing
     * single DPU's preprocessor.
     * 
     * @param context
     * @param source
     * @param longMessage
     *            Description of the error.
     * @param throwable
     * @return Instance of dpu event.
     */
    public static DPUEvent createPreExecutorFailed(Context context,
            DPUPreExecutor source,
            String longMessage,
            Throwable throwable) {
        LOG.error("Pre-executor '{}' failed for DPU '{}' with message: '{}'",
                source.getClass().getName(),
                context.getDPU().getName(),
                longMessage,
                throwable);

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.pre.executor.failed"), longMessage, throwable);
    }

    /**
     * Create event which indicate that there has been an error in executing
     * single DPU's post-processor.
     * 
     * @param context
     * @param source
     * @param longMessage
     *            Description of the error.
     * @return Instance of dpu event.
     */
    public static DPUEvent createPostExecutorFailed(Context context,
            DPUPostExecutor source,
            String longMessage) {
        LOG.error("Post-executor '{}' failed for DPU '{}' with message: '{}'",
                source.getClass().getName(),
                context.getDPU().getName(),
                longMessage);

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.post.executor.failed"), longMessage);
    }

    /**
     * Create event which indicate that the DPU execution failed because DPU
     * throw exception.
     * 
     * @param context
     * @param source
     * @param e
     * @return Instance of dpu event.
     */
    public static DPUEvent createFailed(Context context, Object source,
            Exception e) {
        LOG.error("DPU '{}' failed by throwing eception",
                context.getDPU().getName(), e);

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.execution.failed"), e);
    }

    /**
     * Create event which indicate that the execution of DPU failed because of {@link DataUnitException}.
     * 
     * @param context
     * @param source
     * @param e
     * @return Instance of dpu event.
     */
    public static DPUEvent createDataUnitFailed(Context context, Object source,
            DataUnitException e) {
        LOG.error("Failed to create DataUnit for DPU '{}'",
                context.getDPU().getName(), e);

        return new DPUEvent(context, source, MessageRecordType.DPU_ERROR,
                Messages.getString("DPUEvent.dataUnit.error"), e);
    }

    /**
     * Create event which indicate that the execution of pipeline should end
     * on request of last executed DPU.
     * 
     * @param context
     * @param source
     * @return Instance of dpu event.
     */
    public static DPUEvent createStopOnDpuRequest(Context context, Object source) {
        LOG.info("DPU '{}' required the termination of execution",
                context.getDPU().getName());

        return new DPUEvent(context, source, MessageRecordType.DPU_INFO,
                Messages.getString("DPUEvent.request.termination"), "");
    }

    /**
     * @return {@link MessageRecord} that describes event.
     */
    public MessageRecord getRecord() {
        return new MessageRecord(time, type, context.getDPU(),
                context.getExecution(), shortMessage, longMessage);

    }

    public PipelineExecution getExecution() {
        return context.getExecution();
    }

    public DPUInstanceRecord getDpuInstance() {
        return context.getDPU();
    }
}
