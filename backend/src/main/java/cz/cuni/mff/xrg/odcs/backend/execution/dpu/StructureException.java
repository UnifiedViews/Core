package cz.cuni.mff.xrg.odcs.backend.execution.dpu;

/**
 * Exception inform about problem in {@link cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline} structure.
 * 
 * @author Petyr
 */
public class StructureException extends Exception {

    public StructureException(Throwable cause) {
        super(cause);
    }

    public StructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public StructureException(String message) {
        super(message);
    }

}
