package cz.cuni.mff.xrg.odcs.commons.app.conf;

/**
 * Represents error caused by invalid configuration value for given property.
 * 
 * @author Jan Vojt
 */
public class InvalidConfigPropertyException extends ConfigPropertyException {

    /**
     * Invalid value found in configuration.
     */
    private String value;

    public InvalidConfigPropertyException(ConfigProperty property) {
        this(property, "unknown");
    }

    public InvalidConfigPropertyException(ConfigProperty property, String value) {
        super(property);
        this.value = value;
    }

    /**
     * @return error message.
     */
    @Override
    public String getMessage() {
        return "Config property '" + property + "' has invalid value of '" + value + "'.";
    }

}
