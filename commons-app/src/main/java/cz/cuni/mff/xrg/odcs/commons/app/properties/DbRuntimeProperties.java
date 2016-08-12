package cz.cuni.mff.xrg.odcs.commons.app.properties;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccess;

/**
 * Interface for access to {@link RuntimeProperty}.
 * 
 * @author mvi
 */
public interface DbRuntimeProperties extends DbAccess<RuntimeProperty> {

    /**
     * Returns List of all runtime properties in DB
     *  
     * @return List of all runtime properties in DB
     */
    public List<RuntimeProperty> getAll();
    
    /**
     * Finds runtime property according selected name 
     * 
     * @param name
     * @return
     */
    public RuntimeProperty getByName(String name);
}
