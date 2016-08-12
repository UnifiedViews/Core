package cz.cuni.mff.xrg.odcs.commons.app.module.osgi;

/**
 * Used to announce that {@link OSGIModuleFacade#start} failed.
 * 
 * @author Petyr
 */
public class FrameworkStartFailedException extends Exception {

    /**
     * @param message
     *            Description of action that throws.
     * @param cause
     *            Cause of the {@link FrameworkStartFailedException}.
     */
    public FrameworkStartFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
