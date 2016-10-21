package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Exception is responsible for global problems related with RDF data unit.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class RDFDataUnitException extends DataUnitException {

    /**
     * Create a new instance of {@link RDFDataUnitException} with the empty
     * message.
     */
    public RDFDataUnitException() {
        super("");
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the specific
     * message.
     * 
     * @param message
     *            String value of described message
     */
    public RDFDataUnitException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the cause of
     * throwing this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public RDFDataUnitException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the specific
     * message and the cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public RDFDataUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
