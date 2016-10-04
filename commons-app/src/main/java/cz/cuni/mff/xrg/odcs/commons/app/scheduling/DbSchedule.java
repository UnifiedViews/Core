package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import java.util.Date;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;

/**
 * Interface providing access to {@link Schedule} data objects.
 * 
 * @author Jan Vojt
 */
public interface DbSchedule extends DbAccess<Schedule> {

    /**
     * Returns list of all Plans currently persisted in database.
     * 
     * @return list of scheduled jobs
     * @deprecated may be slow for many schedules, use paging instead
     */
    @Deprecated
    public List<Schedule> getAllSchedules();

    /**
     * Fetches all {@link Schedule}s planned for given pipeline.
     * 
     * @param pipeline
     * @return all {@link Schedule}s planned for given pipeline.
     */
    public List<Schedule> getSchedulesFor(Pipeline pipeline);

    /**
     * Fetches all {@link Schedule}s that should be activated after given
     * pipeline execution.
     * 
     * @param pipeline
     *            pipeline to follow
     * @param enabled
     *            <ul>
     *            <li>if true return only followers with enabled schedules,</li>
     *            <li>if false return only followers with disabled schedules,</li>
     * @return schedules configured to follow given pipeline
     */
    public List<Schedule> getFollowers(Pipeline pipeline, boolean enabled);

    /**
     * Fetches all {@link Schedule}s which are activated in
     * certain time and the execution for the scheduled pipeline
     * isn't already queued or running.
     * 
     * @return list of schedules
     */
    public List<Schedule> getAllTimeBasedNotQueuedRunning();

    /**
     * Fetches all {@link Schedule}s which are activated in
     * certain time and the execution for the scheduled pipeline
     * isn't already queued or running.
     * 
     * @return list of schedules
     */
    public List<Schedule> getAllTimeBasedNotQueuedRunningForCluster();

    /**
     * Fetches active (enabled) {@link Schedule}s which are activated based on
     * pipelines executions.
     * 
     * @return list of schedules
     */
    public List<Schedule> getActiveRunAfterBased();

    /**
     * Return times of last executions (or null if there has been no successful
     * execution) of run-after pipelines for runAfter base schedule.
     * 
     * @param schedule
     * @return list of timestamps
     */
    public List<Date> getLastExecForRunAfter(Schedule schedule);

    /**
     * Return times of last executions (or null if there has been no successful
     * execution) of run-after pipelines for runAfter base schedule.
     * 
     * @param schedule
     * @param backendID
     * @return list of timestamps
     */
    public List<Date> getLastExecForRunAfter(Schedule schedule, String backendID);

}
