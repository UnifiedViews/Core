package cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter;

/**
 * Base class for simple filters.
 * 
 * @author Petyr
 */
public class BaseFilter {

    /**
     * Name of property to which the filter apply.
     */
    String propertyName;

    protected BaseFilter(String propertyName) {
        this.propertyName = propertyName;
    }

}
