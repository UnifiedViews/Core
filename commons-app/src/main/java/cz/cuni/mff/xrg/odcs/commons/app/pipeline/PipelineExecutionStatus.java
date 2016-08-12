package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.util.EnumSet;

/**
 * Set of possible states during pipeline execution.
 * 
 * @author Jiri Tomes
 * @author Petyr
 */
public enum PipelineExecutionStatus {

    /**
     * Pipeline is scheduled for run and will run as soon as possible.
     */
    QUEUED,
    /**
     * Pipeline is recently running.
     */
    RUNNING,
    /**
     * Pipeline is being cancelled on user request.
     */
    CANCELLING,
    /**
     * Pipeline execution end because user cancel it.
     */
    CANCELLED,
    /**
     * Pipeline execution failed.
     */
    FAILED,
    /**
     * Pipeline execution has been successful and there were no WARN+ messages
     * or logs.
     */
    FINISHED_SUCCESS,
    /**
     * Pipeline execution has been successful but there ase some WARN+ record.
     */
    FINISHED_WARNING;

    /**
     * Set of execution statuses hinting that execution was completed, or
     * failed. Thus execution canceled by user request are not included. All
     * executions with this status have a valid duration.
     */
    public static final EnumSet<PipelineExecutionStatus> FINISHED = EnumSet.of(
            FINISHED_SUCCESS,
            FINISHED_WARNING,
            FAILED
            );
}
