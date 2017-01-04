package cz.cuni.mff.xrg.odcs.frontend.container;

import com.vaadin.data.Property;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * This property represent a field and respective column in container.
 * 
 * @author Petyr
 * @param <T>
 */
class GeneralProperty<V, T extends DataObject> implements Property<V> {

    String fieldName;

    Class<V> type;

    ReadOnlyContainer<T> container;

    GeneralProperty(Class<V> type, String fieldName, ReadOnlyContainer<T> container) {
        // ...
        this.fieldName = fieldName;
        this.type = type;
        this.container = container;
    }

    /**
     * Create instance of this property binded for given object (id).
     * 
     * @param id
     * @return
     */
    ValueProperty<V, T> bind(Long id) {
        return new ValueProperty<>(this, id);
    }

    @SuppressWarnings("unchecked")
    V getValueForObject(T object) {
        return (V) container.getClassAccessor().getValue(object, fieldName);
    }

    @Override
    public V getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(V newValue)
            throws com.vaadin.data.Property.ReadOnlyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends V> getType() {
        return type;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setReadOnly(boolean newStatus) {
        throw new UnsupportedOperationException();
    }

}
