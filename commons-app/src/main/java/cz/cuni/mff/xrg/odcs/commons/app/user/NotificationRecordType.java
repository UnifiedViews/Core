package cz.cuni.mff.xrg.odcs.commons.app.user;

public enum NotificationRecordType {

    /** Instantly after pipeline is executed - new email for every executed pipeline. */
    INSTANT,
    /** In bulk report containing reports about executed pipelines in given time period(day). */
    DAILY,
    /** Email not informed at all */
    NO_REPORT

}
