package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

/**
 * Period unit used in scheduler. Defines a time unit for time period after
 * which pipeline is repeatedly executed.
 * 
 * @author Petyr
 */
public enum PeriodUnit {
    MINUTE
    , HOUR
    , DAY
    , WEEK
    , MONTH
    , YEAR
}
