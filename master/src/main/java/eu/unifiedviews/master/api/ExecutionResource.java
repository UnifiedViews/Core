package eu.unifiedviews.master.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.cuni.mff.xrg.odcs.commons.app.user.Organization;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import eu.unifiedviews.master.converter.PipelineExecutionDTOConverter;
import eu.unifiedviews.master.converter.PipelineExecutionEventDTOConverter;
import eu.unifiedviews.master.model.ApiException;
import eu.unifiedviews.master.model.PipelineExecutionDTO;
import eu.unifiedviews.master.model.PipelineExecutionEventDTO;

@Component
@Path("/pipelines")
public class ExecutionResource {
    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private UserFacade userFacade;

    @GET
    @Path("/{pipelineid}/executions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineExecutionDTO> getPipelineExecutions(@PathParam("pipelineid") String id) {
        if (StringUtils.isBlank(id) || !StringUtils.isNumeric(id)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            Pipeline pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", id));
            }
            List<PipelineExecution> executions = pipelineFacade.getExecutions(pipeline);
            if (executions == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "PipelineFacade returned null!");
            }
            return PipelineExecutionDTOConverter.convert(executions);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GET
    @Path("/{pipelineid}/executions/last")
    @Produces({ MediaType.APPLICATION_JSON })
    public PipelineExecutionDTO getLastPipelineExecution(@PathParam("pipelineid") String pipelineId) {
        Pipeline pipeline = null;
        PipelineExecution execution = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            HashSet<PipelineExecutionStatus> set = new HashSet<>();
            set.add(PipelineExecutionStatus.CANCELLED);
            set.add(PipelineExecutionStatus.FINISHED_SUCCESS);
            set.add(PipelineExecutionStatus.FINISHED_WARNING);
            set.add(PipelineExecutionStatus.FAILED);
            execution = pipelineFacade.getLastExec(pipeline, set);
            if (execution == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline execution doesn't exist!"));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        if (execution.getPipeline().getId().equals(pipeline.getId())) {
            return PipelineExecutionDTOConverter.convert(execution);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("PipelineExecution with id=%s is not execution of pipeline with id=%s!", execution.getId(), pipelineId));
        }
    }

    @GET
    @Path("/{pipelineid}/executions/pending")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineExecutionDTO> getPendingPipelineExecution(@PathParam("pipelineid") String pipelineId) {
        Pipeline pipeline = null;
        List<PipelineExecution> executions = new ArrayList<>();
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            executions.addAll(pipelineFacade.getExecutions(pipeline, PipelineExecutionStatus.CANCELLING));
            executions.addAll(pipelineFacade.getExecutions(pipeline, PipelineExecutionStatus.RUNNING));
            executions.addAll(pipelineFacade.getExecutions(pipeline, PipelineExecutionStatus.QUEUED));
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        return PipelineExecutionDTOConverter.convert(executions);
    }

    @GET
    @Path("/{pipelineid}/executions/{executionid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public PipelineExecutionDTO getPipelineExecution(@PathParam("pipelineid") String pipelineId, @PathParam("executionid") String executionId) {
        Pipeline pipeline = null;
        PipelineExecution execution = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(executionId) || !StringUtils.isNumeric(executionId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline execution ID", executionId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            execution = pipelineFacade.getExecution(Long.parseLong(executionId));
            if (execution == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline execution with id=%s doesn't exist!", executionId));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        if (execution.getPipeline().getId().equals(pipeline.getId())) {
            return PipelineExecutionDTOConverter.convert(execution);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("PipelineExecution with id=%s is not execution of pipeline with id=%s!", executionId, pipelineId));
        }
    }

    @POST
    @Path("/{pipelineid}/executions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineExecutionDTO createPipelineExecution(@PathParam("pipelineid") String pipelineId, PipelineExecutionDTO newExecution) {
        PipelineExecution execution = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            // try to get pipeline
            Pipeline pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            // try to get user
            User user =  userFacade.getUserByExtId(newExecution.getUserExternalId());
            if(user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("User '%s' could not be found! Schedule could not be created.", newExecution.getUserExternalId()));
            }
            // try to get organization
            Organization organization = userFacade.getOrganizationByName(newExecution.getOrganizationExternalId());
            if(organization == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Organization '%s' could not be found! Schedule could not be created.", newExecution.getOrganizationExternalId()));
            }
            execution = pipelineFacade.createExecution(pipeline);
            execution.setOwner(user);
            execution.setOrganization(organization);
            execution.setDebugging(newExecution.isDebugging());
            execution.setOrderNumber(1L);
//            PipelineExecutionDTOConverter.createPipelineExecution(newExecution, pipeline);
            pipelineFacade.save(execution);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return PipelineExecutionDTOConverter.convert(execution);
    }

    @GET
    @Path("/{pipelineid}/executions/{executionid}/events")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineExecutionEventDTO> getPipelineExecutionEvents(@PathParam("pipelineid") String pipelineId, @PathParam("executionid") String executionId) {
        Pipeline pipeline = null;
        PipelineExecution execution = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(executionId) || !StringUtils.isNumeric(executionId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline execution ID", executionId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            execution = pipelineFacade.getExecution(Long.parseLong(executionId));
            if (execution == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "PipelineFacade returned null!");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        if (execution.getPipeline().getId().equals(pipeline.getId())) {
            List<MessageRecord> events = dpuFacade.getAllDPURecords(execution);
            return PipelineExecutionEventDTOConverter.convert(events);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("PipelineExecution with id=%s is not execution of pipeline with id=%s!", executionId, pipelineId));
        }
    }
}
