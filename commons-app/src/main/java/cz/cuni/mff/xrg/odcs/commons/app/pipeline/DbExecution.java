package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.Date;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;

/**
 * Interface for access to {@link PipelineExecution}s.
 * Spring does not support autowired on generic types
 * 
 * @author Petyr
 * @author Jan Vojt
 */
public interface DbExecution extends DbAccess<PipelineExecution> {

    /**
     * @return all pipeline executions in the system
     */
    public List<PipelineExecution> getAll();

    /**
     * @param pipeline
     * @return all executions for given pipeline
     */
    public List<PipelineExecution> getAll(Pipeline pipeline);

    /**
     * @param status
     * @return all executions with given status
     */
    public List<PipelineExecution> getAll(PipelineExecutionStatus status);

    /**
     * Get all executions with given status ordered by priority
     * 
     * @param status
     *            Execution status
     * @return List with priority ordered executions
     */
    public List<PipelineExecution> getAllByPriorityLimited(PipelineExecutionStatus status);

    /**
     * Get all executions with given status and backend ID ordered by priority
     * 
     * @param status
     *            Execution status
     * @param backendID
     *            Backend ID
     * @return List of all executions with given status and backend ID
     */
    public List<PipelineExecution> getAllByPriorityLimited(PipelineExecutionStatus status, String backendID);

    /**
     * Get all executions with given status and backend ID
     * 
     * @param status
     *            Execution status
     * @param backendID
     *            Backend ID
     * @return List of all executions with given status and backend ID
     */
    public List<PipelineExecution> getAll(PipelineExecutionStatus status, String backendID);

    /**
     * @param pipeline
     * @param status
     * @return all executions of given pipeline with given status
     */
    public List<PipelineExecution> getAll(Pipeline pipeline, PipelineExecutionStatus status);

    /**
     * Return latest execution of given statuses for given pipeline. Ignore null
     * values.
     * 
     * @param pipeline
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    public PipelineExecution getLastExecution(Pipeline pipeline,
            Set<PipelineExecutionStatus> statuses);

    /**
     * Return latest execution of given statuses for given schedule. Ignore null
     * values.
     * 
     * @param schedule
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    public PipelineExecution getLastExecution(Schedule schedule,
            Set<PipelineExecutionStatus> statuses);

    /**
     * Tells whether there were any changes to pipeline executions since the
     * last load.
     * <p>
     * This method is provided purely for performance optimization of refreshing execution statuses. Functionality is backed by database trigger
     * &quot;update_last_change&quot;.
     * 
     * @param since
     * @return whether any execution was updated since given date
     */
    public boolean hasModified(Date since);

    /**
     * Checks if some of the executions were deleted
     * <p>
     * 
     * @param ids
     *            executions to check
     * @return true if one or more execution were deleted
     */
    public boolean hasDeleted(List<Long> ids);

    /**
     * Checks if there are executions for selected pipeline with selected statuses
     * 
     * @param pipeline
     *            for which executions we are checking
     * @param statuses
     *            of executions we are checking
     * @return true if there is at least one execution with selected statuses, false otherwise
     */
    boolean hasWithStatus(Pipeline pipeline, List<PipelineExecutionStatus> statuses);

}
