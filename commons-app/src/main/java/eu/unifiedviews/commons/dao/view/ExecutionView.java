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
package eu.unifiedviews.commons.dao.view;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

import java.util.Date;

/**
 * @author Škoda Petr
 */
public class ExecutionView implements DataObject {

    private Long id;
    private PipelineExecutionStatus status;

    //pipeline_id
    private Long pipelineId;

    //pipeline_name
    private String pipelineName;

    //debug_mode
    private boolean isDebugging;

    //t_start
    private Date start;

    //t_end
    private Date end;

    //schedule_id
    private Long scheduleId;

    //owner_name
    private String ownerName;

    //owner_full_name
    private String ownerFullName;

    //user_actor_name
    private String userActorName;

    //stop
    private boolean stop;

    //t_last_change
    private Date lastChange;

    public ExecutionView(Long id,
                         PipelineExecutionStatus status,
                         Long pipelineId,
                         String pipelineName,
                         boolean isDebugging,
                         Date start,
                         Date end,
                         Long scheduleId,
                         String ownerName,
                         String ownerFullName,
                         String userActorName,
                         boolean stop,
                         Date lastChange)
    {
        this.id = id;
        this.status = status;
        this.pipelineId = pipelineId;
        this.pipelineName = pipelineName;
        this.isDebugging = isDebugging;
        this.start = start;
        this.end = end;
        this.scheduleId = scheduleId;
        this.ownerName = ownerName;
        this.ownerFullName = ownerFullName;
        this.userActorName = userActorName;
        this.stop = stop;
        this.lastChange = lastChange;
    }

    private String backendId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PipelineExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineExecutionStatus status) {
        this.status = status;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public boolean isDebugging() {
        return isDebugging;
    }

    public void setDebugging(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleOd(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

    public String getUserActorName() {
        return this.userActorName;
    }

    public void setUserActorName(String userActorName) {
        this.userActorName = userActorName;
    }

    public String getOwnerFullName() {
        return this.ownerFullName;
    }

    public void setOwnerFullName(String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    public String getBackendId() {
        return this.backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    /**
     * @return Duration of last pipeline execution, -1 if no such execution exists.
     */
    public long getDuration() {
        if (start == null || end == null) {
            return -1l;
        } else {
            return end.getTime() - start.getTime();
        }
    }

}
