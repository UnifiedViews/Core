package cz.cuni.mff.xrg.odcs.backend.dpu.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecordType;

/**
 * Class for representing DPURecord messages send by ProcessingContext
 * sendMessage.
 * 
 * @author Petyr
 */
public final class DPUMessage extends DPUEvent {

    private static final Logger LOG = LoggerFactory.getLogger(DPUMessage.class);

    public DPUMessage(String shortMessage,
            String longMessage,
            DPUContext.MessageType type,
            Context context,
            Object source) {
        super(context, source, MessageRecordType.fromMessageType(type),
                shortMessage, longMessage);
        // log based on type of message
        switch (type) {
            case DEBUG:
                LOG.debug("DPU '{}' publish message short: '{}' long: '{}'",
                        context.getDPU().getName(),
                        shortMessage,
                        longMessage);
                break;
            case ERROR:
                LOG.error("DPU '{}' publish message short: '{}' long: '{}'",
                        context.getDPU().getName(),
                        shortMessage,
                        longMessage);
                break;
            case INFO:
                LOG.info("DPU '{}' publish message short: '{}' long: '{}'",
                        context.getDPU().getName(),
                        shortMessage,
                        longMessage);
                break;
            case WARNING:
                LOG.warn("DPU '{}' publish message short: '{}' long: '{}'",
                        context.getDPU().getName(),
                        shortMessage,
                        longMessage);
                break;
        }
    }

}
