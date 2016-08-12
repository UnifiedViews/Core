package cz.cuni.mff.xrg.odcs.backend.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filter log events base on MDC values. The filter test if there is
 * value with given key in MDC. If not, the message is DENY. Otherwise
 * return NEUTRAL.
 * 
 * @author Petyr
 */
public class MdcFilter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {

    /**
     * Required MDC key.
     */
    private String requiredKey;

    public MdcFilter() {
        this.requiredKey = "";
    }

    public MdcFilter(String requiredKey) {
        this.requiredKey = requiredKey;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMDCPropertyMap().containsKey(requiredKey)) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }
    }

    public void setRequiredKey(String requiredKey) {
        this.requiredKey = requiredKey;
    }

}
