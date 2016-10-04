package cz.cuni.mff.xrg.odcs.commons.app.module;

/**
 * Class report error during replacing DPU's jar file.
 * 
 * @author Petyr
 */
public class DPUReplaceException extends Exception {

    /**
     * @param cause
     *            Cause of the {@link DPUCreateException}.
     */
    public DPUReplaceException(String cause) {
        super(cause);
    }

}
