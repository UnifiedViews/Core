package cz.cuni.mff.xrg.odcs.frontend.dpu.wrap;

/**
 * Exception used to wrap other exception that can occurs
 * during working with {@link DPURecordWrap} and it's descendants.
 * 
 * @author Petyr
 */
public class DPUWrapException extends Exception {

    /**
     * Constructor.
     * 
     * @param cause
     *            Cause of the exception.
     */
    public DPUWrapException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     * @param cause
     */
    public DPUWrapException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     */
    public DPUWrapException(String message) {
        super(message);
    }

}
