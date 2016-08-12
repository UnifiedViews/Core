package cz.cuni.mff.xrg.odcs.commons.app.conf;

/**
 * Represents an error caused by missing property in configuration.
 * 
 * @author Jan Vojt
 */
public class MissingConfigPropertyException extends ConfigPropertyException {

    public MissingConfigPropertyException(ConfigProperty property) {
        super(property);
    }

    /**
     * @return error message.
     */
    @Override
    public String getMessage() {
        return "Config is missing property: " + property + ".";
    }
}
