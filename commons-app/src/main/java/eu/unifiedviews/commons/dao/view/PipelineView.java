package eu.unifiedviews.commons.dao.view;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAuthorizator;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * View for pipelines. It is an object which is displayed by the PipelineListView in frontend.
 * We do not directly use Pipeline object, as it is necessary to get also certain data about last execution runs from PipelineExecution object.
 * It is in commons-app, because it is needed by {@link DbAuthorizator}
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
