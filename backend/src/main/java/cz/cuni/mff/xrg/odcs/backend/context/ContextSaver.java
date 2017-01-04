package cz.cuni.mff.xrg.odcs.backend.context;

/**
 * @author Petyr
 */
public class ContextSaver {

    /**
     * Save data from given context.
     * 
     * @param context
     */
    public void save(Context context) {
        // save existing dataUnits
        context.getInputsManager().save();
        context.getOutputsManager().save();
    }

}
