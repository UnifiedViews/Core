package cz.cuni.mff.xrg.odcs.commons.app.dao.db;

/**
 * Class used to explain filter.
 * 
 * @author Petyr
 */
public class FilterExplanation {

    private final String propertyName;

    private final String operation;

    private final Object value;

    /**
     * If true then represent a complex query, in such case every other field
     * should be null in such case.
     */
    private final boolean complex;

    /**
     * Create explanation for complex filter.
     */
    public FilterExplanation() {
        this.propertyName = null;
        this.operation = null;
        this.value = null;
        this.complex = true;
    }

    /**
     * Create filter explanation.
     * 
     * @param propertyName
     *            Name of the column in filter.
     * @param operation
     *            Used sql operation.
     * @param value
     *            Value used in filter.
     */
    public FilterExplanation(String propertyName, String operation, Object value) {
        this.propertyName = propertyName;
        this.operation = operation;
        this.value = value;
        this.complex = false;
    }

    /**
     * @return Name of property used in filter.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return String representation of operation in filter. In sql form.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return Value used in filter.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return True if the filter is complex (some logic operation over other
     *         filters) and so cannot be decompose and explain.
     */
    public boolean isComplex() {
        return complex;
    }

}
