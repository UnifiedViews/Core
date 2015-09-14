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
package eu.unifiedviews.master.model;

import java.util.Map;

public class ScheduledExecutionDTO {

    private String start;

    private Long schedule;
    
    private Map<Long, String> afterPipelines;

    public ScheduledExecutionDTO(String start, Long schedule) {
        super();
        this.start = start;
        this.schedule = schedule;
    }
    
    public ScheduledExecutionDTO(Map<Long, String> afterPipelines, Long schedule) {
        super();
        this.afterPipelines = afterPipelines;
        this.schedule = schedule;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Long getSchedule() {
        return schedule;
    }

    public void setSchedule(Long schedule) {
        this.schedule = schedule;
    }

    public Map<Long, String> getAfterPipelines() {
        return afterPipelines;
    }

    public void setAfterPipelines(Map<Long, String> afterPipelines) {
        this.afterPipelines = afterPipelines;
    }
}
