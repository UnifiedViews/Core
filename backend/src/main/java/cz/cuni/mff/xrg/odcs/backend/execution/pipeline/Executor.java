/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.backend.execution.pipeline;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import ch.qos.logback.classic.Level;
import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextException;
import cz.cuni.mff.xrg.odcs.backend.execution.ExecutionResult;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.backend.logback.MdcExecutionLevelFilter;
import cz.cuni.mff.xrg.odcs.backend.logback.SqlAppender;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineAbortedEvent;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFailedEvent;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFinished;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineStarted;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.facade.LogFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Execute given pipeline. The {@link Executor} must be bind to the certain {@link PipelineExecution} by calling {@link #bind(PipelineExecution)} before
 * any future use.
 * 
 * @author Petyr
 */
public class Executor implements Runnable {

    /**
     * Logger class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    /**
     * Publisher instance for publishing pipeline execution events.
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Bean factory used to create beans for single pipeline execution.
     */
    @Autowired
    private BeanFactory beanFactory;

    /**
     * Pipeline facade.
     */
    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * Log facade.
     */
    @Autowired
    private LogFacade logFacade;

    /**
     * List of all {@link PreExecutor}s to execute before executing pipeline.
     * Can be null.
     */
    @Autowired(required = false)
    private List<PreExecutor> preExecutors;

    /**
     * List of all {@link PostExecutor}s to execute after pipeline execution has
     * finished. Can be null.
     */
    @Autowired(required = false)
    private List<PostExecutor> postExecutors;

    /**
     * Logger.
     */
    @Autowired
    private SqlAppender logAppender;

    /**
     * PipelineExecution record, determine pipeline to run.
     */
    private PipelineExecution execution;

    /**
     * Store context related to Nodes (DPUs).
     */
    private Map<Node, Context> contexts = new HashMap<>();

    /**
     * End time of last successful pipeline execution.
     */
    private Date lastSuccessfulExTime;

    @Autowired
    private AppConfig appConfig;

    /**
     * Backend identifier
     */
    private String backendID;

    /**
     * Sort pre/post executors.
     */
    @PostConstruct
    public void init() {
        this.backendID = this.appConfig.getString(ConfigProperty.BACKEND_ID);
        if (preExecutors != null) {
            Collections.sort(preExecutors,
                    AnnotationAwareOrderComparator.INSTANCE);
        }
        if (postExecutors != null) {
            Collections.sort(postExecutors,
                    AnnotationAwareOrderComparator.INSTANCE);
        }
    }

    /**
     * Bind {@link Executor} to the given {@link PipelineExecution}. Also update
     * the {@link PipelineExecution}'s state.
     * 
     * @param execution
     */
    public void bind(PipelineExecution execution) {
        this.execution = execution;
        contexts = new HashMap<>();

        // for newly scheduled pipelines delete the execution directory
        if (execution.getStatus() == PipelineExecutionStatus.QUEUED) {
            // update state and set start time
            this.execution.setStart(new Date());
            this.execution.setStatus(PipelineExecutionStatus.RUNNING);
            this.execution.setBackendId(this.backendID);

            try {
                pipelineFacade.save(this.execution);
            } catch (EntityNotFoundException ex) {
                LOG.warn("Seems like someone deleted our pipeline run.", ex);
            }
        } else {
            // we continue in run ... so just continue
        }

        // load last execution time
        Date lastSucess = pipelineFacade.getLastExecTime(
                execution.getPipeline(),
                PipelineExecutionStatus.FINISHED_SUCCESS);
        Date lastSucessWarn = pipelineFacade.getLastExecTime(
                execution.getPipeline(),
                PipelineExecutionStatus.FINISHED_WARNING);

        if (lastSucess == null) {
            this.lastSuccessfulExTime = lastSucessWarn;
        } else if (lastSucessWarn == null) {
            this.lastSuccessfulExTime = lastSucess;
        } else {
            // get last successful execution time
            this.lastSuccessfulExTime = lastSucess.after(lastSucessWarn) ? lastSucess
                    : lastSucessWarn;
        }
    }

    /**
     * Execute {@link PreExecutor} from {@link Executor#preExecutors}. If any {@link PreExecutor} return false then return false. If there are no
     * {@link PreExecutor} ({@link Executor#preExecutors} == null) then
     * instantly return true.
     * 
     * @param graph
     *            Dependency graph used for execution.
     * @return False if any of the pre-executors failed.
     */
    private boolean executePreExecutors(DependencyGraph graph) {
        if (preExecutors == null) {
            return true;
        }
        boolean success = true;
        for (PreExecutor item : preExecutors) {
            if (!item.preAction(execution, contexts, graph, success)) {
                LOG.error("PreProcessor: {} failed", item.getClass().getName());
                success = false;
            }
        }
        return success;
    }

    /**
     * Execute {@link PostExecutor} from {@link Executor#postExecutors}. If any {@link PostExecutor} return false then return false. If there are no
     * {@link PostExecutor} ({@link Executor#postExecutors} == null) then
     * instantly return true.
     * 
     * @param graph
     *            Dependency graph that has been used for execution.
     * @return False if any of the post-executors failed.
     */
    private boolean executePostExecutors(DependencyGraph graph) {
        if (postExecutors == null) {
            return true;
        }
        boolean success = true;
        for (PostExecutor item : postExecutors) {
            if (!item.postAction(execution, contexts, graph)) {
                LOG.error("PostProcessor: {} failed", item.getClass().getName());
                success = false;
            }
        }
        return success;
    }

    /**
     * Should be called in case that the execution failed. Does not save the {@link PipelineExecution} into database.
     */
    private void executionFailed() {
        execution.setStatus(PipelineExecutionStatus.FAILED);
    }

    /**
     * Should be called in case that the execution was cancelled by user. Does
     * not save the {@link PipelineExecution} into database.
     */
    private void executionCancelled() {
        execution.setStatus(PipelineExecutionStatus.CANCELLED);
    }

    /**
     * Should be called in case that the execution has finished without error.
     * Does not save the {@link PipelineExecution} into database.
     */
    private void executionSuccessful() {
        boolean warnings = false;
        // look if there is context that finished with warnings
        for (Context item : contexts.values()) {
            if (item.warningMessagePublished()) {
                warnings = true;
                break;
            }
        }

        if (warnings) {
            // ok we know that this is true
        } else {
            // test logs
            warnings = logFacade.existLogsGreaterOrEqual(execution, Level.WARN);
        }

        if (warnings) {
            execution.setStatus(PipelineExecutionStatus.FINISHED_WARNING);
        } else {
            execution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        }

    }

    /**
     * Prepare and return instance of {@link DependencyGraph}.
     * 
     * @return Dependency graph for currently executed pipeline.
     */
    private DependencyGraph prepareDependencyGraph() {
        final Pipeline pipeline = execution.getPipeline();
        // if in debug mode then pass the final DPU
        DependencyGraph dependencyGraph;
        if (execution.isDebugging() && execution.getDebugNode() != null) {
            dependencyGraph = new DependencyGraph(pipeline.getGraph(),
                    execution.getDebugNode());
        } else {
            dependencyGraph = new DependencyGraph(pipeline.getGraph());
        }
        return dependencyGraph;
    }

    /**
     * Run the execution.
     */
    private void execute() {
        LOG.info("execute() start of # {}", this.execution.getId());
        // get dependency graph
        DependencyGraph dependencyGraph = prepareDependencyGraph();

        // we need result state
        ExecutionResult execResult = new ExecutionResult();
        if (!dependencyGraph.iterator().hasNext()) {
            eventPublisher.publishEvent(PipelineFailedEvent.create(Messages.getString("Executor.execution.blank.pipeline"), Messages.getString("Executor.execution.blank.pipeline.no.dpus"), null, this.execution, this));
            execResult.failure();
        }
        // execute pre-executors
        if (!executePreExecutors(dependencyGraph)) {
            execResult.failure();
        }

        // and we also have evidance about user abort request
        boolean userAbortRequest = false;

        // execute each node
        for (Node node : dependencyGraph) {

            // check for the end of the execution
            // this test has to be here .. as the pre executors
            // can failed .. in such case no DPU will be launched
            if (!execResult.continueExecution() || userAbortRequest) {
                break;
            }

            // put dpuInstance id to MDC, so we can identify logs related to the
            // dpuInstance
            MDC.put(Log.MDC_DPU_INSTANCE_KEY_NAME,
                    Long.toString(node.getDpuInstance().getId()));

            cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUExecutor dpuExecutor = beanFactory
                    .getBean(cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUExecutor.class);

            try {
                dpuExecutor.bind(node, contexts, execution, lastSuccessfulExTime);
            } catch (ContextException e) {
                // failed to create context .. fail the execution
                eventPublisher.publishEvent(PipelineFailedEvent.create(e, node.getDpuInstance(), execution, this));
                execResult.failure();
                break;
            }

            LOG.info("Starting execution of dpu {} = {}", node.getDpuInstance()
                    .getId(), node.getDpuInstance().getName());

            final String threadName = "dpu: " + node.getDpuInstance().getName();
            Thread executorThread = new Thread(dpuExecutor, threadName);
            executorThread.start();

            // repeat until the executorThread is running
            while (executorThread.isAlive()) {
                try {
                    // sleep for five seconds
                    executorThread.join(5000);
                } catch (InterruptedException e) {
                    // request stop
                    stopExecution(executorThread, dpuExecutor);
                    // set stop to true
                    execResult.stop();
                    break;
                }

                // check for user request to stop execution -> we need new
                // instance
                PipelineExecution uptodateExecution = pipelineFacade
                        .getExecution(execution.getId());
                if (uptodateExecution == null) {
                    LOG.warn("Seems like someone deleted our execution.");
                    // stop execution
                    stopExecution(executorThread, dpuExecutor);
                    // set stop to true
                    execResult.stop();
                    break;
                } else if (uptodateExecution.getStop()) {
                    eventPublisher.publishEvent(new PipelineAbortedEvent(
                            execution, this));
                    // try to stop the DPU's execution thread
                    stopExecution(executorThread, dpuExecutor);
                    // update flag, so we do not override the value in database
                    execution.setStatus(PipelineExecutionStatus.CANCELLING);
                    // set flags
                    execResult.stop();
                    userAbortRequest = true;
                    break;
                }
            } // end of single DPU thread execution

            // merge result information
            ExecutionResult dpuResults = dpuExecutor.getExecResult();
            // check for corrent ending
            if (dpuResults.executionEndsProperly()) {
                // ok execution ends properly
            } else {
                // this mean that we end in non standart way ...
                // and this is equal to the failure
                dpuResults.failure();
                eventPublisher.publishEvent(PipelineFailedEvent.create(
                        Messages.getString("Executor.execution.failed"),
                        Messages.getString("Executor.execution.failed.detail"),
                        node.getDpuInstance(), execution, this));
            }
            execResult.add(dpuResults);
        }

        // apost executors are comming
        if (!executePostExecutors(dependencyGraph)) {
            // failed ..
            execResult.failure();
        }

        // make sure all logs are in database as we use them to determine
        // pipeline state
        try {
            logAppender.flush();
        } catch (Throwable e) {
            LOG.error("logAppender.flush() throws!!!", e);
        }

        // all done, resolve the way of ending .. 
        // set time then the pipeline's execution finished
        if (userAbortRequest) {
            executionCancelled();
        } else {
            if (execResult.executionEndsSuccessfully()) {
                executionSuccessful();
            } else {
                executionFailed();
            }
        }

    }

    @Override
    public void run() {
        // the execution start time has been already set in bind function
        // add marker to logs from this thread -> both must be specified !!
        final String executionId = Long.toString(execution.getId());
        if (!execution.isDebugging()) {
            // add minimal level to MDCExecutionLevelFilter
            MdcExecutionLevelFilter.add(executionId,
                    ch.qos.logback.classic.Level.INFO);
        }
        MDC.put(Log.MDC_EXECUTION_KEY_NAME, executionId);

        LOG.debug("Before publishing PipelineStarted event");
        eventPublisher.publishEvent(new PipelineStarted(this.execution, this));
        LOG.debug("After publishing PipelineStarted event");

        // execute the pipeline it self
        execute();

        // set end time
        execution.setEnd(new Date());

        // publish information for the rest of the application
        // that the execution finished ..
        eventPublisher.publishEvent(new PipelineFinished(execution, this));

        LOG.trace("Saving pipeline changes into SQL ..");

        // save the execution
        try {
            pipelineFacade.save(execution);
        } catch (EntityNotFoundException ex) {
            LOG.warn("Seems like someone deleted our pipeline run.", ex);
        }

        // we have to do above in this order as event create message and logs, 
        // those we flush into database
        // and then we change the state, which cause the frontneds refresh
        // to top .. but before that all the data will be ready in the database
        LOG.debug("Execution thread is about to finish ..");

        // unregister MDC execution filter
        MdcExecutionLevelFilter.remove(executionId);
        // clear all threads markers
        MDC.clear();
    }

    /**
     * Stops pipeline execution. Usually invoke by user action.
     * 
     * @param executorThread
     *            thread servicing execution which needs to be
     *            stopped
     * @param dpuExecutor
     *            DPUExecutor for given DPUs.
     */
    private void stopExecution(Thread executorThread,
            cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUExecutor dpuExecutor) {
        LOG.debug("Cancelling the DPU execution ...");
        // set cancel flag
        dpuExecutor.cancel();
        // interrupt executorThread, and wait for it ...
        try {
            // TODO Petr revise core code to kame sure that it's ready to work with potential interrupt
            executorThread.interrupt();
            executorThread.join();
        } catch (InterruptedException e) {
            // if we are interrupt stop waiting
        }
        LOG.debug("DPU thread cancelled");
    }

}
