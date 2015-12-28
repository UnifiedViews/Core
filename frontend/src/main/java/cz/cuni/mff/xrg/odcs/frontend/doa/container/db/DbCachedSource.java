/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.frontend.doa.container.db;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbAccessRead;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ClassAccessor;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.ContainerSource;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of {@link ContainerSource}. Has data caching abilities.
 * 
 * @author Petyr
 * @param <T>
 */
public class DbCachedSource<T extends DataObject> implements ContainerSource<T>,
        ContainerSource.Filterable, ContainerSource.Sortable {

    private static final Logger LOG = LoggerFactory.getLogger(DbCachedSource.class);

    /**
     * The maximum size of cache, it this size is exceeded then the cache is
     * cleared.
     */
    private static final int CACHE_MAX_SIZE = 200;

    /**
     * Store size of data set in database.
     */
    protected Integer size;

    /**
     * Store cached data.
     */
    protected final Map<Long, T> data = new HashMap<>();

    /**
     * Store indexes for current data.
     */
    protected final Map<Integer, Long> dataIndexes = new HashMap<>();

    /**
     * Data source.
     */
    protected final DbAccessRead<T> source;

    /**
     * The query builder.
     */
    protected final DbQueryBuilder<T> queryBuilder;

    /**
     * Filters that can be set by {@link Filterable} interface.
     */
    protected final List<Filter> filters = new LinkedList<>();

    /**
     * Special set of core filters.
     */
    protected final List<Filter> coreFilters;

    /**
     * Class accessor.
     */
    protected final ClassAccessor<T> classAccessor;

    protected int pageSize;

    /**
     * Initialize the source with given data access. No core filters are used.
     * 
     * @param access
     * @param classAccessor
     * @param pageSize
     */
    public DbCachedSource(DbAccessRead<T> access, ClassAccessor<T> classAccessor,
            int pageSize) {
        this.source = access;
        this.queryBuilder = source.createQueryBuilder();
        this.coreFilters = null;
        this.classAccessor = classAccessor;
        this.pageSize = pageSize;
    }

    /**
     * Initialize the source with given data access. The core filters are apply
     * before every query, ant the list is used as reference. That means that
     * changes in list changed the used filters in source.
     * 
     * @param access
     * @param classAccessor
     * @param coreFilters
     * @param pageSize
     */
    public DbCachedSource(DbAccessRead<T> access, ClassAccessor<T> classAccessor,
            List<Filter> coreFilters, int pageSize) {
        this.source = access;
        this.queryBuilder = source.createQueryBuilder();
        this.coreFilters = coreFilters;
        this.classAccessor = classAccessor;
        this.pageSize = pageSize;
    }

    /**
     * Invalidate data cache.
     */
    public void invalidate() {
        size = null;
        data.clear();
        dataIndexes.clear();
    }

    /**
     * Return new default instance of variable.
     * 
     * @return new default instance of variable
     */
    private T getDefault() {
        try {
            return classAccessor.getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Failed to create the default instance.", e);
        }
        throw new RuntimeException(Messages.getString("DbCachedSource.data.failed"));
    }

    /**
     * Add item to cache. If the item is null nothing happen. If the item's id
     * is null then negative index is used as index.
     * 
     * @param item
     * @param index
     */
    private void addToCache(T item, int index) {
        // add to caches
        data.put(item.getId(), item);
        dataIndexes.put(index, item.getId());
    }

    /**
     * Load data size from {@link #source} and return it. Do not store them into
     * any member variable.
     */
    int loadSize() {
        applyFilters();
        return (int) source.executeSize(queryBuilder.getCountQuery());
    }

    /**
     * Read data from {@link #source} and return it.
     * 
     * @param index
     */
    T loadByIndex(int index) {
        applyFilters();
        try {
            return source.execute(queryBuilder.getQuery().limit(index, 1));
        } catch (Throwable e) {
            LOG.error("Failed to load item. Try to use new instance instead.", e);
        }
        // try to return default instead
        return getDefault();
    }

    /**
     * Load data on given indexes and return them.
     * 
     * @param startIndex
     * @param numberOfItems
     * @return data on given indexes
     */
    List<T> loadByIndex(int startIndex, int numberOfItems) {
        applyFilters();
        try {
            final List<T> items = source.executeList(
                    queryBuilder.getQuery().limit(startIndex, numberOfItems));
            return items;
        } catch (Throwable e) {
            // failed to load some message
        }
        // let's try it by one .. 
        final List<T> result = new LinkedList<>();
        for (int index = 0; index < numberOfItems; ++index) {
            result.add(loadByIndex(startIndex + index));
        }
        return result;
    }

    /**
     * Read data from {@link #source} with given ID.
     * 
     * @param id
     */
    protected void loadById(Long id) {
        LOG.trace("{}.loadById({})", classAccessor.getClass().getSimpleName(), id);
        applyFilters();

        if (queryBuilder instanceof DataQueryBuilder.Filterable) {
            // ok continue
        } else {
            LOG.warn("Can not set filters on nonfilterable query builder."
                    + " We can not ask for given id.");
            return;
        }

        final DbQueryBuilder.Filterable filtrableBuilder = (DbQueryBuilder.Filterable) queryBuilder;

        filtrableBuilder.addFilter(new Compare.Equal("id", id));

        T item;
        try {
            item = source.execute(queryBuilder.getQuery().limit(0, 1));
        } catch (Throwable e) {
            LOG.error("Failed to load item. Try to use new instance instead.", e);
            item = getDefault();
        }
        if (item == null) {
            LOG.warn("Failed to load data with id {}", id);
            return;
        }
        // add to the data cache
        data.put(item.getId(), item);
        LOG.trace("{}.loadById({}) -> done", classAccessor.getClass().getSimpleName(), id);
    }

    /**
     * Re-apply all filters to the {@link #queryBuilder}. If it's filterable.
     */
    protected void applyFilters() {
        LOG.trace("{}.applyFilters()", classAccessor.getClass().getSimpleName());
        // TODO we can optimize and do not set those filter twice .. 

        if (queryBuilder instanceof DataQueryBuilder.Filterable) {
            // ok continue
        } else {
            LOG.warn("Can not set filters on nonfilterable query builder. The filters are ignored.");
            return;
        }

        final DbQueryBuilder.Filterable filtrableBuilder = (DbQueryBuilder.Filterable) queryBuilder;

        // clear filters and build news
        filtrableBuilder.claerFilters();
        // add filters
        for (Filter filter : filters) {
            filtrableBuilder.addFilter(filter);
        }
        // add core filters if eqists
        if (coreFilters != null) {
            for (Filter filter : coreFilters) {
                filtrableBuilder.addFilter(filter);
            }
        }
    }

    /**
     * Add items to the cache, if there are collisions between new and old data
     * in ID then the old data are replaced.
     * 
     * @param items
     * @param startIndex
     */
    void add(List<T> items, int startIndex) {
        int index = startIndex;
        for (T item : items) {
            addToCache(item, index);
            ++index;
        }
    }

    /**
     * Return list of used core filters, do not modify the returned list! The
     * core filters are not modifiable by using other {@link DbCachedSource} methods.
     * 
     * @return list of used core filters
     */
    public List<Filter> getFiltersCore() {
        return coreFilters;
    }

    @Override
    public int size() {
        if (size == null) {
            // reload size
            size = loadSize();
        }
        return size;
    }

    @Override
    public T getObject(Long id) {
        if (data.containsKey(id)) {
            // the data are already cached
        } else {
            LOG.trace("getObject({}) - non, cached", id);
            loadById(id);
        }
        return data.get(id);
    }

    @Override
    public T getObjectByIndex(int index) {
        LOG.trace("{}.getObjectByIndex({})", classAccessor.getClass().getSimpleName(), index);
        if (dataIndexes.containsKey(index)) {
            // we have the mapping index -> id
            LOG.trace("{}.getObjectByIndex({}) -> cached", classAccessor.getClass().getSimpleName(), index);
            return getObject(dataIndexes.get(index));
        } else {
            // cache whole page
            getItemIds(index, pageSize);
            // return required object
            if (dataIndexes.containsKey(index)) {
                return getObject(dataIndexes.get(index));
            }
            LOG.warn("Failed to cache line {} in group ({}, {})",
                    index, index, pageSize);
            // try to load directely 
            T item = loadByIndex(index);
            if (item != null) {
                // add to caches
                addToCache(item, index);
            }
            // return new item .. can be null
            LOG.trace("{}.getObjectByIndex({}) -> loaded", classAccessor.getClass().getSimpleName(), index);
            return item;
        }
    }

    @Override
    public boolean containsId(Long id) {
        if (data.containsKey(id)) {
            return true;
        }
        LOG.debug("containsId called on non-cached data .. this generates the query into database");
        // try to load that object
        loadById(id);
        // ask again		
        return data.containsKey(id);
    }

    @Override
    public List<Long> getItemIds(int startIndex, int numberOfItems) {
        LOG.trace("{}.getItemIds({}, {})", classAccessor.getClass().getSimpleName(), startIndex, numberOfItems);

        List<Long> result = new ArrayList<>(numberOfItems);
        // first try to load data from cache
        int endIndex = startIndex + numberOfItems;
        for (int index = startIndex; index < endIndex; ++index) {
            if (dataIndexes.containsKey(index)) {
                // we have mapping, so use it to return the index
                result.add(dataIndexes.get(index));
            } else {
                // some data are mising, we have to load them
                final int toLoad = numberOfItems - (index - startIndex);
                List<T> newData = loadByIndex(index, toLoad);
                // gather IDs and add data to caches
                List<Long> newIDs = new ArrayList<>(numberOfItems);
                for (T item : newData) {
                    addToCache(item, index);
                    index++;
                    newIDs.add(item.getId());
                }
                // add new IDs to the result list
                result.addAll(newIDs);
                break;
            }
        }

        if (data.size() > CACHE_MAX_SIZE) {
            LOG.debug("Cache cleared");
            // we preserve indexes as we may need to use them 
            // for some direct access

            // what we remove are the data .. if they are not in result one
            List<Long> ids = new ArrayList<>(data.keySet());
            ids.removeAll(result);
            // now in ids, are ids to remove ..
            for (Long item : ids) {
                data.remove(item);
            }

            // this may result in additional query for the data we drop
            // maybe we can try to be smarter here ...
        }

        LOG.trace("{}.getItemIds({}, {}) -> done", classAccessor.getClass().getSimpleName(), startIndex, numberOfItems);
        return result;
    }

    @Override
    public int indexOfId(Long itemId) {
        for (Integer index : dataIndexes.keySet()) {
            if (dataIndexes.get(index) == itemId) {
                return index;
            }
        }
        throw new RuntimeException(Messages.getString("DbCachedSource.index.fail"));
    }

    @Override
    public ClassAccessor<T> getClassAccessor() {
        return classAccessor;
    }

    @Override
    public void addFilter(Container.Filter filter) {
        filters.add(filter);
        // and invalidate data
        invalidate();
    }

    @Override
    public void removeFilter(Container.Filter filter) {
        filters.remove(filter);
        // and invalidate data
        invalidate();

    }

    @Override
    public void removeAllFilters() {
        filters.clear();
        // and invalidate data
        invalidate();
    }

    @Override
    public Collection<Container.Filter> getFilters() {
        return filters;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (queryBuilder instanceof DataQueryBuilder.Sortable) {
            // ok continue
        } else {
            LOG.warn("Call of sort(Objet[], boolean[]) on non sortable-source ignored.");
            return;
        }

        final DbQueryBuilder.Sortable sortableBuilder = (DbQueryBuilder.Sortable) queryBuilder;

        switch (propertyId.length) {
            case 0: // remove sort
                sortableBuilder.sort(null, false);
                invalidate();
                break;
            default:
                LOG.warn("sort(Objet[], boolean[]) called with multiple targets."
                        + " Only first used others are ignored.");
            case 1: // sort, but we need expresion for sorting first
                sortableBuilder.sort((String) propertyId[0], ascending[0]);
                invalidate();
                break;
        }
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

}
