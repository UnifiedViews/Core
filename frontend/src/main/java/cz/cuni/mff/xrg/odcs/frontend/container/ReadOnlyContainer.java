package cz.cuni.mff.xrg.odcs.frontend.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.UnsupportedFilterException;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataAccessRead;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ContainerSource;

/**
 * Implementation of read only container that use {@link DataAccessRead} as data
 * source.
 * The container provide possibility to set core filters in null {@link #ReadOnlyContainer(cz.cuni.mff.xrg.odcs.frontend.doa.container.ContainerSource)} those
 * filters are then applied in every data query. The given list is used as
 * reference so changes in the list will be reflected by the container. There
 * are no guards for concurrent access, so it's up to the user to secure that
 * the list will not be modified in time of querying for data.
 * 
 * @author Petyr
 * @param <T>
 */
public class ReadOnlyContainer<T extends DataObject> implements Container,
        Container.Indexed, Container.Filterable, Container.Sortable,
        Container.ItemSetChangeNotifier, ContainerDescription {

    private final static Logger LOG = LoggerFactory
            .getLogger(ReadOnlyContainer.class);

    /**
     * Container's data source.
     */
    private final ContainerSource<T> source;

    /**
     * {@link ClassAccessor} used to gain information about presented class.
     */
    private final ClassAccessor<T> classAccessor;

    /**
     * Store definition for properties ie. columns.
     */
    private final Map<Object, GeneralProperty<?, T>> properties = new HashMap<>();

    /**
     * Contains properties id in sorted order.
     */
    private final List<Object> propertiesIds = new LinkedList<>();

    /**
     * List of registered onChange listeners.
     */
    private final List<ItemSetChangeListener> changeListeners = new LinkedList<>();

    /**
     * Create read only container.
     * 
     * @param source
     *            data access
     */
    @SuppressWarnings("unchecked")
    public ReadOnlyContainer(ContainerSource<T> source) {
        this.source = source;
        this.classAccessor = source.getClassAccessor();
        // build properties list
        buildProperties();
    }

    /**
     * Build properties list and and print warnings about sorting and filtering.
     */
    private void buildProperties() {
        // build properties list
        for (String id : classAccessor.all()) {
            properties.put(id, new GeneralProperty(classAccessor.getType(id),
                    id, this));
            // we also add id
            propertiesIds.add(id);
        }
    }

    Map<Object, GeneralProperty<?, T>> getProperties() {
        return properties;
    }

    ClassAccessor<T> getClassAccessor() {
        return classAccessor;
    }

    /**
     * Announce that the container has changed to tables.
     */
    public void refresh() {
        LOG.trace("{}.refresh()", classAccessor.getClass().getSimpleName());
        final ReadOnlyContainer<T> container = this;

        for (ItemSetChangeListener listener : changeListeners) {
            // emit change
            listener.containerItemSetChange(new ItemSetChangeEvent() {
                @Override
                public Container getContainer() {
                    return container;
                }
            });
        }
    }

    /**
     * Return object with given id.
     * 
     * @param id
     * @return object with given id
     */
    T getObject(Long id) {
        return source.getObject(id);
    }

    @Override
    public Item getItem(Object itemId) {
        if (itemId == null) {
            LOG.warn("Request for null id ignored.");
            return null;
        }

        return new ValueItem(this, (Long) itemId);
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return propertiesIds;
    }

    @Override
    public Collection<?> getItemIds() {
        int size = source.size();
        return source.getItemIds(0, size);
    }

    @Override
    public Property<?> getContainerProperty(Object itemId, Object propertyId) {
        return properties.get(propertyId).bind((Long) itemId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return properties.get(propertyId).getType();
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return source.containsId((Long) itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object nextItemId(Object itemId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object prevItemId(Object itemId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object firstItemId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lastItemId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return firstItemId() == itemId;
    }

    @Override
    public boolean isLastId(Object itemId) {
        return lastItemId() == itemId;
    }

    @Override
    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOfId(Object itemId) {
        return source.indexOfId((Long) itemId);
    }

    @Override
    public Object getIdByIndex(int index) {
        return source.getObjectByIndex(index);
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return source.getItemIds(startIndex, numberOfItems);
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addContainerFilter(Filter filter)
            throws UnsupportedFilterException {
        if (source instanceof ContainerSource.Filterable) {
            ((ContainerSource.Filterable) source).addFilter(filter);
        } else {
            LOG.warn("The data source is not filterable. "
                    + "Ignoring addContainerFilter call.");
        }
    }

    @Override
    public void removeContainerFilter(Filter filter) {
        if (source instanceof ContainerSource.Filterable) {
            ((ContainerSource.Filterable) source).removeFilter(filter);
        } else {
            LOG.warn("The data source is not filterable. "
                    + "Ignoring removeContainerFilter call.");
        }
    }

    @Override
    public void removeAllContainerFilters() {
        if (source instanceof ContainerSource.Filterable) {
            ((ContainerSource.Filterable) source).removeAllFilters();
        } else {
            LOG.warn("The data source is not filterable. "
                    + "Ignoring removeAllContainerFilters call.");
        }
    }

    @Override
    public Collection<Filter> getContainerFilters() {
        if (source instanceof ContainerSource.Filterable) {
            return ((ContainerSource.Filterable) source).getFilters();
        } else {
            LOG.warn("The data source is not filterable. "
                    + "Ignoring getContainerFilters call.");
            return Arrays.asList(new Filter[] {});
        }
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (source instanceof ContainerSource.Sortable) {
            ((ContainerSource.Sortable) source).sort(propertyId, ascending);
        } else {
            LOG.warn("The data source is not sortable. Ignoring sort call.");
        }
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        if (source instanceof ContainerSource.Sortable) {
            // ok return list from accessor
            return classAccessor.sortable();
        } else {
            if (!classAccessor.sortable().isEmpty()) {
                LOG.warn("Container has non sortable source but "
                        + "ClassAccess {} define sortable columns.",
                        classAccessor.getClass().getSimpleName());
            }
            return new ArrayList<>(0);
        }
    }

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        // this method is deprecated, so just recall the right one
        addItemSetChangeListener(listener);
    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        removeItemSetChangeListener(listener);
    }

    @Override
    public List<String> getFilterables() {
        if (source instanceof ContainerSource.Filterable) {
            // ok return list from accessor
            return classAccessor.filterable();
        } else {
            if (!classAccessor.filterable().isEmpty()) {
                LOG.warn("Container has non filterable source but "
                        + "ClassAccess {} define filterable columns.",
                        classAccessor.getClass().getSimpleName());
            }
            return new ArrayList<>(0);
        }
    }

    @Override
    public String getColumnName(String id) {
        return classAccessor.getColumnName(id);
    }

    @Override
    public List<String> getVisibles() {
        return classAccessor.visible();
    }

}
