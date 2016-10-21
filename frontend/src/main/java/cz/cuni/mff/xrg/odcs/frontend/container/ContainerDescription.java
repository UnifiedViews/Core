package cz.cuni.mff.xrg.odcs.frontend.container;

import java.util.List;

/**
 * Interface for container self description.
 * 
 * @author Petyr
 */
public interface ContainerDescription {

    /**
     * Return ids of columns that are filterable. If there are no filters
     * available then return empty List.
     * 
     * @return ids of columns that are filterable
     */
    public List<String> getFilterables();

    /**
     * Return name for column of given id.
     * 
     * @param id
     * @return name for column of given id
     */
    public String getColumnName(String id);

    /**
     * Return ids of column that are visible.
     * 
     * @return ids of column that are visible
     */
    public List<String> getVisibles();

}
