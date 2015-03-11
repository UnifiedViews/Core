package eu.unifiedviews.master.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.PeriodUnit;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import eu.unifiedviews.master.model.PipelineScheduleDTO;

public class ScheduleDTOConverter {
    public static PipelineScheduleDTO convertToDTO(Schedule schedule) {
        PipelineScheduleDTO dto = null;
        if (schedule != null) {
            dto = new PipelineScheduleDTO();
            dto.setId(schedule.getId());
            dto.setDescription(schedule.getDescription());
            dto.setEnabled(schedule.isEnabled());
            dto.setFirstExecution(ConvertUtils.dateToString(schedule.getFirstExecution()));
            dto.setJustOnce(schedule.isJustOnce());
            dto.setLastExecution(ConvertUtils.dateToString(schedule.getLastExecution()));
            Date nextExecution = schedule.getNextExecutionTimeInfo();
            dto.setNextExecution(ConvertUtils.dateToString(nextExecution));
            dto.setPeriod(schedule.getPeriod());
            dto.setScheduledJobsPriority(schedule.getPriority());
            if (schedule.getPeriodUnit() != null) {
                dto.setPeriodUnit(schedule.getPeriodUnit().toString());
            } else {
                dto.setPeriodUnit(null);
            }
            if(schedule.getOwner() != null) {
                dto.setUserExternalId(schedule.getOwner().getExternalIdentifier());
            } else {
                dto.setUserExternalId(null);
            }
            if(schedule.getOrganization() != null) {
                dto.setOrganizationExternalId(schedule.getOrganization().getName());
            } else {
                dto.setOrganizationExternalId(null);
            }
            dto.setScheduleType(schedule.getType());
            Set<Pipeline> pipelines = schedule.getAfterPipelines();
            List<Long> pipelineIds = null;
            if (pipelines != null && pipelines.size() > 0) {
                pipelineIds = new ArrayList<Long>();
                for (Pipeline pipeline : pipelines) {
                    pipelineIds.add(pipeline.getId());
                }
            }
            dto.setAfterPipelines(pipelineIds);
        }
        return dto;
    }

    public static List<PipelineScheduleDTO> convertToDTOs(List<Schedule> schedules) {
        List<PipelineScheduleDTO> dtos = null;
        if (schedules != null) {
            dtos = new ArrayList<PipelineScheduleDTO>();
            for (Schedule schedule : schedules) {
                PipelineScheduleDTO dto = convertToDTO(schedule);
                if (dto != null) {
                    dtos.add(dto);
                }

            }
        }
        return dtos;
    }

    public static Schedule convertFromDTO(PipelineScheduleDTO dto, List<Pipeline> pipelines, Schedule schedule) {
        schedule.setDescription(dto.getDescription());
        schedule.setJustOnce(dto.isJustOnce());
        schedule.setEnabled(dto.isEnabled());
        schedule.setFirstExecution(ConvertUtils.stringToDate(dto.getFirstExecution()));
        schedule.setLastExecution(ConvertUtils.stringToDate(dto.getLastExecution()));
        Set<Pipeline> originalSet = schedule.getAfterPipelines();
        originalSet.clear();
        if (pipelines != null) {
            originalSet.addAll(pipelines);
            schedule.setAfterPipelines(originalSet);
        } else {
            schedule.setAfterPipelines(null);
        }

        schedule.setPeriod(dto.getPeriod());
        if (dto.getPeriodUnit() != null) {
            schedule.setPeriodUnit(PeriodUnit.valueOf(dto.getPeriodUnit()));
        } else {
            schedule.setPeriodUnit(null);
        }
        return schedule;
    }
}
