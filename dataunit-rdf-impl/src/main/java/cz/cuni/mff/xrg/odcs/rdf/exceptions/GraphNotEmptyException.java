package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * This exception is thrown when target graph on the SPARQL endpoint where RDF
 * data will be loaded is not empty.
 * 
 * @author Jiri Tomes
 */
public class GraphNotEmptyException extends DataUnitException {

    /**
     * Create a new instance of {@link GraphNotEmptyException} with {@link #defaultMessage}.
     */
    public GraphNotEmptyException() {
        super(Messages.getString("GraphNotEmptyException.default.messages"));
    }

    /**
     * Create new instance of {@link GraphNotEmptyException} with specific
     * message.
     * 
     * @param message
     *            String value of described message
     */
    public GraphNotEmptyException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link GraphNotEmptyException} with cause of
     * throwing this exception.
     * 
     * @param cause
     *            Cause of throwing exception
     */
    public GraphNotEmptyException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link GraphNotEmptyException} with a
     * specific message and cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            Cause of throwing exception
     */
    public GraphNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
