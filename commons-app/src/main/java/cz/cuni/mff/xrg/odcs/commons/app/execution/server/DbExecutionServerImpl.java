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
	if ( 1 == 1 ) {
	    // Version for Ms SQL Server 2014
	    final String queryStrMsSql = "UPDATE exec_pipeline SET backend_id = '%s'"
                + " WHERE id IN (SELECT id FROM"
	        + " (SELECT TOP %d e.id from exec_pipeline e WHERE e.backend_id IS NULL AND e.status = %d"
                + " ORDER BY e.order_number ASC, e.id ASC) AS temp)";

	    String query = String.format(queryStrMsSql,
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
	    String query = String.format(queryStrDefault,
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

}
