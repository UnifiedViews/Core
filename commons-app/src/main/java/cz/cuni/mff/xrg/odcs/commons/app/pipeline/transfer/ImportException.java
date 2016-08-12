package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

/**
 * Exception used by {@link ImportService}.
 * 
 * @author Škoda Petr
 */
public class ImportException extends Exception {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
