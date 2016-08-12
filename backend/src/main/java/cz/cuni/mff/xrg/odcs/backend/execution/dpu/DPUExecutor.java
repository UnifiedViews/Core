package cz.cuni.mff.xrg.odcs.backend.execution.dpu;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextException;
import cz.cuni.mff.xrg.odcs.backend.context.ContextFacade;
import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.execution.ExecutionResult;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFailedEvent;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

/**
 * Execute a single {@link DPUInstanceRecord} from {@link PipelineExecution}.
 * Take care about calling appropriate {@link DPUPreExecutor}s and {@link DPUPostExecutor}.
 * The {@link DPUExecutor} must be bind to the given {@link PipelineExecution} and {@link DPUInstanceRecord} by calling
 * {@link #bind(Node, Map, PipelineExecution, Date)} method before use.
 * 
 * @author Petyr
 */
public final class DPUExecutor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DPUExecutor.class);

    /**
     * Pipeline facade.
     */
    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * Module facade.
     */
    @Autowired
    private ModuleFacade moduleFacade;

    /**
     * List of all {@link DPUPreExecutor}s to execute before running DPU. Can be
     * null.
     */
    @Autowired(required = false)
    private List<DPUPreExecutor> dPUPreExecutors;

    /**
     * List of all {@link DPUPostExecutor}s to execute after DPU execution has
     * finished. Can be null.
     */
    @Autowired(required = false)
    private List<DPUPostExecutor> dPUPostExecutors;

    /**
     * Publisher instance for publishing pipeline execution events.
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ContextFacade contextFacade;

    /**
     * Node to execute.
     */
    private Node node;

    /**
     * Contexts.
     */
    private Map<Node, Context> contexts;

    /**
     * Our pipeline execution.
     */
    private PipelineExecution execution;

    /**
     * Context for current execution.
     */
    private Context context;

    /**
     * Store result state of the execution.
     */
    private final ExecutionResult executionResult = new ExecutionResult();

    /**
     * Sort pre/post executors.
     */
    @PostConstruct
    public void init() {
        if (dPUPreExecutors != null) {
            Collections.sort(dPUPreExecutors,
                    AnnotationAwareOrderComparator.INSTANCE);
        }
        if (dPUPostExecutors != null) {
            Collections.sort(dPUPostExecutors,
                    AnnotationAwareOrderComparator.INSTANCE);
        }

    }

    /**
     * Bind executor to the given {@link PipelineExecution} and {@link DPUInstanceRecord} by calling.
     * Can throw {@link ContextException} it the creation of {@link Context} failed. In such case does not publish any message.
     * 
     * @param node
     *            Node to execute.
     * @param contexts
     *            Contexts of other DPU's.
     * @param execution
     *            Pipeline execution.
     * @param lastExecutionTime
     *            Time of last successful execution. Can be null.
     * @throws cz.cuni.mff.xrg.odcs.backend.context.ContextException
     */
    public void bind(Node node,
            Map<Node, Context> contexts,
            PipelineExecution execution,
            Date lastExecutionTime) throws ContextException {
        this.node = node;
        this.contexts = contexts;
        this.execution = execution;
        // obtain context, bind it to this execution and add it to the contexts
        this.context = contextFacade.create(node.getDpuInstance(),
                execution.getContext(), lastExecutionTime);
        // if we have context then add it to the context storage
        this.contexts.put(node, context);
    }

    /**
     * Load instance of node to execute. In case of error return null and
     * publish error event message.
     * 
     * @return DPU's instance or null.
     */
    private Object loadInstance() {
        final DPUInstanceRecord dpu = node.getDpuInstance();
        // load instance
        try {
            dpu.loadInstance(moduleFacade);
        } catch (ModuleException e) {
            eventPublisher.publishEvent(PipelineFailedEvent.create(e, dpu,
                    execution, this));
            return null;
        }
        return dpu.getInstance();
    }

    /**
     * Execute {@link DPUPreExecutor} from {@link DPUExecutor#dPUPreExecutors}. If any {@link DPUPreExecutor} return false then return false. If there are no
     * {@link DPUPreExecutor} ({@link DPUExecutor#dPUPreExecutors} == null) then
     * instantly return true.
     * 
     * @param dpuInstance
     *            Instance of DPU to execute.
     * @param unitInfo
     * @return False if any of the pre-executors failed.
     */
    private boolean executePreExecutors(Object dpuInstance,
            ProcessingUnitInfo unitInfo) {
        if (dPUPreExecutors == null) {
            return true;
        }

        boolean result = true;
        for (DPUPreExecutor item : dPUPreExecutors) {
            LOG.trace("Executing pre-executor: {}", item.getClass().getSimpleName());

            try {
                if (!item.preAction(node, contexts, dpuInstance, execution,
                        unitInfo, result)) {
                    result = false;
                }
            } catch (Throwable t) {
                // instant failure
                LOG.error("The pre-executor throws", t);
                // one event abour failure
                eventPublisher.publishEvent(DPUEvent.createPreExecutorFailed(
                        context, item,
                        Messages.getString("DPUExecutor.pre.executor.exception")));
                // and second about .. the whole failure
                eventPublisher.publishEvent(PipelineFailedEvent.create(
                        t, node.getDpuInstance(), execution, this));
                return false;
            }
        }
        return result;
    }

    /**
     * Execute a single DPU instance. If the DPU execution failed ie. throw
     * exception then publish event and {@link #executionResult} failure flag is
     * set.
     * 
     * @param dpuInstance
     *            DPU instance.
     */
    private void executeInstance(Object dpuInstance) {
        // execute
        LOG.debug("Executing DPU ... ");

        // put dpuInstance id to MDC, so we can identify logs related to the
        // dpuInstance
        MDC.put(Log.MDC_DPU_INSTANCE_KEY_NAME,
                Long.toString(node.getDpuInstance().getId()));

        try {
            if (dpuInstance instanceof DPU) {
                ((DPU) dpuInstance).execute(context);
            } else {
                // can not be executed
                LOG.error("DPU do not implement execution interface.");
            }
        } catch (DPUException e) {
            LOG.error("Execution failed for DPUException.", e);
            eventPublisher.publishEvent(PipelineFailedEvent.create(e,
                    node.getDpuInstance(), execution, this));
            executionResult.failure();
        } catch (Exception e) {
            LOG.error("Execution failed for Exception.", e);
            eventPublisher.publishEvent(PipelineFailedEvent.create(e,
                    node.getDpuInstance(), execution, this));
            executionResult.failure();
        } catch (Error e) {
            LOG.error("Execution failed for Error.", e);
            eventPublisher.publishEvent(PipelineFailedEvent.create(e,
                    node.getDpuInstance(), execution, this));
            executionResult.failure();
        } catch (Throwable e) {
            LOG.error("Execution failed for Throwable.", e);
            eventPublisher.publishEvent(PipelineFailedEvent.create(e,
                    node.getDpuInstance(), execution, this));
            executionResult.failure();
        }

        // remove MDC from logs
        MDC.remove(Log.MDC_DPU_INSTANCE_KEY_NAME);

        LOG.debug("Executing DPU ... done");
    }

    /**
     * Execute {@link DPUPostExecutor} from {@link DPUExecutor#dPUPostExecutors}. If any {@link DPUPostExecutor} return false then return false. If there are no
     * {@link DPUPostExecutor} ({@link DPUExecutor#dPUPostExecutors} == null) then
     * instantly return true.
     * 
     * @param dpuInstance
     *            Instance of DPU that has been executed.
     * @param unitInfo
     * @return False if any of the post-executors failed.
     */
    private boolean executePostExecutors(Object dpuInstance,
            ProcessingUnitInfo unitInfo) {
        if (dPUPostExecutors == null) {
            return true;
        }

        boolean result = true;
        for (DPUPostExecutor item : dPUPostExecutors) {
            LOG.trace("Executing post-executor: {}", item.getClass().getSimpleName());
            try
            {
                if (!item.postAction(node, contexts, dpuInstance, execution,
                        unitInfo)) {
                    result = false;
                }
            } catch (Throwable t) {
                // instant failure
                LOG.error("The post-executor throws", t);
                // one event abour failure
                eventPublisher.publishEvent(DPUEvent.createPostExecutorFailed(
                        context, item,
                        Messages.getString("DPUExecutor.post.executor.exception")));
                // and second about .. the whole failure
                eventPublisher.publishEvent(PipelineFailedEvent.create(
                        t, node.getDpuInstance(), execution, this));
                return false;
            }
        }
        return result;
    }

    /**
     * Execute binded {@link Node} from {@link cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline}. Assume that
     * the given {@link Node} has not been executed yet. In case of problems set {@link #executionResult}.
     * 
     * @param unitInfo
     */
    private void execute(ProcessingUnitInfo unitInfo) {

        // get dpu's instance
        Object dpuInstance = loadInstance();
        if (dpuInstance == null) {
            executionResult.failure();
            return;
        }

        // call PreExecutors
        LOG.debug("Executing pre-executors ...");
        if (!executePreExecutors(dpuInstance, unitInfo)) {
            // we failed because of preExecutors
            executionResult.failure();
        }
        LOG.debug("Executing pre-executors ... done");

        switch (unitInfo.getState()) {
            case PREPROCESSING:
                // this is ok .. we continue
                break;
            case ABORTED:
            case FAILED:
                // we ignore this, they should return false when they fail
                LOG.warn(
                        "Some pre-processor set ABORTED/FAILED state before start of the execution. Ignoring state change.");
                break;
            case FINISHED:
                // dpu finished .. yes this can be
                // we also do not prevent DPU with such state to get here
                LOG.info(
                        "DPU has already been executed, skipping the execution.");
                // the context has already been loaded with given data
                return;
            case RUNNING:
                // wrong state, probably from last interrupted execution
                eventPublisher.publishEvent(DPUEvent
                        .createWrongState(context, this));
                executionResult.failure();
                return;
        }

        // set state to RUNNING and save this, by this we announce
        // that we have started the execution of this DPU
        unitInfo.setState(DPUExecutionState.RUNNING);

        try {
            pipelineFacade.save(execution);
        } catch (EntityNotFoundException ex) {
            LOG.error("Seems like someone deleted our pipeline run.", ex);
            executionResult.stop();
            return;
        }

        // execute the given instance - also catch all exception
        if (executionResult.continueExecution()) {
            eventPublisher.publishEvent(DPUEvent.createStart(context, this));

            executeInstance(dpuInstance);
            // check context for messages
            if (context.errorMessagePublished()) {
                executionResult.failure();
            }

            LOG.trace("DPU.executeInstance completed ..");
        } else {
            LOG.info("Skipping DPU execution.");
        }

        // set state
        if (executionResult.executionFailed()) {
            unitInfo.setState(DPUExecutionState.FAILED);
        } else if (context.canceled()) {
            unitInfo.setState(DPUExecutionState.ABORTED);
        } else {
            unitInfo.setState(DPUExecutionState.FINISHED);
        }

        // call PostExecutors if they fail then the execution fail
        LOG.debug("Executing post-executors ... done");
        if (!executePostExecutors(dpuInstance, unitInfo)) {
            executionResult.failure();
        }
        LOG.debug("Executing post-executors ... done");

        // we save the state into database
        try {
            pipelineFacade.save(execution);
        } catch (EntityNotFoundException ex) {
            LOG.error("Seems like someone deleted our pipeline run.", ex);
            executionResult.stop();
        }

    }

    /**
     * Execute the single DPU. If the DPU has been executed then do not execute
     * it again. If the DPU execution has been interrupted start the execution
     * again.
     */
    @Override
    public void run() {
        // get DPU instance record, the DPU to execute
        DPUInstanceRecord dpu = node.getDpuInstance();
        // get processing context info
        ProcessingUnitInfo unitInfo = execution.getContext().getDPUInfo(dpu);
        if (unitInfo == null) {
            // no previous information about execution, create it
            unitInfo = execution.getContext().createDPUInfo(dpu);
            // DPUExecutionState.PREPROCESSING
        } else {
            // check if not finished yet
            switch (unitInfo.getState()) {
                case ABORTED:
                case FAILED:
                    // dpu has been already executed and it failed
                    executionResult.failure();
                    return;
                case FINISHED:
                    // we will continue as we need to run pre/post Processors
                    // so they create DataUnits
                    break;
                case PREPROCESSING:
                    // some context data may have been already created, 
                    // so we delete (clear) context
                    // then new DataUnits will be automatically cleared during
                    // creation
                    // so we do not want to preserve context into here
                    contextFacade.delete(context, false);
                    break;
                case RUNNING:
                    // the context has already been initialized 
                    // and loaded in #bind method, as the
                    // ContextPreparator run only on PREPROCESSING
                    // we can safely continue
                    break;
            }
        }

        // run dpu
        execute(unitInfo);

        // publish message, this is standart end of execution
        eventPublisher.publishEvent(DPUEvent.createComplete(context, this));

        // check for planned end (to apply it there should be no other
        // reason to stop the execuiton)
        if (context.shouldStopExecution() && executionResult.continueExecution()) {
            // publish message
            eventPublisher.publishEvent(
                    DPUEvent.createStopOnDpuRequest(context, this));
            // and set stop flag
            executionResult.stop();
        }

        // we have done our job
        executionResult.finished();

        LOG.trace("DPU execution thread is about to finish");
    }

    /**
     * Call {@link Context#cancel()}, can be called from other thread. Use this
     * to order DPU to stop it's execution. The pre/post processors are executed
     * normally.
     */
    public void cancel() {
        context.cancel();
    }

    public ExecutionResult getExecResult() {
        return this.executionResult;
    }

}
