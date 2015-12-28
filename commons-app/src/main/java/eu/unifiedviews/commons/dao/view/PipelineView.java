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

import java.io.Serializable;
import java.util.Date;

/**
 * View for pipelines.
 *
 * @author Škoda Petr
 */
public class PipelineView implements Serializable, DataObject {

    private Long id;
    private String name;

    //t_start
    private Date start;

    //t_end
    private Date end;

    //usr_name
    private String usrName;

    //usr_full_name
    private String usrFullName;

    private PipelineExecutionStatus status;

    //user_actor_name
    private String userActorName;

    public PipelineView(Long id,
                        String name,
                        Date start,
                        Date end,
                        String usrName,
                        String usrFullName,
                        PipelineExecutionStatus status,
                        String userActorName)
    {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.usrName = usrName;
        this.usrFullName = usrFullName;
        this.status = status;
        this.userActorName = userActorName;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public PipelineExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineExecutionStatus status) {
        this.status = status;
    }

    public String getUsrName() {
        return usrName;
    }

    public void setUsrName(String usrName) {
        this.usrName = usrName;
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

    public String getUserActorName() {
        return this.userActorName;
    }

    public void setUserActorName(String userActorName) {
        this.userActorName = userActorName;
    }

    public String getUsrFullName() {
        return this.usrFullName;
    }

    public void setUsrFullName(String usrFullName) {
        this.usrFullName = usrFullName;
    }

}
