package cz.cuni.mff.xrg.odcs.commons.app.execution.server;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

public class DbExecutionServerImpl extends DbAccessBase<ExecutionServer> implements DbExecutionServer {

    public DbExecutionServerImpl() {
        super(ExecutionServer.class);
    }

    @Override
    public ExecutionServer getExecutionServer(String backendId) {
        final String stringQuery = "SELECT e FROM ExecutionServer e WHERE e.backendId = :backendId";
        TypedQuery<ExecutionServer> query = createTypedQuery(stringQuery);
        query.setParameter("backendId", backendId);
        return execute(query);
    }

    @Override
    public List<ExecutionServer> getAllExecutionServers() {
        final String queryStr = "SELECT e FROM ExecutionServer e";
        return executeList(queryStr);
    }

    @Override
    @Transactional
    public int allocateQueuedExecutionsForBackendByPriority(String backendID, int limit) {
        final String queryStr = "UPDATE exec_pipeline SET backend_id = '%s'"
                + " WHERE id IN (SELECT id FROM"
                + " (SELECT e.id from exec_pipeline e WHERE e.backend_id IS NULL AND e.status = %d"
                + " ORDER BY e.order_number ASC, e.id ASC LIMIT %d FOR UPDATE) AS temp)";
        String query = String.format(queryStr,
                backendID,
                0, // = QUEUED
                limit);
        return this.em.createNativeQuery(query).executeUpdate();
    }

    @Override
    public long getCountOfUnallocatedQueuedExecutionsWithIgnorePriority() {
        final String stringQuery = "SELECT COUNT(e) FROM PipelineExecution e"
                + " WHERE e.status = :status"
                + " AND e.backendId IS NULL"
                + " AND e.orderNumber = :priority";
        TypedQuery<Long> query = createCountTypedQuery(stringQuery);
        query.setParameter("status", PipelineExecutionStatus.QUEUED);
        query.setParameter("priority", ScheduledJobsPriority.IGNORE.getValue());
        Long count = (Long) query.getSingleResult();

        return count;
    }

}
