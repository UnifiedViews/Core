package cz.cuni.mff.xrg.odcs.backend.logback;

import java.util.concurrent.ConcurrentHashMap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.Log;

/**
 * Use MDC value {@link Log#MDC_EXECUTION_KEY_NAME} to determine related execution and then do Level filtering based on stored
 * informations.
 * 
 * @author Petyr
 */
public class MdcExecutionLevelFilter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {

    /**
     * Store required minimal levels for executions. This is shared
     * between thread.
     * The key is name of the execution, the value (level) is
     * the minimal allowed log level.
     */
    private static final ConcurrentHashMap<String, Level> levels = new ConcurrentHashMap<>();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        final String execution = event.getMDCPropertyMap().get(Log.MDC_EXECUTION_KEY_NAME);
        Level level = levels.get(execution);
        if (level == null) {
            // no restriction
            return FilterReply.NEUTRAL;
        } else {
            // use filter level
            if (event.getLevel().isGreaterOrEqual(level)) {
                return FilterReply.NEUTRAL;
            } else {
                // event.level < minLevel
                return FilterReply.DENY;
            }
        }
    }

    /**
     * Add record for min. required level for given execution.
     * 
     * @param execution
     * @param level
     */
    public static void add(String execution, Level level) {
        levels.put(execution, level);
    }

    /**
     * Remove record for given execution.
     * 
     * @param execution
     */
    public static void remove(String execution) {
        levels.remove(execution);
    }

}
