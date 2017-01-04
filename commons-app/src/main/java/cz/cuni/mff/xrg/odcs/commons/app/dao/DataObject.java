package cz.cuni.mff.xrg.odcs.commons.app.dao;

import java.io.Serializable;

/**
 * Marker for objects that can be handled by DAO abstract access layer - i.e.,
 * used with {@link DataAccess} and {@link DataAccessRead}.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
public interface DataObject extends Serializable {

    /**
     * @return object's id
     */
    public abstract Long getId();

}
