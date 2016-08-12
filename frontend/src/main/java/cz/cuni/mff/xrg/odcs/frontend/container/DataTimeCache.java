package cz.cuni.mff.xrg.odcs.frontend.container;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Time based cache for data. The data in cache remain valid until next call
 * of {@link #setKey(String, Date)} or until one second from last call of this
 * function elapsed.
 * 
 * @author Petyr
 * @param <T>
 *            TODO Let user set the time for which the data stays valid
 */
public class DataTimeCache<T> {

    /**
     * Data cache.
     */
    private final Map<Long, T> cache = new HashMap<>();

    /**
     * List with ids. Contains ids from {@link #cache} as we need
     * them in order.
     */
    private final List<Long> cacheIds = new LinkedList<>();

    /**
     * Time when data were added.
     */
    private Date setTime = new Date();

    /**
     * Key used to obtain data. Only if the key is same
     * as last time then data from {@link #cache} are used.
     */
    private String accessKey;

    /**
     * Invalidate data.
     */
    public void invalidate() {
        cache.clear();
        cacheIds.clear();
        accessKey = null;
    }

    /**
     * Get object from cache if exist otherwise return null.
     * 
     * @param id
     * @return object from cache if exist otherwise null
     */
    public T get(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        } else {
            return null;
        }
    }

    /**
     * Add data object into cache.
     * 
     * @param id
     * @param object
     */
    public void set(Long id, T object) {
        cache.put(id, object);
        cacheIds.add(id);
    }

    /**
     * Set new cache key. Automatically call {@link #invalidate()} method. Set
     * new cache key and last update time.
     * 
     * @param accessKey
     * @param now
     */
    public void setKey(String accessKey, Date now) {
        invalidate();
        this.accessKey = accessKey;
        this.setTime = now;
    }

    /**
     * Return true if cache has valid data for given key.
     * 
     * @param accessKey
     * @param now
     * @return true if cache has valid data for given key
     */
    public boolean isValid(String accessKey, Date now) {
        if (this.accessKey == null || accessKey == null) {
            return false;
        }

        final long elapsed = now.getTime() - setTime.getTime();
        return accessKey.compareTo(this.accessKey) == 0 && elapsed < 1000;
    }

    /**
     * Return keys of contained data.
     * 
     * @return keys of contained data
     */
    public List<Long> getKeys() {
        return cacheIds;
    }

    /**
     * Return true if the cache contains data for given id.
     * 
     * @param id
     * @return true if the cache contains data for given id
     */
    public boolean containsId(Long id) {
        return cache.containsKey(id);
    }

}
