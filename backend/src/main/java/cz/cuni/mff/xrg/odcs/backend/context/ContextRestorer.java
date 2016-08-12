package cz.cuni.mff.xrg.odcs.backend.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;

/**
 * Reconstruct given {@link Context} based on {@link ExecutionContextInfo} and prepare it for usage.
 * 
 * @author Petyr
 */
class ContextRestorer {

    private static final Logger LOG = LoggerFactory.getLogger(ContextRestorer.class);

    /**
     * Restore data of given context. If there is some data already
     * loaded then does not load them again otherwise nothing happen.
     * 
     * @param context
     */
    public void restore(Context context) throws DataUnitException {
        // we can assume that file exist .. as HDD is persistent
        // so only DataUnits leave to load
        LOG.trace("Context.restore called ...");
        context.getInputsManager().reload();
        context.getOutputsManager().reload();
    }

}
