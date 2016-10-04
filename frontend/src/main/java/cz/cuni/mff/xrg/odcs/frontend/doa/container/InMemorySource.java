package cz.cuni.mff.xrg.odcs.frontend.doa.container;

import cz.cuni.mff.xrg.odcs.commons.app.dao.*;

import java.util.*;

/**
 * Implementation of {@link ContainerSource}. The data are all loaded and hold
 * in memory.
 * Call {@link #loadData(cz.cuni.mff.xrg.odcs.commons.app.dao.DataAccessRead)} method to load data before first use.
 * 
 * @author Petyr
 * @param <T>
 * @param <BUILDER>
 * @param <QUERY>
 * @param <QUERY_SIZE>
 */
public class InMemorySource<T extends DataObject, BUILDER extends DataQueryBuilder<T, QUERY, QUERY_SIZE>, QUERY extends DataQuery<T>, QUERY_SIZE extends DataQueryCount<T>> implements ContainerSource<T> {

    /**
     * Enable data in-memory filtering (hide, show).
     * 
     * @param <T>
     */
    public interface Filter<T extends DataObject> {

        /**
         * Check the object and decide if show it or not.
         * 
         * @param object
         * @return True if object pass the filter false otherwise.
         */
        boolean filter(T object);

    }

    /**
     * Store data.
     */
    protected final Map<Long, T> data = new HashMap<>();

    /**
     * List of all IDs.
     */
    protected final List<Long> ids = new ArrayList<>();

    /**
     * List of filtered-active IDs.
     */
    protected final List<Long> idsVisible = new ArrayList<>();

    /**
     * Class accessor.
     */
    protected final ClassAccessor<T> classAccessor;

    /**
     * Basic constructor.
     * 
     * @param classAccessor
     *            Class of data in source.
     */
    public InMemorySource(ClassAccessor<T> classAccessor) {
        this.classAccessor = classAccessor;
    }

    /**
     * Extended constructor, loads data.
     * 
     * @param classAccessor
     *            Class of data in source.
     * @param source
     *            {@link DataAccessRead} source.
     */
    public InMemorySource(ClassAccessor<T> classAccessor,
            DataAccessRead<T, BUILDER, QUERY, QUERY_SIZE> source) {
        this.classAccessor = classAccessor;
        loadData(source);
    }

    /**
     * Load new data from data source. The old data are deleted.
     * 
     * @param source
     */
    public final void loadData(DataAccessRead<T, BUILDER, QUERY, QUERY_SIZE> source) {
        // load new data
        final List<T> newData = source.executeList(source.createQueryBuilder().getQuery());
        loadData(newData);
    }

    /**
     * Load new data from data source given list. The old data are deleted.
     * 
     * @param newData
     */
    public void loadData(List<T> newData) {
        // clear lists
        data.clear();
        ids.clear();
        idsVisible.clear();
        // load new data
        for (T item : newData) {
            data.put(item.getId(), item);
            ids.add(item.getId());
        }
        // sort, so we get same order based on id every time
        Collections.sort(ids);
        // all visible
        idsVisible.addAll(ids);
    }

    /**
     * Add given object into the source.
     * 
     * @param object
     */
    public void add(T object) {
        if (data.containsKey(object.getId())) {
            // already here ..
        } else {
            data.put(object.getId(), object);
            // add to ids and sort them, so we get the same
            // id's every time
            ids.add(object.getId());
            idsVisible.add(object.getId());
            Collections.sort(ids);
            Collections.sort(idsVisible);
        }
    }

    /**
     * Remove object of given id.
     * 
     * @param id
     */
    public void remove(Long id) {
        ids.remove(id);
        idsVisible.remove(id);
        data.remove(id);
    }

    /**
     * Hide object of given id. The object can be still acceded under the id by
     * the {@link #getObject(java.lang.Long) } method but it's id is not
     * returned by {@link #getItemIds(int, int) }.
     * 
     * @param id
     * @param hard
     *            If true then the item can be bring back only by call
     *            of {@link #show(java.lang.Long)} with it's id.
     */
    public void hide(Long id, boolean hard) {
        idsVisible.remove(id);
        if (hard) {
            ids.remove(id);
        }
    }

    /**
     * Apply filter.
     * 
     * @param useAll
     *            If true all data are made visible and then filtered,
     *            otherwise the visible data are filtered.
     * @param filter
     */
    public void filter(boolean useAll, Filter<T> filter) {
        List<Long> newIdsVisible = new ArrayList<>(ids.size());
        List<Long> toFilter = useAll ? ids : idsVisible;
        for (Long id : toFilter) {
            if (filter.filter(data.get(id))) {
                newIdsVisible.add(id);
            }
        }
        // add the new ids to the visble collection
        idsVisible.clear();
        idsVisible.addAll(newIdsVisible);
        // as we start with sorted colleciton we also 
        // end up with sorted colleciton so we do not have to 
        // resort again
    }

    /**
     * Show object of given id if it has been previously hidden.
     * 
     * @param id
     * @return False if such object is not presented.
     */
    public boolean show(Long id) {
        if (data.containsKey(id)) {
            // ok, we can add
        } else {
            // no such data ..
            return false;
        }

        // add to ids and sort them, so we get the same
        // id's every time
        idsVisible.add(id);
        Collections.sort(ids);
        return true;
    }

    /**
     * Show all data.
     */
    public void showAll() {
        idsVisible.clear();
        idsVisible.addAll(ids);
    }

    @Override
    public int size() {
        return idsVisible.size();
    }

    @Override
    public T getObject(Long id) {
        return data.get(id);
    }

    @Override
    public T getObjectByIndex(int index) {
        return data.get(ids.get(index));
    }

    @Override
    public int indexOfId(Long itemId) {
        return ids.indexOf(itemId);
    }

    @Override
    public boolean containsId(Long id) {
        return data.containsKey(id);
    }

    @Override
    public List<Long> getItemIds(int startIndex, int numberOfItems) {
        List<Long> result = new ArrayList<>(numberOfItems);
        for (int i = 0; i < numberOfItems; ++i) {
            result.add(i, idsVisible.get(i + startIndex));
        }
        return result;
    }

    @Override
    public ClassAccessor<T> getClassAccessor() {
        return classAccessor;
    }

}
