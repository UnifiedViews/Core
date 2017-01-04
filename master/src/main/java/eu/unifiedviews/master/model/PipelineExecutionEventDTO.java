package eu.unifiedviews.master.model;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;

public class PipelineExecutionEventDTO {

    private Long id;

    private String time;

    private MessageRecordType type;

    private String shortMessage;

    private String fullMessage;

    private DPUInstanceDTO dpuInstance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public MessageRecordType getType() {
        return type;
    }

    public void setType(MessageRecordType type) {
        this.type = type;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public DPUInstanceDTO getDpuInstance() {
        return dpuInstance;
    }

    public void setDpuInstance(DPUInstanceDTO dpuInstance) {
        this.dpuInstance = dpuInstance;
    }

}
