package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Facade providing actions with pipelines.
 * 
 * @author Jan Vojt
 */
public interface PipelineFacade extends Facade {

    /* ******************* Methods for managing Pipeline ******************** */
    /**
     * Pipeline factory with preset currently logged-in {@link User} as owner.
     * Created instance is not yet managed by {@link EntityManager}, thus needs
     * to be saved with {@link #save(Pipeline)} method.
     * 
     * @return newly created pipeline
     */
    Pipeline createPipeline();

    /**
     * Creates a clone of given pipeline, persists it, and returns it as a new
     * instance. Original owner is not preserved, rather currently logged in
     * user is set as an owner of the newly created pipeline.
     * 
     * @param pipeline
     *            original pipeline to copy
     * @return newly copied pipeline
     */
    Pipeline copyPipeline(Pipeline pipeline);

    /**
     * Returns list of all pipelines persisted in the database.
     * 
     * @return list of pipelines
     * @deprecated performance intensive for many pipelines in DB, use lazy
     *             container with paging instead
     */
    List<Pipeline> getAllPipelines();

    /**
     * Returns list of all pipelines persisted in the database for given organization.
     *
     * @param organizationExternalId of pipeline
     * @return list of pipelines
     * @deprecated performance intensive for many pipelines in DB, use lazy
     *             container with paging instead
     */
    List<Pipeline> getAllPipelines(Long organizationExternalId);

    /**
     * Find pipeline in database by ID and return it.
     * 
     * @param id
     *            of Pipeline
     * @return Pipeline the found pipeline or null if the pipeline with given ID
     *         does not exist
     */
    Pipeline getPipeline(long id);

    /**
     * Saves any modifications made to the pipeline into the database.
     * 
     * @param pipeline
     */
    void save(Pipeline pipeline);

    /**
     * Deletes pipeline from database.
     * 
     * @param pipeline
     */
    void delete(Pipeline pipeline);

    /**
     * Fetches pipelines using given DPU template which are visible to currently
     * logged-in user.
     * 
     * @param dpu
     *            template
     * @return pipelines using DPU template
     */
    List<Pipeline> getPipelinesUsingDPU(DPUTemplateRecord dpu);

    /**
     * Fetches all pipelines using given DPU template.
     * 
     * @param dpu
     *            template
     * @return pipelines using DPU template
     */
    List<Pipeline> getAllPipelinesUsingDPU(DPUTemplateRecord dpu);

    /**
     * Checks for duplicate pipeline names. The name of pipeline in second
     * argument is ignored, if given. It is to be used when editing already
     * existing pipeline.
     * 
     * @param newName
     * @param pipeline
     *            to be renamed, or null
     * @return whether there already is a pipeline with the same name in the
     *         system
     */
    boolean hasPipelineWithName(String newName, Pipeline pipeline);

    /**
     * Lists all private DPU templates which are used in given pipeline.
     * 
     * @param pipeline
     *            to inspect for private DPUs
     * @return list of private DPUs used in pipeline
     */
    List<DPUTemplateRecord> getPrivateDPUs(Pipeline pipeline);

    /**
     * Creates an open pipeline event with current timestamp. User is taken from
     * authentication context (currently logged in user).
     * 
     * @param pipeline
     *            which is open
     */
    void createOpenEvent(Pipeline pipeline);

    /**
     * Lists all open events representing a list of pipeline that are currently
     * open in pipeline canvas. Events of currently logged in user are ignored
     * and not included in the resulting list.
     * 
     * @param pipeline
     * @return list of open events
     */
    List<OpenEvent> getOpenPipelineEvents(Pipeline pipeline);

    /**
     * Checks if (possibly detached) pipeline has been modified by someone else.
     * 
     * @param pipeline
     *            to check
     * @return true if pipeline was changed while detached from entity manager,
     *         false otherwise
     */
    boolean isUpToDate(Pipeline pipeline);

    /* ******************** Methods for managing PipelineExecutions ********* */
    /**
     * Creates a new {@link PipelineExecution}, which represents a pipeline run.
     * Created instance is not yet managed by {@link EntityManager}, thus needs
     * to be saved with {@link #save(PipelineExecution)} method.
     * 
     * @param pipeline
     * @return pipeline execution of given pipeline
     */
    PipelineExecution createExecution(Pipeline pipeline);

