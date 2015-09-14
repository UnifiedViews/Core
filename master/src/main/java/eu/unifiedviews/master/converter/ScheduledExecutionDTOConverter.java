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
