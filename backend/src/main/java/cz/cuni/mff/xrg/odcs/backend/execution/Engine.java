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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.backend.execution.event.CheckDatabaseEvent;
import cz.cuni.mff.xrg.odcs.backend.execution.pipeline.Executor;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFinished;
import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
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
    private String backendID;

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

        this.backendID = this.appConfig.getString(ConfigProperty.BACKEND_ID);
        LOG.info("Backend ID: {}", this.backendID);
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
     * concurrently. Check database every 20 seconds.
     */

    @Async
    @Scheduled(fixedDelay = 20000)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    protected void checkJobs() {
        synchronized (LockRunningJobs) {
            LOG.debug(">>> Entering checkJobs()");
            if (!startUpDone) {
                // we does not start any execution
                // before start up method is executed
                startUp();
                return;
            }
            boolean lockOwned = this.executionFacade.obtainLockAndUpdateTimestamp(this.backendID);
            if (!lockOwned) {
                LOG.info("Database backend lock not obtained, going to sleep before trying again");
                return;
            }

            Integer limitOfScheduledPipelines = getLimitOfScheduledPipelines();
            LOG.debug("limit of scheduled pipelines: " + limitOfScheduledPipelines);

            List<PipelineExecution> jobs = pipelineFacade.getAllExecutionsByPriorityLimited(PipelineExecutionStatus.QUEUED);
            // run pipeline executions ..
            for (PipelineExecution job : jobs) {
                if (job.getOrderNumber() == ScheduledJobsPriority.IGNORE.getValue()) {
                    run(job);
                    numberOfRunningJobs++;
                    continue;
                }

                if (numberOfRunningJobs < limitOfScheduledPipelines) {
                    run(job);
                    numberOfRunningJobs++;
                } else {
                    break;
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
        List<PipelineExecution> running = this.pipelineFacade.getAllExecutions(PipelineExecutionStatus.RUNNING, this.backendID);
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

        List<PipelineExecution> cancelling = pipelineFacade
                .getAllExecutions(PipelineExecutionStatus.CANCELLING);

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
