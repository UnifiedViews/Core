package cz.cuni.mff.xrg.odcs.backend.context;

/**
 * Close and save the {@link Context} does not delete the data so {@link Context} can be reconstructed later.
 * 
 * @author Petyr
 */
class ContextCloser {

    /**
     * Closet the given context. The context should not be
     * called after is closed by this method.
     * 
     * @param context
     */
    public void close(Context context) {
        // release data
        context.getInputsManager().release();
        context.getOutputsManager().release();

        // we do not delete any directories or files
    }

}
