package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

/**
 * Implementation providing access to {@link Schedule} data objects.
 *
 * @author Jan Vojt
 * @author Petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
public class DbScheduleImpl extends DbAccessBase<Schedule>
        implements DbSchedule {

    public DbScheduleImpl() {
        super(Schedule.class);
    }

    @Override
    public List<Schedule> getAllSchedules() {
        final String queryStr = "SELECT e FROM Schedule e";
        return executeList(queryStr);
    }

    @Override
    public List<Schedule> getSchedulesFor(Pipeline pipeline) {
        final String stringQuery = "SELECT e FROM Schedule e WHERE e.pipeline = :pipeline";
        TypedQuery<Schedule> query = createTypedQuery(stringQuery);
        query.setParameter("pipeline", pipeline);
        return executeList(query);
    }

    @Override
    public List<Schedule> getFollowers(Pipeline pipeline, boolean enabled) {
        final String stringQuery = "SELECT s FROM Schedule s JOIN s.afterPipelines p"
                + " WHERE p = :pipeline"
                + " AND s.type = :type"
                + " AND s.enabled = :enabled";
        TypedQuery<Schedule> query = createTypedQuery(stringQuery);
        query.setParameter("pipeline", pipeline);
        query.setParameter("type", ScheduleType.AFTER_PIPELINE);
        query.setParameter("enabled", enabled);
        return executeList(query);
    }

    @Override
    public List<Schedule> getAllTimeBasedNotQueuedRunning() {
        final List<PipelineExecutionStatus> status = Arrays.asList(
                PipelineExecutionStatus.QUEUED,
                PipelineExecutionStatus.RUNNING);

        final String stringQuery = "SELECT s FROM Schedule s"
                + " WHERE s.type = :type AND s.id NOT IN ("
                + " SELECT s1.id FROM Schedule s1"
                + " LEFT JOIN PipelineExecution e"
                + " WHERE e.pipeline = s1.pipeline AND e.status IN :status)"
                + " order by s.id Asc";
        TypedQuery<Schedule> query = createTypedQuery(stringQuery);
        query.setParameter("type", ScheduleType.PERIODICALLY);
        query.setParameter("status", status);
        return executeList(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Schedule> getAllTimeBasedNotQueuedRunningForCluster() {

        final String queryStr = "SELECT * FROM exec_schedule s"
                + " WHERE s.type = %d AND s.id NOT IN ("
                + " SELECT s1.id FROM exec_schedule s1"
                + " LEFT JOIN exec_pipeline e"
                + " ON e.id = s1.pipeline_id WHERE e.status IN (%d, %d))"
                + " ORDER BY s.id ASC"
                + " FOR UPDATE";

        String query = String.format(queryStr, 1, 0, 1);
        return this.em.createNativeQuery(query, Schedule.class).getResultList();
    }

    @Override
    public List<Schedule> getActiveRunAfterBased() {
        final String stringQuery = "SELECT s FROM Schedule s"
                + " WHERE s.type = :type"
                + " AND s.enabled = 1";
        TypedQuery<Schedule> query = createTypedQuery(stringQuery);
        query.setParameter("type", ScheduleType.AFTER_PIPELINE);
        return executeList(query);
    }

    @Override
    public List<Date> getLastExecForRunAfter(Schedule schedule) {
        final String stringQuery = "SELECT max(exec.end)"
                + " FROM Schedule schedule"
                + " JOIN schedule.afterPipelines pipeline"
                + " JOIN PipelineExecution exec ON exec.pipeline = pipeline"
                + " WHERE schedule.id = :schedule AND exec.status IN :status"
                + " GROUP BY pipeline.id";

        Set<PipelineExecutionStatus> statuses = new HashSet<>();
        statuses.add(PipelineExecutionStatus.FINISHED_SUCCESS);
        statuses.add(PipelineExecutionStatus.FINISHED_WARNING);

        TypedQuery<Date> query = em.createQuery(stringQuery, Date.class);
        query.setParameter("schedule", schedule.getId());
        query.setParameter("status", statuses);

        return Collections.checkedList(query.getResultList(), Date.class);
    }

    @Override
    public List<Date> getLastExecForRunAfter(Schedule schedule, String backendId) {
        final String stringQuery = "SELECT max(exec.end)"
                + " FROM Schedule schedule"
                + " JOIN schedule.afterPipelines pipeline"
                + " JOIN PipelineExecution exec ON exec.pipeline = pipeline"
                + " WHERE schedule.id = :schedule AND exec.status IN :status AND exec.backendId = :backendId"
                + " GROUP BY pipeline.id";

        Set<PipelineExecutionStatus> statuses = new HashSet<>();
        statuses.add(PipelineExecutionStatus.FINISHED_SUCCESS);
        statuses.add(PipelineExecutionStatus.FINISHED_WARNING);

        TypedQuery<Date> query = this.em.createQuery(stringQuery, Date.class);
        query.setParameter("schedule", schedule.getId());
        query.setParameter("status", statuses);
        query.setParameter("backendId", backendId);

        return Collections.checkedList(query.getResultList(), Date.class);
    }

}
