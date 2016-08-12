package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;

/**
 * Exception is thrown when during {@link RDFParser#parse(java.io.InputStream, java.lang.String)} method is the
 * execution cancelled manually by user.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class RDFCancelException extends RDFHandlerException {

    /**
     * 
     */
    private static final long serialVersionUID = 6289348156771806583L;

    /**
     * Create new instance of {@link RDFCancelException} with the specific
     * message.
     * 
     * @param msg
     *            String value of described message
     */
    public RDFCancelException(String msg) {
        super(msg);
    }

    /**
     * Create new instance of {@link RDFCancelException} with the cause of
     * throwing this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public RDFCancelException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link RDFCancelException} with the specific
     * message and the cause of throwing this exception.
     * 
     * @param msg
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public RDFCancelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
