package eu.unifiedviews.master.model;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

public class PipelineExecutionDTO {
    private Long id;

    private PipelineExecutionStatus status;

    private boolean isDebugging;

    private Long orderNumber;

    private String start;

    private String end;

    private Long schedule;

    private boolean stop;

    private String lastChange;

    private String userExternalId;

    private String organizationExternalId;

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

    public boolean isDebugging() {
        return isDebugging;
    }

    public void setDebugging(boolean isDebugging) {
        this.isDebugging = isDebugging;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Long getSchedule() {
        return schedule;
    }

    public void setSchedule(Long schedule) {
        this.schedule = schedule;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }

    public String getUserExternalId() {
        return userExternalId;
    }

    public void setUserExternalId(String userExternalId) {
        this.userExternalId = userExternalId;
    }

    public String getOrganizationExternalId() {
        return organizationExternalId;
    }

    public void setOrganizationExternalId(String organizationExternalId) {
        this.organizationExternalId = organizationExternalId;
    }

    @Override public String toString() {
        return "PipelineExecutionDTO{" +
                "id=" + id +
                ", status=" + status +
                ", isDebugging=" + isDebugging +
                ", orderNumber=" + orderNumber +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", schedule=" + schedule +
                ", stop=" + stop +
                ", lastChange='" + lastChange + '\'' +
                ", userExternalId=" + userExternalId +
                ", organizationExternalId='" + organizationExternalId + '\'' +
                '}';
    }
}
