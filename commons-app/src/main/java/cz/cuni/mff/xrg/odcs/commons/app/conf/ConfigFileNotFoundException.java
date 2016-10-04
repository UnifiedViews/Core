package cz.cuni.mff.xrg.odcs.commons.app.conf;

/**
 * Represents an error when configuration file cannot be read.
 * 
 * @author Jan Vojt
 */
public class ConfigFileNotFoundException extends RuntimeException {

    /**
     * Creates a new instance of <code>ConfigFileNotFoundException</code> without detail message.
     */
    public ConfigFileNotFoundException() {
    }

    /**
     * Constructs an instance of <code>ConfigFileNotFoundException</code> with
     * the specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public ConfigFileNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ConfigFileNotFoundException</code> with
     * the specified detail message and root cause.
     * 
     * @param message
     * @param cause
     */
    public ConfigFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of <code>ConfigFileNotFoundException</code> with
     * the specified root cause.
     * 
     * @param cause
     */
    public ConfigFileNotFoundException(Throwable cause) {
        super(cause);
    }

}
