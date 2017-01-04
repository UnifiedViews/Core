package cz.cuni.mff.xrg.odcs.commons.app.execution.message;

import eu.unifiedviews.dpu.DPUContext;


/**
 * Types for execution.record.
 * 
 * @author Petyr
 */
public enum MessageRecordType {
    /**
     * Debug information from DPURecord.
     */
    DPU_DEBUG,
    /**
     * Closer unspecified information from DPURecord.
     */
    DPU_INFO,
    /**
     * Warning from DPURecord.
     */
    DPU_WARNING,
    /**
     * Error from DPURecord.
     */
    DPU_ERROR,
    /**
     * Termination request.
     */
    DPU_TERMINATION_REQUEST,
    /**
     * Represent error message from pipeline.
     */
    PIPELINE_ERROR,
    /**
     * Represent information about pipeline execution.
     */
    PIPELINE_INFO;

    /**
     * Convert {@link MessageType} to {@link MessageRecordType}.
     * 
     * @param type
     *            message type
     * @return record type
     */
    public static MessageRecordType fromMessageType(DPUContext.MessageType type) {
        switch (type) {
            case DEBUG:
                return MessageRecordType.DPU_DEBUG;
            case INFO:
                return MessageRecordType.DPU_INFO;
            case WARNING:
                return MessageRecordType.DPU_WARNING;
            case ERROR:
                return MessageRecordType.DPU_ERROR;
            default:
                return MessageRecordType.DPU_INFO;
        }
    }
}
