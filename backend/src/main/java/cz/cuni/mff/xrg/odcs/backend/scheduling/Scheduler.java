package cz.cuni.mff.xrg.odcs.backend.scheduling;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFinished;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;

/**
 * Take care about execution of scheduled plans.
 * 
 * @author Petyr
 */
class Scheduler implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(Schedule.class);

    @Autowired
    private AppConfig appConfig;

    private boolean clusterMode = false;

    /**
     * Schedule facade.
     */
    @Autowired
    private ScheduleFacade scheduleFacade;

    private String backendID;

    @PostConstruct
    private void init() {
        try {
            this.clusterMode = this.appConfig.getBoolean(ConfigProperty.BACKEND_CLUSTER_MODE);
        } catch (MissingConfigPropertyException e) {
            // ignore
        }
        if (this.clusterMode) {
            this.backendID = this.appConfig.getString(ConfigProperty.BACKEND_ID);
        }
        initialCheck();

    }

    private void initialCheck() {
        // do initial run-after check 
        if (this.clusterMode) {
            this.scheduleFacade.executeFollowers(this.backendID);
        } else {
            this.scheduleFacade.executeFollowers();
        }
    }

    /**
     * Run pipelines that should be executed after given pipeline.
     * 
     * @param pipelineFinishedEvent
     */
    private synchronized void onPipelineFinished(PipelineFinished pipelineFinishedEvent) {
        LOG.trace("onPipelineFinished started");
        if (!pipelineFinishedEvent.sucess()) {
            return; // If pipeline not successful, no post-process 
        }

        if (pipelineFinishedEvent.getExecution().getSilentMode()) {
            // pipeline run in silent mode .. ignore
        } else {
            scheduleFacade.executeFollowers(
                    pipelineFinishedEvent.getExecution().getPipeline());
        }
        LOG.trace("onPipelineFinished finished");
    }

    /**
     * Check database for time-based schedules.
     * Run every 30 seconds.
     */
    @Async
    @Scheduled(fixedDelay = 30000)
    @Transactional
    protected synchronized void timeBasedCheck() {
        LOG.trace("onTimeCheck started");
        // check DB for pipelines based on time scheduling

        Date now = new Date();
        // get all pipelines that are time based
        LOG.debug("Going to check all time based not queued schedules");
        List<Schedule> candidates = null;
        if (this.clusterMode) {
            candidates = this.scheduleFacade.getAllTimeBasedNotQueuedRunningForCluster();
        } else {
            candidates = this.scheduleFacade.getAllTimeBasedNotQueuedRunning();
        }
        LOG.debug("Found {} schedule candidates, that could be executed", candidates.size());
        // check ..
        for (Schedule schedule : candidates) {
            // we use information about next execution
            Date nextExecution = schedule.getNextExecutionTimeInfo();
            if (nextExecution == null) {
                // do not run .. is disabled, missed it's time  
            } else if (nextExecution.before(now)) {
                LOG.debug("Executing id:{} time of execution is {}",
                        schedule.getId(),
                        nextExecution);
                scheduleFacade.execute(schedule);
            }
        }
        LOG.trace("onTimeCheck finished");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof PipelineFinished) {
            PipelineFinished pipelineFinishedEvent = (PipelineFinished) event;
            // ...
            LOG.trace("Received PipelineFinished event");
            onPipelineFinished(pipelineFinishedEvent);
        }

    }
}
