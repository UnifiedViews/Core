package eu.unifiedviews.commons.rdf.repository;

/**
 *
 * @author Škoda Petr
 */
public class RDFException extends Exception {

    public RDFException(String message) {
        super(message);
    }

    public RDFException(String message, Throwable cause) {
        super(message, cause);
    }

    public RDFException(Throwable cause) {
        super(cause);
    }

}
