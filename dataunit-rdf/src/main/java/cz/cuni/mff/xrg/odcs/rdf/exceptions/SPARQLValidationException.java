package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Exception is thrown when SPARQL data are not valid using SPARQL validator.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class SPARQLValidationException extends DPUConfigException {

    private int queryNumber = 1;

    /**
     * Create new instance of {@link SPARQLValidationException} with the
     * specific message.
     * 
     * @param message
     *            String value of described message
     */
    public SPARQLValidationException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link SPARQLValidationException} with the
     * specific message and number of invalid SPARQL query.
     * 
     * @param message
     *            String value of described message
     * @param queryNumber
     *            Number of SPARQL query which is not valid (in case of
     *            validation more queries).
     */
    public SPARQLValidationException(String message, int queryNumber) {
        super(message);
        this.queryNumber = queryNumber;
    }

    /**
     * Create new instance of {@link SPARQLValidationException} with the cause
     * of throwing this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public SPARQLValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link SPARQLValidationException} with the
     * specific message and the cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public SPARQLValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the number of SPARQL query which is not valid. This method is
     * used to specify number invalid query in case of validation more SPARQL
     * queries.
     * 
     * @return the number of SPARQL query which is not valid. This method is
     *         used to specify number invalid query in case of validation more
     *         SPARQL queries.
     */
    public int getQueryNumber() {
        return queryNumber;
    }
}
