package eu.unifiedviews.dataunit.relational.repository;

/**
 * Exception which can be thrown when problems with relational repository occur
 */
public class RelationalException extends Exception {

    private static final long serialVersionUID = -7477415761453944871L;

    public RelationalException(String message) {
        super(message);
    }

    public RelationalException(String message, Throwable cause) {
        super(message, cause);
    }

    public RelationalException(Throwable cause) {
        super(cause);
    }

}
