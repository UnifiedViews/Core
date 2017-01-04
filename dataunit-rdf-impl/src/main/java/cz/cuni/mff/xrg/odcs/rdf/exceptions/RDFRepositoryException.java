package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import org.openrdf.repository.RepositoryException;

/**
 * Custom replacement for {@link RepositoryException} with identical behavior
 * and representation. The only difference is that this exception is runtime, so
 * we do not have to litter our code with try-catch blocks. In case this
 * exception is thrown, we usually still do not know what to do in the catch
 * block.
 * 
 * @see RepositoryException for more info.
 * @author Jan Vojt
 */
public class RDFRepositoryException extends RuntimeException {

    /**
     * Creates a new instance of {@link RDFRepositoryException} without detail
     * message.
     */
    public RDFRepositoryException() {
    }

    /**
     * Constructs an instance of {@link RDFRepositoryException} with the
     * specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public RDFRepositoryException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link RDFRepositoryException} with specified
     * detail message and root cause.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            Cause of throwing exception
     */
    public RDFRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link RDFRepositoryException} with specified
     * root cause.
     * 
     * @param cause
     *            Cause of throwing exception.
     */
    public RDFRepositoryException(Throwable cause) {
        super(cause);
    }
}
