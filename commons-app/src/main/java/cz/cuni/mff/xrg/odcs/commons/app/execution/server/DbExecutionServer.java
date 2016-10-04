package cz.cuni.mff.xrg.odcs.commons.app.execution.server;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

public interface DbExecutionServer extends DbAccess<ExecutionServer> {

    /**
     * Get backend execution server entry via its ID
     * 
     * @param backendId
     * @return Backend execution server
     */
    ExecutionServer getExecutionServer(String backendId);

    /**
     * Get list of all executions backend servers registered in database
     * 
     * @return List of all backend servers
     */
    List<ExecutionServer> getAllExecutionServers();

    /**
     * Allocates QUEUED pipeline executions in database to backend execution server with given ID
     * This method is transactional and guarantees that each pipeline execution is taken only once only by one backend
     * Once allocated execution is further processed only by the allocating backend
     * Other backends will never touch it
     * 
     * @param backendID
     *            Backend ID to allocate executions to
     * @param limit
     *            Limit of executions to be allocated
     * @return Number of allocated executions for backend
     */
    int allocateQueuedExecutionsForBackendByPriority(String backendID, int limit);

    /**
     * Get count of unallocated QUEUED executions with IGNORE priority
     * 
     * @return Count of unallocated QUEUED executions with IGNORE priority
     */
    long getCountOfUnallocatedQueuedExecutionsWithIgnorePriority();

}
