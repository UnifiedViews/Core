package cz.cuni.mff.xrg.odcs.frontend.doa.container;

import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;

/**
 * Describe class so it can be used in {@link ReadOnlyContainer}.
 * 
 * @author Petyr
 * @param <T>
 */
public interface ClassAccessor<T extends DataObject> {

    /**
     * Return list of object's id.
     * 
     * @return list of object's id
     */
    public List<String> all();

    /**
     * Return subset of {@link #all()}. This subset will be enabled for
     * sorting.
     * 
     * @return sortable subset of id's
     */
    public List<String> sortable();

    /**
     * Return subset of {@link #all()}. This subset will be enabled for
     * filtering.
     * 
     * @return filterable subset of id's
     */
    public List<String> filterable();

    /**
     * Return subset of {@link #all()}. This subset will be visible.
     * 
     * @return visible subset of id's
     */
    public List<String> visible();

    /**
     * List of properties to fetch.
     * 
     * @return List of properties to fetch.
     */
    public List<String> toFetch();

    /**
     * Return entity class.
     * 
     * @return entity class
     */
    public Class<T> getEntityClass();

    /**
     * Return name of column for given id.
     * 
     * @param id
     * @return name of column for given id
     */
    public String getColumnName(String id);

    /**
     * Return value of object's given variable.
     * 
     * @param object
     * @param id
     *            Variable identification.
     * @return value of object's given variable
     */
    public Object getValue(T object, String id);

    /**
     * Return type for given variable.
     * 
     * @param id
     *            Variable identification.
     * @return type for given variable
     */
    public Class<?> getType(String id);
}
