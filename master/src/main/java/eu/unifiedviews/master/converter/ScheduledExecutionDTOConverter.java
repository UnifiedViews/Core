package eu.unifiedviews.master.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleType;
import eu.unifiedviews.master.model.ScheduledExecutionDTO;

public class ScheduledExecutionDTOConverter {

    public static ScheduledExecutionDTO convert(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        Date nextStart = schedule.getNextExecutionTimeInfo();
        if (nextStart != null) {
            return new ScheduledExecutionDTO(ConvertUtils.dateToString(nextStart), schedule.getId());
        } else if (schedule.getType() == ScheduleType.AFTER_PIPELINE
                && schedule.getAfterPipelines() != null
                && !schedule.getAfterPipelines().isEmpty()) {
            
            Map<Long, String> pipelineIds = new HashMap<Long, String>();
            for (Pipeline pipeline : schedule.getAfterPipelines()) {
                pipelineIds.put(pipeline.getId(), pipeline.getName());
            }
            return new ScheduledExecutionDTO(pipelineIds, schedule.getId());
        }
        return null;
    }

    public static List<ScheduledExecutionDTO> convert(List<Schedule> schedules) {
        List<ScheduledExecutionDTO> result = new ArrayList<>();
        if (schedules != null) {
            for (Schedule schedule : schedules) {
                ScheduledExecutionDTO dto = convert(schedule);
                if (dto != null) {
                    result.add(dto);
                }
            }
        }
        return result;
    }
}
