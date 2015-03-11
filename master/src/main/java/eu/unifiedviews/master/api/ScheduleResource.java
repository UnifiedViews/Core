package eu.unifiedviews.master.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.cuni.mff.xrg.odcs.commons.app.user.Organization;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import eu.unifiedviews.master.converter.ScheduleDTOConverter;
import eu.unifiedviews.master.converter.ScheduledExecutionDTOConverter;
import eu.unifiedviews.master.model.ApiException;
import eu.unifiedviews.master.model.PipelineScheduleDTO;
import eu.unifiedviews.master.model.ScheduledExecutionDTO;

@Component
@Path("/pipelines")
@AuthenticationRequired
public class ScheduleResource {
    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private UserFacade userFacade;

    @GET
    @Path("/{pipelineid}/schedules")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<PipelineScheduleDTO> getPipelineSchedules(@PathParam("pipelineid") String id) {
        Pipeline pipeline = null;
        List<Schedule> schedules = null;
        if (StringUtils.isBlank(id) || !StringUtils.isNumeric(id)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", id));
            }
            schedules = scheduleFacade.getSchedulesFor(pipeline);
            if (schedules == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "ScheduleFacade returned null!");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        return ScheduleDTOConverter.convertToDTOs(schedules);
    }

    @GET
    @Path("/{pipelineid}/schedules/{scheduleid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public PipelineScheduleDTO getPipelineSchedule(@PathParam("pipelineid") String pipelineId, @PathParam("scheduleid") String scheduleId) {
        Pipeline pipeline = null;
        Schedule schedule = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid schedule ID", scheduleId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline schedule with id=%s doesn't exist!", scheduleId));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        if (schedule.getPipeline().getId().equals(pipeline.getId())) {
            return ScheduleDTOConverter.convertToDTO(schedule);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("Schedule with id=%s is not for pipeline with id=%s!", scheduleId, pipelineId));
        }
    }

    @GET
    @Path("/{pipelineid}/schedules/{scheduleid: [1-9][0-9]*}/scheduledexecutions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ScheduledExecutionDTO> getScheduleScheduledExecutions(@PathParam("pipelineid") String pipelineId, @PathParam("scheduleid") String scheduleId) {
        Pipeline pipeline = null;
        Schedule schedule = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid schedule ID", scheduleId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline schedule with id=%s doesn't exist!", scheduleId));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        if (schedule.getPipeline().getId().equals(pipeline.getId())) {
            return ScheduledExecutionDTOConverter.convert(Arrays.asList(schedule));
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, String.format("Schedule with id=%s is not for pipeline with id=%s!", scheduleId, pipelineId));
        }
    }

    @GET
    @Path("/{pipelineid}/schedules/~all/scheduledexecutions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ScheduledExecutionDTO> getAllScheduleScheduledExecutions(@PathParam("pipelineid") String pipelineId) {
        Pipeline pipeline = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            List<Schedule> schedules = scheduleFacade.getSchedulesFor(pipeline);
            Collections.sort(schedules, new Comparator<Schedule>() {

                @Override
                public int compare(Schedule o1, Schedule o2) {
                    Date o1time = o1.getNextExecutionTimeInfo();
                    Date o2time = o2.getNextExecutionTimeInfo();
                    if ((o1time == null) && (o2time == null)) {
                        return 0;
                    }
                    if ((o1time == null) && (o2time != null)) {
                        return 1;
                    }
                    if ((o1time != null) && (o2time == null)) {
                        return -1;
                    }
                    return o1time.compareTo(o2time);
                }
            });
            return ScheduledExecutionDTOConverter.convert(schedules);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PUT
    @Path("/{pipelineid}/schedules/{scheduleid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineScheduleDTO updatePipelineSchedule(@PathParam("pipelineid") String pipelineId, @PathParam("scheduleid") String scheduleId, PipelineScheduleDTO scheduleToUpdate) {
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.NOT_FOUND, String.format("ID=%s is not valid schedule ID", scheduleId));
        }
        try {
            Pipeline pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            Schedule schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "ScheduleFacade returned null!");
            }
            if (schedule.getPipeline().getId().equals(pipeline.getId())) {
                List<Pipeline> afterPipelines = null;
                if (scheduleToUpdate.getAfterPipelines() != null) {
                    afterPipelines = new ArrayList<Pipeline>();
                    for (Long pipId : scheduleToUpdate.getAfterPipelines()) {
                        Pipeline loadedPip = pipelineFacade.getPipeline(pipId.longValue());
                        if (loadedPip != null) {
                            afterPipelines.add(loadedPip);
                        } else {
                            throw new ApiException(Response.Status.BAD_REQUEST, String.format("Pipeline with id=%d doesn't exist!", pipId));
                        }
                    }
                }
                ScheduleDTOConverter.convertFromDTO(scheduleToUpdate, afterPipelines, schedule);
                scheduleFacade.save(schedule);
                return ScheduleDTOConverter.convertToDTO(schedule);
            } else {
                throw new ApiException(Response.Status.BAD_REQUEST, String.format("Schedule with id=%s is not for pipeline with id=%s!", scheduleId, pipelineId));
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @POST
    @Path("/{pipelineid}/schedules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineScheduleDTO createPipelineSchedule(@PathParam("pipelineid") String pipelineId, PipelineScheduleDTO scheduleToUpdate) {
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
            User user =  userFacade.getUserByExtId(scheduleToUpdate.getUserExternalId());
            if(user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("User '%s' could not be found! Schedule could not be created.", scheduleToUpdate.getUserExternalId()));
            }
            // try to get organization
            Organization organization = userFacade.getOrganizationByName(scheduleToUpdate.getOrganizationExternalId());
            if(organization == null) {
                throw new ApiException(Response.Status.NOT_FOUND, String.format("Organization '%s' could not be found! Schedule could not be created.", scheduleToUpdate.getOrganizationExternalId()));
            }
            Schedule schedule = scheduleFacade.createSchedule();
            if (schedule == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "ScheduleFacade returned null!");
            }
            schedule.setPipeline(pipeline);
            schedule.setType(scheduleToUpdate.getScheduleType());
            schedule.setOwner(user);
            schedule.setOrganization(organization);
            List<Pipeline> afterPipelines = null;
            if (scheduleToUpdate.getAfterPipelines() != null) {
                afterPipelines = new ArrayList<Pipeline>();
                for (Long pipId : scheduleToUpdate.getAfterPipelines()) {
                    Pipeline loadedPip = pipelineFacade.getPipeline(pipId.longValue());
                    if (loadedPip != null) {
                        afterPipelines.add(loadedPip);
                    } else {
                        throw new ApiException(Response.Status.BAD_REQUEST, String.format("Pipeline with id=%d doesn't exist!", pipId));
                    }
                }
            }
            schedule.setPriority(scheduleToUpdate.getScheduledJobsPriority());
            ScheduleDTOConverter.convertFromDTO(scheduleToUpdate, afterPipelines, schedule);
            scheduleFacade.save(schedule);
            return ScheduleDTOConverter.convertToDTO(schedule);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException exception) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
