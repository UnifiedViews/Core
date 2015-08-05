package cz.cuni.mff.xrg.odcs.backend.execution;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import cz.cuni.mff.xrg.odcs.backend.execution.event.CheckDatabaseEvent;
import cz.cuni.mff.xrg.odcs.backend.execution.pipeline.Executor;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFinished;
import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ExecutionFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;

/**
 * Responsible for running and supervision queue of PipelineExecution tasks.
 * 
 * @author Petyr
 */
public class Engine implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);

    private static final Integer DEFAULT_LIMIT_SHEDULED_PPL = 2;

    public Integer numberOfRunningJobs = 0;

    private final Object LockRunningJobs = new Object();

    private boolean clusterMode = false;

    /**
     * Publisher instance.
     */
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    /**
     * Bean factory used to create beans for single pipeline execution.
     */
    @Autowired
    private BeanFactory beanFactory;

    /**
     * Pipeline facade.
     */
    @Autowired
    protected PipelineFacade pipelineFacade;

    /**
     * Runtime properties facade.
     */
    @Autowired
    protected RuntimePropertiesFacade runtimePropertiesFacade;

    @Autowired
    protected ExecutionFacade executionFacade;

    /**
     * Thread pool.
     */
    protected ExecutorService executorService;

    /**
     * Working directory.
     */
    protected File workingDirectory;

    /**
     * True if startUp method has already been called.
     */
    protected Boolean startUpDone;

    /**
     * Backend identifier
     */
    protected String backendID;

    @PostConstruct
    private void propertySetter() {
        this.executorService = Executors.newCachedThreadPool();
        this.startUpDone = false;

        workingDirectory = new File(
                appConfig.getString(ConfigProperty.GENERAL_WORKINGDIR));
//        limitOfScheduledPipelines = appConfig.getInteger(ConfigProperty.BACKEND_LIMIT_OF_SCHEDULED_PIPELINES);
        LOG.info("Working dir: {}", workingDirectory.toString());
        // make sure that our working directory exist
        if (workingDirectory.isDirectory()) {
            workingDirectory.mkdirs();
        }

        try {
            this.clusterMode = this.appConfig.getBoolean(ConfigProperty.BACKEND_CLUSTER_MODE);
        } catch (MissingConfigPropertyException e) {
            // ignore
        }
        if (this.clusterMode) {
            this.backendID = this.appConfig.getString(ConfigProperty.BACKEND_ID);
            LOG.info("Backend ID: {}", this.backendID);
        }
    }

    /**
     * Ask executorService to run the pipeline. Call {@link #startUp} before
     * this function.
     * 
     * @param execution
     */
    protected void run(PipelineExecution execution) {
        Executor executor = beanFactory.getBean(Executor.class);
        executor.bind(execution);
        // execute
        this.executorService.submit(executor);
    }

    /**
     * Check database for new task (PipelineExecutions to run). Can run
     * concurrently. Check database every 2 seconds.
     */

    @Async
    @Scheduled(fixedDelay = 2000)
    protected void checkJobs() {
        synchronized (LockRunningJobs) {
            LOG.debug(">>> Entering checkJobs()");
            if (!startUpDone) {
                // we does not start any execution
                // before start up method is executed
                startUp();
                return;
            }

            Integer limitOfScheduledPipelines = getLimitOfScheduledPipelines();
            LOG.debug("limit of scheduled pipelines: " + limitOfScheduledPipelines);
            LOG.debug("Number of running jobs: {}", this.numberOfRunningJobs);

            List<PipelineExecution> jobs = null;
            if (this.clusterMode) {
                // Update backend activity timestamp in DB
                this.executionFacade.updateBackendTimestamp(this.backendID);
                int limit = limitOfScheduledPipelines - this.numberOfRunningJobs;
                if (limit < 0) {
                    limit = 0;
                }
                long countOfUnallocated = this.executionFacade.getCountOfUnallocatedQueuedExecutionsWithIgnorePriority();
                if (limit < countOfUnallocated) {
                    limit = (int) countOfUnallocated;
                }

                int allocated = this.executionFacade.allocateQueuedExecutionsForBackend(this.backendID, limit);
                LOG.debug("Allocated {} executions by backend '{}'", allocated, this.backendID);

                LOG.debug("Going to find all allocated QUEUED executions");
                jobs = this.pipelineFacade.getAllExecutionsByPriorityLimited(PipelineExecutionStatus.QUEUED, this.backendID);
                LOG.debug("Found {} executions planned for execution", jobs.size());
            } else {
                jobs = this.pipelineFacade.getAllExecutionsByPriorityLimited(PipelineExecutionStatus.QUEUED);
            }
            // run pipeline executions ..
            for (PipelineExecution job : jobs) {
                if (this.numberOfRunningJobs < limitOfScheduledPipelines || ScheduledJobsPriority.IGNORE.getValue() == job.getOrderNumber()) {
                    run(job);
                    this.numberOfRunningJobs++;
                }
            }

            LOG.debug("<<< Leaving checkJobs: {}");
        }
    }

    /**
     * Gets runtime property for number of parallel running pipelines from database. If
     * not set or its set wrongly gets default limit.
     * 
     * @return limit for number of parallel running pipelines
     */
    protected Integer getLimitOfScheduledPipelines() {
        RuntimeProperty limit = runtimePropertiesFacade.getByName(ConfigProperty.BACKEND_LIMIT_OF_SCHEDULED_PIPELINES.toString());
        if (limit == null) {
            return DEFAULT_LIMIT_SHEDULED_PPL;
        }
        try {
            return Integer.parseInt(limit.getValue());
        } catch (NumberFormatException e) {
            LOG.error("Value not a number of RuntimeProperty: " + ConfigProperty.BACKEND_LIMIT_OF_SCHEDULED_PIPELINES.toString()
                    + ", error: " + e.getMessage());
            LOG.warn("Setting limit of scheduled pipelines to default value: " + DEFAULT_LIMIT_SHEDULED_PPL);
            return DEFAULT_LIMIT_SHEDULED_PPL;
        }
    }

    /**
     * Check database for hanging running pipelines. Should be run just once
     * before any execution starts.
     * Also setup engine according to it's configuration.
     */
    protected synchronized void startUp() {
        if (startUpDone) {
            LOG.warn("Ignoring second startUp call");
            return;
        }
        startUpDone = true;

        ExecutionSanitizer sanitizer = beanFactory.getBean(ExecutionSanitizer.class);

        // list executions
        List<PipelineExecution> running = null;
        if (this.clusterMode) {
            running = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.RUNNING, this.backendID);
        } else {
            running = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.RUNNING);
        }
        for (PipelineExecution execution : running) {
            MDC.put(Log.MDC_EXECUTION_KEY_NAME, execution.getId().toString());
            // hanging pipeline ..
            sanitizer.sanitize(execution);

            try {
                pipelineFacade.save(execution);
            } catch (EntityNotFoundException ex) {
                LOG.warn("Seems like someone deleted our pipeline run.", ex);
            }

            MDC.remove(Log.MDC_EXECUTION_KEY_NAME);
        }

        List<PipelineExecution> cancelling = null;
        if (this.clusterMode) {
            cancelling = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.CANCELLING, this.backendID);
        } else {
            cancelling = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.CANCELLING);
        }

        for (PipelineExecution execution : cancelling) {
            MDC.put(Log.MDC_EXECUTION_KEY_NAME, execution.getId().toString());
            // hanging pipeline ..
            sanitizer.sanitize(execution);

            try {
                pipelineFacade.save(execution);
            } catch (EntityNotFoundException ex) {
                LOG.warn("Seems like someone deleted our pipeline run.", ex);
            }

            MDC.remove(Log.MDC_EXECUTION_KEY_NAME);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof PipelineFinished) {
            synchronized (LockRunningJobs) {
                if (numberOfRunningJobs >= 0)
                    numberOfRunningJobs--;
            }
            LOG.trace("Received PipelineFinished event");
        }
        if (event instanceof CheckDatabaseEvent) {
            checkJobs();
        }
    }

}
