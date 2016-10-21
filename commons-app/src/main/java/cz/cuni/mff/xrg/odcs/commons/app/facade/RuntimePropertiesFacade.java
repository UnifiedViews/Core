package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.List;
import java.util.Locale;

import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;

/**
 * Facade for fetching persisted entities.
 *
 * @author mvi
 *
 */
public interface RuntimePropertiesFacade extends Facade {

    /**
     * Returns List of all runtime properties currently persisted in database
     *
     * @return List of all runtime properties
     */
    public List<RuntimeProperty> getAllRuntimeProperties();

    /**
     * Saves any modifications made to the property into the database.
     *
     * @param property
     */
    public void save(RuntimeProperty property);

    /**
     * Deletes property from database
     *
     * @param property
     */
    public void delete(RuntimeProperty property);

    /**
     * Returns property with selected name
     *
     * @param name
     * @return
     */
    public RuntimeProperty getByName(String name);
}
