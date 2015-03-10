package eu.unifiedviews.commons.dao.view;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

/**
 * View for pipelines.
 *
 * @author Škoda Petr
 */
@XmlRootElement
@Entity()
@Table(name = "pipeline_view")
public class PipelineView implements Serializable, DataObject {

    @Id
    private Long id;

    /**
     * Human-readable pipeline name
     */
    @Column
    private String name;

    /**
     * Start of last pipeline execution.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "t_start")
    private Date start;

    /**
     * End of last pipeline execution.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "t_end")
    private Date end;

    @Column(name = "usr_name")
    private String usrName;

    @Column(name = "org_name")
    private String orgName;

    /**
     * Status of last pipeline execution.
     */
    @Enumerated(EnumType.ORDINAL)
    private PipelineExecutionStatus status;

    /**
     * Public vs private shareType.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "visibility")
    private ShareType shareType;

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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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
