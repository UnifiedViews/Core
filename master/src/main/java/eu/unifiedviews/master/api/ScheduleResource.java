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
package eu.unifiedviews.master.api;

import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import eu.unifiedviews.master.converter.ScheduleDTOConverter;
import eu.unifiedviews.master.converter.ScheduledExecutionDTOConverter;
import eu.unifiedviews.master.i18n.Messages;
import eu.unifiedviews.master.model.ApiException;
import eu.unifiedviews.master.model.PipelineScheduleDTO;
import eu.unifiedviews.master.model.ScheduledExecutionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", id), String.format("ID=%s is not valid pipeline ID", id));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(id));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", id), String.format("Pipeline with id=%s doesn't exist!", id));
            }
            schedules = scheduleFacade.getSchedulesFor(pipeline);
            if (schedules == null || schedules.isEmpty()) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.get.schedule.general.error"), String.format("null pipeline schedule returned. There is probably a problem with database."));
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.schedule.general.error"), e.getMessage());
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
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", pipelineId), String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.id.invalid", scheduleId), String.format("ID=%s is not valid pipeline schedule ID", scheduleId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.schedule.id.not.found", scheduleId), String.format("Pipeline schedule with id=%s doesn't exist!", scheduleId));
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.schedule.general.error"), e.getMessage());
        }
        if (schedule.getPipeline().getId().equals(pipeline.getId())) {
            return ScheduleDTOConverter.convertToDTO(schedule);
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.mismatch", scheduleId, pipelineId),String.format("Schedule with id=%s is not schedule of pipeline with id=%s!", scheduleId, pipelineId));
        }
    }

    @GET
    @Path("/{pipelineid}/schedules/{scheduleid: [1-9][0-9]*}/scheduledexecutions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ScheduledExecutionDTO> getScheduleScheduledExecutions(@PathParam("pipelineid") String pipelineId, @PathParam("scheduleid") String scheduleId) {
        Pipeline pipeline = null;
        Schedule schedule = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", pipelineId), String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.id.invalid", scheduleId), String.format("ID=%s is not valid pipeline schedule ID", scheduleId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.schedule.id.not.found", scheduleId), String.format("Pipeline schedule with id=%s doesn't exist!", scheduleId));
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.schedule.general.error"), e.getMessage());
        }
        if (schedule.getPipeline().getId().equals(pipeline.getId())) {
            return ScheduledExecutionDTOConverter.convert(Arrays.asList(schedule));
        } else {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.mismatch", scheduleId, pipelineId),String.format("Schedule with id=%s is not schedule of pipeline with id=%s!", scheduleId, pipelineId));
        }
    }

    @GET
    @Path("/{pipelineid}/schedules/~all/scheduledexecutions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ScheduledExecutionDTO> getAllScheduleScheduledExecutions(@PathParam("pipelineid") String pipelineId) {
        Pipeline pipeline = null;
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", pipelineId), String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
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
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.get.schedule.general.error"), e.getMessage());
        }
    }

    @PUT
    @Path("/{pipelineid}/schedules/{scheduleid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineScheduleDTO updatePipelineSchedule(@PathParam("pipelineid") String pipelineId, @PathParam("scheduleid") String scheduleId, PipelineScheduleDTO scheduleToUpdate) {
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", pipelineId), String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        if (StringUtils.isBlank(scheduleId) || !StringUtils.isNumeric(scheduleId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.id.invalid", scheduleId), String.format("ID=%s is not valid pipeline schedule ID", scheduleId));
        }
        try {
            Pipeline pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            Schedule schedule = scheduleFacade.getSchedule(Long.parseLong(scheduleId));
            if (schedule == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.schedule.id.not.found", scheduleId), String.format("Pipeline schedule with id=%s doesn't exist!", scheduleId));
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
                            throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
                        }
                    }
                }
                ScheduleDTOConverter.convertFromDTO(scheduleToUpdate, afterPipelines, schedule);
                scheduleFacade.save(schedule);
                return ScheduleDTOConverter.convertToDTO(schedule);
            } else {
                throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.schedule.mismatch", scheduleId, pipelineId),String.format("Schedule with id=%s is not schedule of pipeline with id=%s!", scheduleId, pipelineId));
            }
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.update.schedule.general.error"), e.getMessage());
        }
    }

    @POST
    @Path("/{pipelineid}/schedules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PipelineScheduleDTO createPipelineSchedule(@PathParam("pipelineid") String pipelineId, PipelineScheduleDTO scheduleToUpdate) {
        if (StringUtils.isBlank(pipelineId) || !StringUtils.isNumeric(pipelineId)) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("pipeline.id.invalid", pipelineId), String.format("ID=%s is not valid pipeline ID", pipelineId));
        }
        try {
            // try to get pipeline
            Pipeline pipeline = pipelineFacade.getPipeline(Long.parseLong(pipelineId));
            if (pipeline == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
            }
            // try to get user
            User user = userFacade.getUserByExtId(scheduleToUpdate.getUserExternalId());
            if (user == null) {
                throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("schedule.user.id.not.found"), String.format("User '%s' could not be found! Pipeline could not be created.", scheduleToUpdate.getUserExternalId()));
            }

            UserActor actor = this.userFacade.getUserActorByExternalId(scheduleToUpdate.getUserActorExternalId());

            Schedule schedule = scheduleFacade.createSchedule();
            if (schedule == null) {
                throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.create.schedule.general.error"), "ScheduleFacade returned null, after creation of schedule");
            }
            schedule.setPipeline(pipeline);
            schedule.setType(scheduleToUpdate.getScheduleType());
            schedule.setOwner(user);
            if (actor != null) {
                schedule.setActor(actor);
            }
            List<Pipeline> afterPipelines = null;
            if (scheduleToUpdate.getAfterPipelines() != null) {
                afterPipelines = new ArrayList<Pipeline>();
                for (Long pipId : scheduleToUpdate.getAfterPipelines()) {
                    Pipeline loadedPip = pipelineFacade.getPipeline(pipId);
                    if (loadedPip != null) {
                        afterPipelines.add(loadedPip);
                    } else {
                        throw new ApiException(Response.Status.NOT_FOUND, Messages.getString("pipeline.id.not.found", pipelineId), String.format("Pipeline with id=%s doesn't exist!", pipelineId));
                    }
                }
            }
            schedule.setPriority(scheduleToUpdate.getScheduledJobsPriority());
            ScheduleDTOConverter.convertFromDTO(scheduleToUpdate, afterPipelines, schedule);
            scheduleFacade.save(schedule);
            return ScheduleDTOConverter.convertToDTO(schedule);
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR, Messages.getString("pipeline.create.schedule.general.error"), e.getMessage());
        }
    }
}
