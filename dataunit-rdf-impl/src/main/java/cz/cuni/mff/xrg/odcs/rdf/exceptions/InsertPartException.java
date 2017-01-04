package cz.cuni.mff.xrg.odcs.rdf.exceptions;

/**
 * Exception is thrown when RDF data insert part for loading data to the SPARQL
 * endpoint have some invalid RDF triples.
 * 
 * @author Jiri Tomes
 */
public class InsertPartException extends RDFException {

    /**
     * Create a new instance of {@link InsertPartException} without detail
     * message.
     */
    public InsertPartException() {
        super();
    }

    /**
     * Create new instance of {@link InsertPartException} with specific message.
     * 
     * @param message
     *            String value of described message
     */
    public InsertPartException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link InsertPartException} with cause of throwing
     * this exception.
     * 
     * @param cause
     *            Cause of throwing exception
     */
    public InsertPartException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link InsertPartException} with a specific
     * message and cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            Cause of throwing exception
     */
    public InsertPartException(String message, Throwable cause) {
        super(message, cause);
    }
}
