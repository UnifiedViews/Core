package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

/**
 * Type of schedule ie. condition of activation.
 * 
 * @author Petyr
 */
public enum ScheduleType {
    /** Activate schedule after another pipeline run finish. */
    AFTER_PIPELINE,
    /** Run in given period. */
    PERIODICALLY
}
