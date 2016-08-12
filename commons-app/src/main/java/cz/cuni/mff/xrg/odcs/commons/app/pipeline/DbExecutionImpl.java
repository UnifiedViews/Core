package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;

/**
 * Implementation of {@link DbExecution}
 * 
 * @author Petyr
 */
@Transactional(propagation = Propagation.MANDATORY)
class DbExecutionImpl extends DbAccessBase<PipelineExecution>implements DbExecution {

    private static Logger LOG = LoggerFactory.getLogger(DbExecutionImpl.class);

    protected DbExecutionImpl() {
        super(PipelineExecution.class);
    }

    @Override
    public List<PipelineExecution> getAll() {
        final String stringQuery = "SELECT e FROM PipelineExecution e";
        return executeList(stringQuery);
    }

    @Override
    public List<PipelineExecution> getAll(Pipeline pipeline) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.pipeline = :pipe";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        return executeList(query);
    }

    @Override
    public List<PipelineExecution> getAll(PipelineExecutionStatus status) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.status = :status";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("status", status);
        return executeList(query);
    }

    @Override
    public List<PipelineExecution> getAllByPriorityLimited(PipelineExecutionStatus status) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.status = :status and e.orderNumber >= :limited_priority order by e.orderNumber ASC , e.id ASC";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("limited_priority", ScheduledJobsPriority.IGNORE.getValue());
        query.setParameter("status", status);
        return executeList(query);
    }

    @Override
    public List<PipelineExecution> getAllByPriorityLimited(PipelineExecutionStatus status, String backendID) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.status = :status and e.backendId = :backendId and e.orderNumber >= :limited_priority "
                + "order by e.orderNumber ASC , e.id ASC";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("limited_priority", ScheduledJobsPriority.IGNORE.getValue());
        query.setParameter("status", status);
        query.setParameter("backendId", backendID);
        return executeList(query);
    }

    @Override
    public List<PipelineExecution> getAll(Pipeline pipeline, PipelineExecutionStatus status) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.pipeline = :pipe AND e.status = :status";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        query.setParameter("status", status);
        return executeList(query);
    }

    @Override
    public List<PipelineExecution> getAll(PipelineExecutionStatus status, String backendID) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.status = :status AND e.backendId = :backend";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("status", status);
        query.setParameter("backend", backendID);
        return executeList(query);
    }

    @Override
    public PipelineExecution getLastExecution(Pipeline pipeline,
            Set<PipelineExecutionStatus> statuses) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.pipeline = :pipe"
                + " AND e.status IN :status"
                + " AND e.end IS NOT NULL"
                + " ORDER BY e.end DESC";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("pipe", pipeline);
        query.setParameter("status", statuses);
        return execute(query);
    }

    @Override
    public PipelineExecution getLastExecution(Schedule schedule,
            Set<PipelineExecutionStatus> statuses) {
        final String stringQuery = "SELECT e FROM PipelineExecution e"
                + " WHERE e.schedule = :schedule"
                + " AND e.status IN :status"
                + " AND e.end IS NOT NULL"
                + " ORDER BY e.end DESC";
        TypedQuery<PipelineExecution> query = createTypedQuery(stringQuery);
        query.setParameter("schedule", schedule);
        query.setParameter("status", statuses);
        return execute(query);
    }

    @Override
    public boolean hasModified(Date since) {
        final String stringQuery = "SELECT MAX(e.lastChange)"
                + " FROM PipelineExecution e";

        TypedQuery<Date> query = em.createQuery(stringQuery, Date.class);
        Date lastModified = (Date) query.getSingleResult();

        if (lastModified == null) {
            // there are no executions in DB
            return false;
        }

        return lastModified.after(since);
    }

    @Override
    public boolean hasDeleted(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        final String stringQuery = "SELECT COUNT(e) FROM PipelineExecution e"
                + " WHERE e.id IN :ids";
        TypedQuery<Long> query = createCountTypedQuery(stringQuery);
        query.setParameter("ids", ids);
        Long number = (Long) query.getSingleResult();
        return !number.equals((long) ids.size());
    }

    @Override
    public boolean hasWithStatus(Pipeline pipeline, List<PipelineExecutionStatus> statuses) {
        final String stringQuery = "SELECT COUNT(e) FROM PipelineExecution e"
                + " WHERE e.pipeline = :pipeline"
                + " AND e.status IN :statuses ";
        TypedQuery<Long> query = createCountTypedQuery(stringQuery);
        query.setParameter("pipeline", pipeline);
        query.setParameter("statuses", statuses);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

}
