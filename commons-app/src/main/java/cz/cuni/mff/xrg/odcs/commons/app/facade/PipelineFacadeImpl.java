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
package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbOpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbPipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Facade providing actions with pipelines.
 * 
 * @author Jan Vojt
 */
@Transactional(readOnly = true)
class PipelineFacadeImpl implements PipelineFacade {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineFacadeImpl.class);

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    @Autowired
    private DbPipeline pipelineDao;

    @Autowired
    private DbExecution executionDao;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private DbOpenEvent openEventDao;

    /**
     * Timeout how long will we consider {@link OpenEvent} active.
     */
    private static final int PPL_OPEN_TTL = 10;

    /* ******************* Methods for managing Pipeline ******************** */
    /**
     * Pipeline factory with preset currently logged-in {@link User} as owner.
     * Created instance is not yet managed by {@link EntityManager}, thus needs
     * to be saved with {@link #save(Pipeline)} method.
     * 
     * @return newly created pipeline
     */
    @PreAuthorize("hasRole('pipeline.create')")
    @Override
    public Pipeline createPipeline() {
        Pipeline newPipeline = new Pipeline();
        newPipeline.setShareType(ShareType.PRIVATE);
        if (authCtx != null) {
            newPipeline.setUser(authCtx.getUser());
            if (this.authCtx.getUser().getUserActor() != null) {
                newPipeline.setActor(this.authCtx.getUser().getUserActor());
            }
        }
        return newPipeline;
    }

    /**
     * Creates a clone of given pipeline, persists it, and returns it as a new
     * instance. Original owner is not preserved, rather currently logged in
     * user is set as an owner of the newly created pipeline.
     * 
     * @param pipeline
     *            original pipeline to copy
     * @return newly copied pipeline
     */
    @Transactional
    @PreAuthorize("hasPermission(#pipeline, 'pipeline.copy') and hasRole('pipeline.create')")
    @Override
    public Pipeline copyPipeline(Pipeline pipeline) {

        Pipeline newPipeline = new Pipeline(pipeline);
        // determine new name for pipeline
        String oName = pipeline.getName();
        String nName;
        int no = 1;
        do {
            nName = Messages.getString("PipelineFacadeImpl.pipeline.copy", no++, oName);
        } while (hasPipelineWithName(nName, null));

        newPipeline.setName(StringUtils.abbreviate(nName, LenghtLimits.PIPELINE_NAME));
        newPipeline.setShareType(ShareType.PRIVATE);

        if (authCtx != null) {
            newPipeline.setUser(authCtx.getUser());
            if (this.authCtx.getUser().getUserActor() != null) {
                newPipeline.setActor(this.authCtx.getUser().getUserActor());
            }
        }

        save(newPipeline);
        return newPipeline;
    }

    /**
     * Returns list of all pipelines persisted in the database.
     *
     * @return list of pipelines
     * @deprecated performance intensive for many pipelines in DB, use lazy
     *             container with paging instead
     */
    @Deprecated
    @PostFilter("hasPermission(filterObject,'pipeline.read')")
    @Override
    public List<Pipeline> getAllPipelines() {
        return pipelineDao.getAll();
    }

    /**
     * Find pipeline in database by ID and return it.
     * 
     * @param id
     *            of Pipeline
     * @return Pipeline the found pipeline or null if the pipeline with given ID
     *         does not exist
     */
    @PostAuthorize("hasPermission(returnObject,'pipeline.read')")
    @Override
    public Pipeline getPipeline(long id) {
        return pipelineDao.getInstance(id);
    }

    /**
     * Saves any modifications made to the pipeline into the database.
     * 
     * @param pipeline
     */
    @Transactional
    // Public writable pipeline can be saved only if user has proper permission for it 
    @PreAuthorize("hasPermission(#pipeline,'pipeline.edit') "
            + "AND (#pipeline.getShareType() != T(cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType).PUBLIC_RW "
            + "OR (#pipeline.getShareType() == T(cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType).PUBLIC_RW "
            + "AND hasRole('pipeline.setVisibilityPublicRw')))")
    @Override
    public void save(Pipeline pipeline) {
        // If pipeline is public, we need to make sure
        // all DPU templates used in this pipeline are
        // public as well.
        if (ShareType.PUBLIC.contains(pipeline.getShareType())) {
            for (DPUTemplateRecord dpu : getPrivateDPUs(pipeline)) {
                if (ShareType.PRIVATE.equals(dpu.getShareType())) {
                    // we found a private DPU in public pipeline -> make public
                    dpu.setShareType(ShareType.PUBLIC_RO);
                    dpuFacade.save(dpu);
                }
            }
        }
        pipeline.setLastChange(new Date());
        pipelineDao.save(pipeline);
    }

    /**
     * Deletes pipeline from database.
     * 
     * @param pipeline
     */
    @Transactional
    @PreAuthorize("hasPermission(#pipeline, 'pipeline.delete')")
    @Override
    public void delete(Pipeline pipeline) {
        pipelineDao.delete(pipeline);
    }

    @PreAuthorize("hasPermission(#dpu, 'dpuTemplate.read')")
    @PostFilter("hasPermission(filterObject,'pipeline.read')")
    @Override
    public List<Pipeline> getPipelinesUsingDPU(DPUTemplateRecord dpu) {
        return pipelineDao.getPipelinesUsingDPU(dpu);
    }

    @PreAuthorize("hasPermission(#dpu, 'dpuTemplate.read')")
    @Override
    public List<Pipeline> getAllPipelinesUsingDPU(DPUTemplateRecord dpu) {
        return pipelineDao.getPipelinesUsingDPU(dpu);
    }

    /**
     * Checks for duplicate pipeline names. The name of pipeline in second
     * argument is ignored, if given. It is to be used when editing already
     * existing pipeline.
     * 
     * @param newName
     * @param pipeline
     *            to be renamed, or null
     * @return
     */
    @Override
    public boolean hasPipelineWithName(String newName, Pipeline pipeline) {
        Pipeline duplicate = pipelineDao.getPipelineByName(newName);
        return !(duplicate == null || duplicate.equals(pipeline));
    }

    /**
     * Lists all private DPU templates which are used in given pipeline.
     * 
     * @param pipeline
     *            to inspect for private DPUs
     * @return list of private DPUs used in pipeline
     */
    @Override
    @PreAuthorize("hasPermission(#pipeline, 'pipeline.read')")
    public List<DPUTemplateRecord> getPrivateDPUs(Pipeline pipeline) {
        List<DPUTemplateRecord> dpus = new ArrayList<>();
        for (Node node : pipeline.getGraph().getNodes()) {
            DPUTemplateRecord dpu = node.getDpuInstance().getTemplate();
            if (ShareType.PRIVATE.equals(dpu.getShareType())) {
                dpus.add(dpu);
            }
        }
        return dpus;
    }

    /**
     * Creates an open pipeline event with current timestamp. User is taken from
     * authentication context (currently logged in user).
     * 
     * @param pipeline
     *            which is open
     */
    @Transactional
    @Override
    public void createOpenEvent(Pipeline pipeline) {
        // TODO Petr: UPDATE on openEvent takes too long ..
//        LOG.trace("createOpenEvent() ... ");
//        if (pipeline.getId() == null) {
//            // pipeline has not been persisted yet
//            // -> it cannot be opened by anyone
//            return;
//        }
//
//        User user = authCtx.getUser();
//        if (user == null) {
//            // user logged out in the meantime -> ignore
//            return;
//        }
//        LOG.trace("createOpenEvent() ... (getOpenEvent)");
//        OpenEvent event = openEventDao.getOpenEvent(pipeline, user);
//
//        if (event == null) {
//            event = new OpenEvent();
//            event.setPipeline(pipeline);
//            event.setUser(user);
//        }
//
//        LOG.trace("createOpenEvent() ... (saving)");
//        event.setTimestamp(new Date());
//        openEventDao.save(event);
//
//        LOG.trace("createOpenEvent() ... done");
    }

    /**
     * Lists all open events representing a list of pipeline that are currently
     * open in pipeline canvas. Events of currently logged in user are ignored
     * and not included in the resulting list.
     * 
     * @param pipeline
     * @return list of open events
     */
    @Override
    public List<OpenEvent> getOpenPipelineEvents(Pipeline pipeline) {
        // Return empry list. Ie. no pipeline is open.
        return Arrays.asList();

        // TODO Petr: UPDATE on openEvent takes too long ..
//        LOG.trace("getOpenPipelineEvents({}) ... ", pipeline.getId());
//        if (pipeline.getId() == null) {
//            // pipeline has not been persisted yet
//            // -> it cannot be opened by anyone else
//            return new ArrayList<>();
//        }
//
//        Date from = new Date((new Date()).getTime() - PPL_OPEN_TTL * 1000);
//
//        if (authCtx != null) {
//            User loggedUser = authCtx.getUser();
//            List<OpenEvent> res = openEventDao.getOpenEvents(pipeline, from, loggedUser);
//            LOG.trace("getOpenPipelineEvents({}) ... done", pipeline.getId());
//            return res;
//        } else {
//            // user is null
//            List<OpenEvent> res = openEventDao.getOpenEvents(pipeline, from);
//            LOG.trace("getOpenPipelineEvents({}) ... done", pipeline.getId());
//            return res;
//        }
    }

    /**
     * Checks if (possibly detached) pipeline has been modified by someone else.
     * 
     * @param pipeline
     *            to check
     * @return true if pipeline was changed while detached from entity manager,
     *         false otherwise
     */
    @Override
    @PreAuthorize("hasPermission(#pipeline, 'pipeline.read')")
    public boolean isUpToDate(Pipeline pipeline) {
        LOG.trace("isUpToDate({})", pipeline.getId());
        if (pipeline.getId() == null) {
            // new pipeline -> lets say it is up-to-date
            return true;
        }

        // fetch fresh pipeline from db
        Pipeline dbPipeline = getPipeline(pipeline.getId());
        if (dbPipeline == null) {
            // someone probably deleted pipeline in the meantime
            // -> lets say it is NOT up-to-date
            return false;
        }

        Date lastChange = dbPipeline.getLastChange();
        Date myLastChange = pipeline.getLastChange();
        return lastChange == null ? true : myLastChange == null ? false : !lastChange.after(myLastChange);
    }

    /* ******************** Methods for managing PipelineExecutions ********* */
    /**
     * Creates a new {@link PipelineExecution}, which represents a pipeline run.
     * Created instance is not yet managed by {@link EntityManager}, thus needs
     * to be saved with {@link #save(PipelineExecution)} method.
     * 
     * @param pipeline
     * @return pipeline execution of given pipeline
     */
    @PreAuthorize("hasPermission(#pipeline, 'pipeline.run')")
    @Override
    public PipelineExecution createExecution(Pipeline pipeline) {
        PipelineExecution newExec = new PipelineExecution(pipeline);
        if (authCtx != null) {
            newExec.setOwner(authCtx.getUser());
            if (this.authCtx.getUser().getUserActor() != null) {
                newExec.setActor(this.authCtx.getUser().getUserActor());
            }
        }
        return newExec;
    }

    /**
     * Fetches all {@link PipelineExecution}s from database.
     * 
     * @return list of executions
     * @deprecated performance intensive for many pipeline executions, use
     *             container with paging support instead
     */
    @Deprecated
    @PreAuthorize("hasRole('pipelineExecution.read')")
    @Override
    public List<PipelineExecution> getAllExecutions() {
        return executionDao.getAll();
    }

    /**
     * Fetches all {@link PipelineExecution}s with given state from database.
     * 
     * @param status
     * @return list of executions
     */
    @Override
    @PreAuthorize("hasRole('pipelineExecution.read')")
    public List<PipelineExecution> getAllExecutions(PipelineExecutionStatus status) {
        return executionDao.getAll(status);
    }

    @PreAuthorize("hasRole('pipelineExecution.read')")
    @Override
    public List<PipelineExecution> getAllExecutions(PipelineExecutionStatus status, String backendID) {
        return this.executionDao.getAll(status, backendID);
    }

    @PreAuthorize("hasRole('pipelineExecution.read')")
    @Override
    public List<PipelineExecution> getAllExecutionsByPriorityLimited(PipelineExecutionStatus status) {
        return executionDao.getAllByPriorityLimited(status);
    }

    @PreAuthorize("hasRole('pipelineExecution.read')")
    @Override
    public List<PipelineExecution> getAllExecutionsByPriorityLimited(PipelineExecutionStatus status, String backendID) {
        return this.executionDao.getAllByPriorityLimited(status, backendID);
    }

    /**
     * Find pipeline execution in database by ID and return it.
     * 
     * @param id
     *            of PipelineExecution
     * @return PipelineExecution
     */
    @PostAuthorize("hasPermission(returnObject, 'pipelineExecution.read')")
    @Override
    public PipelineExecution getExecution(long id) {
        return executionDao.getInstance(id);
    }

    /**
     * Fetch all executions for given pipeline.
     * 
     * @param pipeline
     * @return pipeline executions
     */
    @Override
    @PreAuthorize("hasPermission(#pipeline, 'pipelineExecution.read')")
    public List<PipelineExecution> getExecutions(Pipeline pipeline) {
        return executionDao.getAll(pipeline);
    }

    /**
     * Fetch executions for given pipeline in given status.
     * 
     * @param pipeline
     *            Pipeline which executions should be fetched.
     * @param status
     *            Execution status, in which execution should be.
     * @return PipelineExecutions
     */
    @Override
    @PreAuthorize("hasPermission(#pipeline, 'pipelineExecution.read')")
    public List<PipelineExecution> getExecutions(Pipeline pipeline, PipelineExecutionStatus status) {
        return executionDao.getAll(pipeline, status);
    }

    /**
     * Return end time of latest execution of given status for given pipeline.
     * Ignore null values.
     * 
     * @param pipeline
     * @param status
     *            Execution status, used to filter pipelines.
     * @return
     */
    @Override
    public Date getLastExecTime(Pipeline pipeline, PipelineExecutionStatus status) {

        HashSet statuses = new HashSet(1);
        statuses.add(status);
        PipelineExecution exec = getLastExec(pipeline, statuses);

        return (exec == null) ? null : exec.getEnd();
    }

    /**
     * Return latest execution of given statuses for given pipeline. Ignore null
     * values.
     * 
     * @param pipeline
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    @Override
    @PreAuthorize("hasRole('pipelineExecution.read')")
    public PipelineExecution getLastExec(Pipeline pipeline,
            Set<PipelineExecutionStatus> statuses) {
        return executionDao.getLastExecution(pipeline, statuses);
    }

    /**
     * Return latest execution of given pipeline. Ignore null values.
     * 
     * @param pipeline
     * @return last execution or null
     */
    @Override
    @PreAuthorize("hasRole('pipelineExecution.read')")
    public PipelineExecution getLastExec(Pipeline pipeline) {
        return executionDao.getLastExecution(pipeline, EnumSet.allOf(PipelineExecutionStatus.class));
    }

    /**
     * Return latest execution of given statuses for given schedule. Ignore null
     * values.
     * 
     * @param schedule
     * @param statuses
     *            Set of execution statuses, used to filter pipelines.
     * @return last execution or null
     */
    @Override
    @PreAuthorize("hasRole('pipelineExecution.read')")
    public PipelineExecution getLastExec(Schedule schedule,
            Set<PipelineExecutionStatus> statuses) {
        return executionDao.getLastExecution(schedule, statuses);
    }

    /**
     * Tells whether there were any changes to pipeline executions since the
     * last load.
     * <p>
     * This method is provided purely for performance optimization of refreshing execution statuses. Functionality is backed by database trigger
     * &quot;update_last_change&quot;.
     * 
     * @param lastLoad
     * @return
     */
    @Override
    public boolean hasModifiedExecutions(Date lastLoad) {
        return executionDao.hasModified(lastLoad);
    }

    /**
     * Checks if some of the executions were deleted
     * <p>
     * 
     * @param executionIds
     *            execution to check
     * @return true if one or more execution were deleted
     */
    @Override
    public boolean hasDeletedExecutions(List<Long> executionIds) {
        return executionDao.hasDeleted(executionIds);
    }

    /**
     * Tells whether there were any changes to pipelines since the
     * last load.
     * <p>
     * 
     * @param lastLoad
     * @return
     */
    @Override
    public boolean hasModifiedPipelines(Date lastLoad) {
        return pipelineDao.hasModified(lastLoad);
    }

    /**
     * Tells whether one of pipelines was deleted
     * <p>
     * 
     * @param pipelineIds
     * @return true if one or more pipelines with provided ids were deleted, otherwise false
     */
    @Override
    public boolean hasDeletedPipelines(List<Long> pipelineIds) {
        return pipelineDao.hasDeletedPipelines(pipelineIds);
    }

    /**
     * Persists new {@link PipelineExecution} or updates it if it was already
     * persisted before.
     * 
     * @param exec
     */
    @Transactional
    @PreAuthorize("hasPermission(#exec, 'pipeline.run') "
            + "AND (!#exec.isDebugging() OR (#exec.isDebugging() AND hasRole('pipeline.runDebug')))")
    @Override
    public void save(PipelineExecution exec) {
        exec.setLastChange(new Date());
        executionDao.save(exec);
    }

    /**
     * Deletes pipeline from database.
     * 
     * @param exec
     */
    @Transactional
    @PreAuthorize("hasPermission(#exec, 'pipelineExecution.delete')")
    @Override
    public void delete(PipelineExecution exec) {
        executionDao.delete(exec);
    }

    /**
     * Stop the execution.
     * 
     * @param execution
     *            pipeline execution to stop
     */
    @Override
    @PreAuthorize("hasPermission(#execution, 'pipelineExecution.stop')")
    @Transactional
    public void stopExecution(PipelineExecution execution) {
        PipelineExecution currentExec = getExecution(execution.getId());

        if (currentExec.getStatus() == PipelineExecutionStatus.QUEUED) {
            // not started yet
            delete(execution);
        } else if (currentExec.getStatus() == PipelineExecutionStatus.RUNNING) {
            execution.stop();
            save(execution);
        } else {
            // we are not in running state anymore .. so we do not
            // save the pipeline
        }
    }

    /**
     * Setter for mocking authenticated users.
     * 
     * @param authCtx
     *            authentication context
     */
    void setAuthCtx(AuthenticationContext authCtx) {
        this.authCtx = authCtx;
    }

    @Override
    public boolean hasExecutionsWithStatus(Pipeline pipeline, List<PipelineExecutionStatus> statuses) {
        return executionDao.hasWithStatus(pipeline, statuses);
    }

    @PostFilter("hasPermission(filterObject,'pipeline.read')")
    @Override
    public List<Pipeline> getAllPipelines(String externalUserId) {
        return this.pipelineDao.getPipelinesForUser(externalUserId);
    }

    @PostFilter("hasPermission(filterObject,'pipeline.read')")
    @Override
    public List<Pipeline> getAllVisiblePipelines(String externalUserId) {
        return this.pipelineDao.getAllVisiblePipelines(externalUserId);
    }
}
