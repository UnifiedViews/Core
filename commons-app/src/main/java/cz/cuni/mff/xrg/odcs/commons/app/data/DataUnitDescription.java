package cz.cuni.mff.xrg.odcs.commons.app.data;

/**
 * Class describe {@link cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo} class.
 * 
 * @author Petyr
 */
public class DataUnitDescription {

    /**
     * Name.
     */
    private final String name;

    /**
     * Type class name.
     */
    private final String typeName;

    /**
     * Description provided by developer.
     */
    private final String description;

    /**
     * True if usage of {@link cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo} is optional.
     */
    private final boolean optional;

    private DataUnitDescription(String name,
            String typeName,
            String description,
            boolean optional) {
        this.name = name;
        this.typeName = typeName;
        this.description = description;
        this.optional = optional;
    }

    /**
     * Create description for output {@link cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo}.
     * 
     * @param name
     *            Name of data unit.
     * @param typeName
     *            Type of data unit.
     * @param description
     *            Description of data unit.
     * @param optional
     *            True if the output data unit is optional.
     * @return description
     */
    public static DataUnitDescription createOutput(String name,
            String typeName,
            String description,
            boolean optional) {
        return new DataUnitDescription(name, typeName, description, optional);
    }

    /**
     * Create description for input {@link cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo}.
     * 
     * @param name
     *            Name of data unit.
     * @param typeName
     *            Type of data unit.
     * @param description
     *            Description of data unit.
     * @param optional
     *            True if the input data unit is optional.
     * @return description
     */
    public static DataUnitDescription createInput(String name,
            String typeName,
            String description,
            boolean optional) {
        return new DataUnitDescription(name, typeName, description, optional);
    }

    /**
     * @return Name of data unit.
     */
    public String getName() {
        return name;
    }

    /**
     * @return String representation of data unit type.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return Description of data unit provided by dpu.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return true if the usage of this DataUnit is optional. For output {@link cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo} always return
     *         false.
     */
    public boolean getOptional() {
        return this.optional;
    }

    @Override
    public String toString() {
        return name;
    }

}
