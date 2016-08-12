package cz.cuni.mff.xrg.odcs.commons.app.conf;

/**
 * Represents syntax error in configuration file.
 * 
 * @author Jan Vojt
 */
public class MalformedConfigFileException extends RuntimeException {

    /**
     * Creates a new instance of <code>MalformedConfigFileException</code> without detail message.
     */
    public MalformedConfigFileException() {
    }

    /**
     * Constructs an instance of <code>MalformedConfigFileException</code> with
     * the specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public MalformedConfigFileException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>MalformedConfigFileException</code> with
     * the specified detail message and cause.
     * 
     * @param message
     * @param cause
     */
    public MalformedConfigFileException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of <code>MalformedConfigFileException</code> with
     * the specified cause.
     * 
     * @param cause
     */
    public MalformedConfigFileException(Throwable cause) {
        super(cause);
    }

}
