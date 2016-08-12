package cz.cuni.mff.xrg.odcs.rdf.exceptions;

/**
 * Exception is thrown when RDF operations (extract,transform,load) cause
 * problems - there were not executed successfully.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class RDFException extends RDFDataUnitException {

    /**
     * Create a new instance of {@link RDFException} with empty default message.
     */
    public RDFException() {
        super();
    }

    /**
     * Create new instance of {@link RDFException} with the cause of throwing
     * this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public RDFException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link RDFException} with the specific message.
     * 
     * @param message
     *            String value of described message
     */
    public RDFException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link RDFException} with the specific message and
     * cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public RDFException(String message, Throwable cause) {
        super(message, cause);
    }
}
