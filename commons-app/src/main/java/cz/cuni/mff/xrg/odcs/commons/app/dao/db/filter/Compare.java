package cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter;

/**
 * Filter for equality of given property.
 * 
 * @author Petyr
 */
public class Compare extends BaseFilter {

    Object propertyValue;

    CompareType type;

    private Compare(String propertyName, Object propertyValue, CompareType type) {
        super(propertyName);
        this.propertyValue = propertyValue;
        this.type = type;
    }

    /**
     * Create filter for equality.
     * 
     * @param propertyName
     *            Name of property to use in filter.
     * @param object
     *            Value to filter against.
     * @return Filter representing: propertyName == object
     */
    public static Compare equal(String propertyName, Object object) {
        return new Compare(propertyName, object, CompareType.EQUAL);
    }

    /**
     * Create filter for greater.
     * 
     * @param propertyName
     *            Name of property to use in filter.
     * @param object
     *            Value to filter against.
     * @return Filter representing: propertyName > object
     */
    public static Compare greater(String propertyName, Comparable object) {
        return new Compare(propertyName, object, CompareType.GREATER);
    }

    /**
     * Create filter for greater or equal.
     * 
     * @param propertyName
     *            Name of property to use in filter.
     * @param object
     *            Value to filter against.
     * @return Filter representing: propertyName >= object
     */
    public static Compare greaterEqual(String propertyName, Comparable object) {
        return new Compare(propertyName, object, CompareType.GREATER_OR_EQUAL);
    }

    /**
     * Create filter for less.
     * 
     * @param propertyName
     *            Name of property to use in filter.
     * @param object
     *            Value to filter against.
     * @return Filter representing: propertyName < object
     */
    public static Compare less(String propertyName, Comparable object) {
        return new Compare(propertyName, object, CompareType.LESS);
    }

    /**
     * Create filter for less or equal.
     * 
     * @param propertyName
     *            Name of property to use in filter.
     * @param object
     * @return Filter representing: propertyName <= object
     */
    public static Compare lessEqual(String propertyName, Comparable object) {
        return new Compare(propertyName, object, CompareType.LESS_OR_EQUAL);
    }

}
