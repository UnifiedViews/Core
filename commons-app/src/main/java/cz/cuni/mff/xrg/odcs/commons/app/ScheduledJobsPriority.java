package cz.cuni.mff.xrg.odcs.commons.app;

/**
 * Priorities for the scheduled events
 * 
 * @author tomasknap
 */
public enum ScheduledJobsPriority {
    IGNORE(0), HIGHEST(3), MEDIUM(2), LOWEST(1);

    private final long priority;

    ScheduledJobsPriority(long priority) {
        this.priority = priority;
    }

    public long getValue() {
        return priority;
    }

    public static ScheduledJobsPriority getForValue(final long value) {
        if (value <= 0) {
            return IGNORE;
        } else if (value == MEDIUM.getValue()) {
            return MEDIUM;
        } else if (value == LOWEST.getValue()) {
            return LOWEST;
        }
        return HIGHEST; // value >= HIGHEST
    }
}
