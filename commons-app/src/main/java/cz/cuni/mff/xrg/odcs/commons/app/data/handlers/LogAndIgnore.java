package cz.cuni.mff.xrg.odcs.commons.app.data.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeCompiler;

/**
 * Default error handler for {@link EdgeCompiler}.
 * 
 * @author Petyr
 */
public class LogAndIgnore implements EdgeCompiler.ErrorHandler {

    private final static Logger LOG = LoggerFactory.getLogger(LogAndIgnore.class);

    @Override
    public void sourceIndexOutOfRange() {
        LOG.warn("Source index out of range, mapping has been ignored");
    }

    @Override
    public void targetIndexOutOfRange() {
        LOG.warn("Target index out of range, mapping has been ignored");
    }

    @Override
    public void unknownCommand(String item) {
        LOG.warn("Unknown command: {}", item);
    }

    @Override
    public void invalidMapping(String item) {
        LOG.warn("Invalid mapping: {}", item);
    }

}
