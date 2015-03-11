package eu.unifiedviews.master.model;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.ScheduledJobsPriority;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleType;

public class PipelineScheduleDTO {
    private Long id;

    private String description;

    private boolean enabled;

    private ScheduleType scheduleType;

    private String firstExecution;

    private String lastExecution;

    private String nextExecution;

    private List<Long> afterPipelines;

    private boolean justOnce;

    private Integer period;

    private String periodUnit;

    private Long scheduledJobsPriority;

    private String userExternalId;

    private String organizationExternalId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getFirstExecution() {
        return firstExecution;
    }

    public void setFirstExecution(String firstExecution) {
        this.firstExecution = firstExecution;
    }

    public String getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(String lastExecution) {
        this.lastExecution = lastExecution;
    }

    public String getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(String nextExecution) {
        this.nextExecution = nextExecution;
    }

    public List<Long> getAfterPipelines() {
        return afterPipelines;
    }

    public void setAfterPipelines(List<Long> afterPipelines) {
        this.afterPipelines = afterPipelines;
    }

    public boolean isJustOnce() {
        return justOnce;
    }

    public void setJustOnce(boolean justOnce) {
        this.justOnce = justOnce;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getPeriodUnit() {
        return periodUnit;
    }

    public void setPeriodUnit(String periodUnit) {
        this.periodUnit = periodUnit;
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

    public Long getScheduledJobsPriority() {
        return scheduledJobsPriority;
    }

    public void setScheduledJobsPriority(Long scheduledJobsPriority) {
        this.scheduledJobsPriority = scheduledJobsPriority;
    }

    @Override public String toString() {
        return "PipelineScheduleDTO{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", scheduleType=" + scheduleType +
                ", firstExecution='" + firstExecution + '\'' +
                ", lastExecution='" + lastExecution + '\'' +
                ", nextExecution='" + nextExecution + '\'' +
                ", afterPipelines=" + afterPipelines +
                ", justOnce=" + justOnce +
                ", period=" + period +
                ", periodUnit='" + periodUnit + '\'' +
                ", scheduledJobsPriority=" + scheduledJobsPriority +
                ", userExternalId='" + userExternalId + '\'' +
                ", organizationExternalId='" + organizationExternalId + '\'' +
                '}';
    }
}
