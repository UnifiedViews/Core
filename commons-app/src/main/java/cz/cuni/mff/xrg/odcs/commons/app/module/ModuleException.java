package cz.cuni.mff.xrg.odcs.commons.app.module;

/**
 * Base exception used by {@link cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade} and related classes.
 * 
 * @author Petyr
 */
public class ModuleException extends Exception {

    /**
     * @param cause
     *            Cause of this exception.
     */
    public ModuleException(Throwable cause) {
        super(cause);
    }

    /**
     * @param cause
     *            Cause of this exception.
     */
    public ModuleException(String cause) {
        super(cause);
    }

    /**
     * @param message
     *            Description of action that throws.
     * @param cause
     *            Cause of this exception.
     */
    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }

}