    /**
     * Fetches all {@link PipelineExecution}s from database.
     *
     * @return list of executions
     * @deprecated performance intensive for many pipeline executions, use
     *             container with paging support instead
     */
    List<PipelineExecution> getAllExecutions();



    /**
     * Fetches all {@link PipelineExecution}s with given state from database.
     * 
     * @param status
     * @return list of executions
     */
    List<PipelineExecution> getAllExecutions(PipelineExecutionStatus status);


    List<PipelineExecution> getAllExecutionsByPriorityLimited(PipelineExecutionStatus status);


    /**
     * Find pipeline execution in database by ID and return it.
     * 
     * @param id
     *            of PipelineExecution
     * @return PipelineExecution
     */
    PipelineExecution getExecution(long id);

    /**
     * Fetch all executions for given pipeline.
     * 
     * @param pipeline
     * @return pipeline executions
     */
    List<PipelineExecution> getExecutions(Pipeline pipeline);

    /**
     * Fetch executions for given pipeline in given status.
     * 
     * @param pipeline
     *            Pipeline which executions should be fetched.
     * @param status
     *            Execution status, in which execution should be.
     * @return PipelineExecutions
     */
    List<PipelineExecution> getExecutions(Pipeline pipeline, PipelineExecutionStatus status);

    /**
     * Return end time of latest execution time of given status for given
     * pipeline. Ignore null values.
     * 
     * @param pipeline
     * @param status
     *            Execution status, used to filter pipelines.
     * @return latest execution time of given status for given pipeline or null.
     */
    Date getLastExecTime(Pipeline pipeline, PipelineExecutionStatus status);

    /**
     * Return latest execution of given statuses for given pipeline. Ignore null
     * values.
     * 
     * @param pipeline
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    PipelineExecution getLastExec(Pipeline pipeline,
            Set<PipelineExecutionStatus> statuses);

    /**
     * Return latest execution of given pipeline. Ignore null values.
     * 
     * @param pipeline
     * @return last execution or null
     */
    PipelineExecution getLastExec(Pipeline pipeline);

    /**
     * Return latest execution of given statuses for given schedule. Ignore null
     * values.
     * 
     * @param schedule
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    PipelineExecution getLastExec(Schedule schedule,
            Set<PipelineExecutionStatus> statuses);

    /**
     * Tells whether there were any changes to pipeline executions since the
     * last load.
     * <p>
     * This method is provided purely for performance optimization of refreshing execution statuses. Functionality is backed by database trigger
     * &quot;update_last_change&quot;.
     * 
     * @param lastLoad
     * @return whether there were any changes to pipeline executions since given
     *         time
     */
    boolean hasModifiedExecutions(Date lastLoad);

    /**
     * Tells whether there were any changes to pipelines since the
     * last load.
     * <p>
     * 
     * @param lastLoad
     * @return
     */
    public boolean hasModifiedPipelines(Date lastLoad);
    
    /**
     * Tells whether one of pipelines was deleted
     * <p>
     * 
     * @param pipelinesIds
     * @return true if one or more pipelines with provided ids were deleted, otherwise false 
     */
    public boolean hasDeletedPipelines(List<Long> pipelineIds);
    
    /**
     * Persists new {@link PipelineExecution} or updates it if it was already
     * persisted before.
     * 
     * @param exec
     */
    void save(PipelineExecution exec);

    /**
     * Deletes pipeline from database.
     * 
     * @param exec
     */
    void delete(PipelineExecution exec);

    /**
     * Stop the execution.
     * 
     * @param execution
     */
    void stopExecution(PipelineExecution execution);

    /**
     * Checks if some of the executions were deleted
     * @param executionIds execution to check
     * @return true if one or more execution were deleted
     */
	boolean hasDeletedExecutions(List<Long> executionIds);

	/**
     * Checks if there are executions for selected pipeline with selected statuses
     * 
     * @param pipeline for which executions we are checking
     * @param statuses of executions we are checking
     * @return true if there is at least one execution with selected statuses, false otherwise
     */
    boolean hasExecutionsWithStatus(Pipeline pipeline, List<PipelineExecutionStatus> statuses);

}
