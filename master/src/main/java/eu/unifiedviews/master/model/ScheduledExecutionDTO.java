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
