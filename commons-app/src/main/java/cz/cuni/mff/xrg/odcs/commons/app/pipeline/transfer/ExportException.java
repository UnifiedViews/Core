package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

/**
 * Exception used by {@link ExportService}
 * 
 * @author Škoda Petr
 */
public class ExportException extends Exception {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

}
