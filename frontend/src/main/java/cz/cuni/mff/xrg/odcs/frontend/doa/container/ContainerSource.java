package cz.cuni.mff.xrg.odcs.frontend.doa.container;

import com.vaadin.data.Container;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import java.util.Collection;
import java.util.List;

/**
 * Interface for data source that is meant to be used by containers.
 * 
 * @author Petyr
 * @param <T>
 */
public interface ContainerSource<T extends DataObject> {

    /**
     * Interface provides filtering abilities on source.
     */
    public interface Filterable {

        /**
         * Add filter to the data source.
         * 
         * @param filter
         */
        void addFilter(Container.Filter filter);

        /**
         * Remove given filter.
         * 
         * @param filter
         */
        void removeFilter(Container.Filter filter);

        /**
         * Remove all filters set by {@link #addFilter(com.vaadin.data.Container.Filter)}.
         */
        void removeAllFilters();

        /**
         * Return collection of filters that has been added and not removed by {@link #addFilter(com.vaadin.data.Container.Filter)}.
         * 
         * @return collection of filters
         */
        Collection<Container.Filter> getFilters();
    }

    /**
     * Interface provides sorting abilities on source.
     */
    public interface Sortable {

        /**
         * Apply sorting. Only one sorting can be active at time.
         * 
         * @param propertyId
         *            Use null to remove sorting.
         * @param ascending
         */
        void sort(Object[] propertyId, boolean[] ascending);
    }

    /**
     * Return data size. Respects current filters.
     * 
     * @return data size
     */
    int size();

    /**
     * Return object of given id. Respects current filters.
     * 
     * @param id
     * @return object of given id
     */
    T getObject(Long id);

    /**
     * Return object with given index. Respects current filters.
     * 
     * @param index
     * @return object with given index
     */
    T getObjectByIndex(int index);

    /**
     * Return index of given id.
     * 
     * @param itemId
     * @return index of given id
     */
    int indexOfId(Long itemId);

    /**
     * Check if object of given id is presented and available with current
     * filters.
     * 
     * @param id
     * @return if object of given id is present and available
     */
    boolean containsId(Long id);

    /**
     * Return id's of items on given positions. Respects current filters.
     * 
     * @param startIndex
     * @param numberOfItems
     * @return id's of items on given positions
     */
    List<Long> getItemIds(int startIndex, int numberOfItems);

    /**
     * Return instance of used class accessor.
     * 
     * @return instance of used class accessor
     */
    ClassAccessor<T> getClassAccessor();

}
