package cz.cuni.mff.xrg.odcs.frontend.doa.container;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Provide base implementation for {@link ClassAccessor}.
 * 
 * @author Petyr
 * @param <T>
 */
public class ClassAccessorBase<T extends DataObject> implements ClassAccessor<T> {

    /**
     * Holds information about single column.
     * 
     * @param <U>
     */
    public class Column<U> {

        /**
         * Caption of column shown to user.
         */
        final String caption;

        /**
         * Column's data type.
         */
        final Class<U> type;

        /**
         * Value getter.
         */
        final ColumnGetter<U> columnGetter;

        /**
         * Register non-filterable/sortable column. The data are obtained by
         * columnGetter and not by {@link #getValue(cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject, java.lang.String)}.
         * 
         * @param clazz
         *            column data type
         * @param caption
         *            column caption
         * @param columnGetter
         *            getter used to get value
         */
        public Column(Class<U> clazz, String caption, ColumnGetter<U> columnGetter) {
            this.caption = caption;
            this.type = clazz;
            this.columnGetter = columnGetter;
        }

    }

    /**
     * Class used to access the data in given column.
     * 
     * @param <U>
     */
    public abstract class ColumnGetter<U> {

        /**
         * Get the information of type U from object.
         * 
         * @param object
         * @return information of type U from object
         */
        public abstract U get(T object);

    }

    private static final Logger LOG = LoggerFactory.getLogger(ClassAccessorBase.class);

    /**
     * Store informations about columns.
     */
    private final Map<String, Column<?>> columns = new HashMap<>();

    /**
     * List of all column names.
     */
    private final List<String> all = new LinkedList<>();

    /**
     * List of sortable columns.
     */
    private final List<String> sortable = new LinkedList<>();

    /**
     * List of filterable.
     */
    private final List<String> filterable = new LinkedList<>();

    /**
     * List of visible columns.
     */
    private final List<String> visible = new LinkedList<>();

    /**
     * List of columns to fetch.
     */
    private final List<String> toFetch = new LinkedList<>();

    /**
     * Entity class.
     */
    private final Class<T> entityClass;

    /**
     * Constructor.
     * 
     * @param entityClass
     *            Class of the data objects.
     */
    public ClassAccessorBase(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Add visible, sortable and filter able column.
     * 
     * @param <U>
     * @param clazz
     *            Column name and caption.
     * @param name
     * @param getter
     */
    public <U> void add(Class<U> clazz, String name, ColumnGetter<U> getter) {
        add(clazz, name, name, true, true, getter);
    }

    /**
     * Add visible, sortable and filter able column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param caption
     * @param getter
     */
    public <U> void add(Class<U> clazz, String name, String caption, ColumnGetter<U> getter) {
        add(clazz, name, caption, true, true, getter);
    }

    /**
     * Add visible column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param caption
     * @param sorting
     * @param filtering
     * @param getter
     */
    public <U> void add(Class<U> clazz, String name, String caption, boolean sorting, boolean filtering, ColumnGetter<U> getter) {
        add(clazz, name, caption, sorting, filtering, true, getter);
    }

    /**
     * Add visible, non-sortable and non-filterable visible column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param getter
     */
    public <U> void addNon(Class<U> clazz, String name, ColumnGetter<U> getter) {
        add(clazz, name, name, false, false, getter);
    }

    /**
     * Add visible, non-sortable and non-filterable visible column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param caption
     * @param getter
     */
    public <U> void addNon(Class<U> clazz, String name, String caption, ColumnGetter<U> getter) {
        add(clazz, name, caption, false, false, getter);
    }

    /**
     * Add invisible non-sortable and non-filterable column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param getter
     */
    public <U> void addInvisible(Class<U> clazz, String name, ColumnGetter<U> getter) {
        addInvisible(clazz, name, getter, false, false);
    }

    /**
     * Add invisible column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param getter
     * @param sorting
     * @param filtering
     */
    public <U> void addInvisible(Class<U> clazz, String name, ColumnGetter<U> getter, boolean sorting, boolean filtering) {
        add(clazz, name, name, sorting, filtering, false, getter);
    }

    /**
     * Add given column to the fetch list. Only direct non trivial column
     * can be fetched.
     * Fetching column will result in eager loading on this column so there
     * will be no additional query as the data will be loaded in single query.
     * 
     * @param name
     */
    public void fetch(String name) {
        toFetch.add(name);
    }

    /**
     * Add column.
     * 
     * @param <U>
     * @param clazz
     * @param name
     * @param caption
     * @param sorting
     * @param filtering
     * @param visible
     * @param getter
     */
    private <U> void add(Class<U> clazz, String name, String caption, boolean sorting, boolean filtering, boolean visible, ColumnGetter<U> getter) {
        columns.put(name, new Column<>(clazz, caption, getter));
        all.add(name);
        if (sorting) {
            this.sortable.add(name);
        }
        if (filtering) {
            this.filterable.add(name);
        }
        if (visible) {
            this.visible.add(name);
        }
    }

    @Override
    public List<String> all() {
        return all;
    }

    @Override
    public List<String> sortable() {
        return sortable;
    }

    @Override
    public List<String> filterable() {
        return filterable;
    }

    @Override
    public List<String> visible() {
        return visible;
    }

    @Override
    public List<String> toFetch() {
        return toFetch;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public String getColumnName(String id) {
        return columns.get(id).caption;
    }

    @Override
    public Object getValue(T object, String id) {
        final ColumnGetter getter = columns.get(id).columnGetter;
        if (getter == null) {
            // missing
            LOG.error("Mising getter for column {}", id);
            return null;
        } else {
            return getter.get(object);
        }
    }

    @Override
    public Class<?> getType(String id) {
        return columns.get(id).type;
    }

}
