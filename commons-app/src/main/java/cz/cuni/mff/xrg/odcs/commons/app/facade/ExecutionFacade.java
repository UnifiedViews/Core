package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.execution.server.ExecutionServer;

public interface ExecutionFacade extends Facade {

    /**
     * Get all backend execution servers registered in the database
     * 
     * @return List of all backend servers
     */
    List<ExecutionServer> getAllExecutionServers();

    /**
     * Get execution server for given backend ID
     * 
     * @param backendId
     * @return Backend server entry for given ID
     */
    ExecutionServer getExecutionServer(String backendId);

    /**
     * Check if any of the backend executions servers registered in the database are active. <br/>
     * By default active means, that timestamp for some of the backends have been updated in less than 20s ago <br/>
     * Timeout for backend activity can be set via property 'backend.alive.limit'
     * 
     * @return True if at least one backend is active, False if no backend is active
     */
    boolean checkAnyBackendActive();

    /**
     * Updates timestamp of given backend to actual time
     * If backend with given ID not in the database yet, new entry with this ID and current time is created
     * 
     * @param backendId
     */
    void updateBackendTimestamp(String backendId);

    /**
     * Allocates QUEUED pipeline executions in database to backend execution server with given ID
     * This method is transactional and guarantees that each pipeline execution is taken only once only by one backend
     * Once allocated execution is further processed only by the allocating backend
     * Other backends will never touch it
     * <p/>
     * Queued executions with IGNORE priority are all allocated to first backend, and only limited count of non-ignore priority executions are allocated.
     * 
     * @param backendID
     *            Backend ID to allocate executions to
     * @param limit
     *            Limit of non-ignore priority executions to be allocated
     * @return Number of allocated executions for backend
     */
    int allocateQueuedExecutionsForBackend(String backendID, int limit);

    /**
     * Get count of unallocated QUEUED executions with IGNORE priority
     * 
     * @return Count of unallocated QUEUED executions with IGNORE priority
     */
    long getCountOfUnallocatedQueuedExecutionsWithIgnorePriority();

}
