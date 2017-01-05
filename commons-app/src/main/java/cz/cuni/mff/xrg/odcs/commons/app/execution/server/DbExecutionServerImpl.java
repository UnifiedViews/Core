package cz.cuni.mff.xrg.odcs.commons.app.execution.server;

import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessBase;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

public class DbExecutionServerImpl extends DbAccessBase<ExecutionServer> implements DbExecutionServer {

    public DbExecutionServerImpl() {
        super(ExecutionServer.class);
    }

    private String sqlDriverInfo;

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
        String query = null;
        if ( sqlDriverInfo.startsWith("com.microsoft.sqlserver.jdbc") ) {
	    // Version for Ms SQL Server (with TOP instead of LIMIT)
        // sqlDriverInfo is taken from config.properties, see: commons-app-context.xml
	    final String queryStrMsSql = "UPDATE exec_pipeline SET backend_id = '%s'"
                + " WHERE id IN (SELECT id FROM"
	        + " (SELECT TOP %d e.id from exec_pipeline e WHERE e.backend_id IS NULL AND e.status = %d"
                + " ORDER BY e.order_number ASC, e.id ASC) AS temp)";

	    query = String.format(queryStrMsSql,
					 backendID,
					 limit,
					 0 // = QUEUED
					 );
	} else {
	    // Version for everything else (note limit/status params are inverse)
	    final String queryStrDefault = "UPDATE exec_pipeline SET backend_id = '%s'"
                + " WHERE id IN (SELECT id FROM"
	        + " (SELECT e.id from exec_pipeline e WHERE e.backend_id IS NULL AND e.status = %d"
                + "  ORDER BY e.order_number ASC, e.id ASC LIMIT %d) AS temp)";
	    query = String.format(queryStrDefault,
					 backendID,
					 0, // = QUEUED
					 limit
					 );
	};
	
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

    @Override
    public void setSqlDriverInfo(String sqlDriverInfo) {
        this.sqlDriverInfo = sqlDriverInfo;
    }

    @Override
    public String getSqlDriverInfo() {
        return sqlDriverInfo;
    }
}
