package cz.cuni.mff.xrg.odcs.commons.app.module;

/**
 * Exception indicating wrong DPU's jar name format
 * 
 * @author mvi
 *
 */
public class DPUJarNameFormatException extends Exception {

    private static final long serialVersionUID = -1114519630027656944L;

    /**
     * 
     * @param cause
     *          Cause of the {@link DPUJarNameFormatException}
     */
    public DPUJarNameFormatException(String cause) {
        super(cause);
    }
    
    /**
     * 
     * @param message
     *          Description of the error
     * @param cause
     *          Cause of the {@link DPUJarNameFormatException}
     */
    public DPUJarNameFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
