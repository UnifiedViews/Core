package cz.cuni.mff.xrg.odcs.commons.app.module;

/**
 * Exception indicates failure during DPU's creation process.
 * 
 * @author Petyr
 */
public class DPUCreateException extends Exception {

    /**
     * @param cause
     *            Cause of the {@link DPUCreateException}.
     */
    public DPUCreateException(String cause) {
        super(cause);
    }

    /**
     * @param message
     *            Description of action that throws.
     * @param cause
     *            Cause of the {@link DPUCreateException}.
     */
    public DPUCreateException(String message, Throwable cause) {
        super(message, cause);
    }

}
