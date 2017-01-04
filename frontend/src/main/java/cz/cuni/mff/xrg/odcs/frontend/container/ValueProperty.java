package cz.cuni.mff.xrg.odcs.frontend.container;

import com.vaadin.data.Property;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

/**
 * Holds property which is associated with single object.
 * 
 * @author Petyr
 * @param <T>
 */
class ValueProperty<V, T extends DataObject> implements Property<V> {

    private final GeneralProperty<V, T> generalProperty;

    private final T object;

    ValueProperty(GeneralProperty<V, T> generalProperty, Long id) {
        this.generalProperty = generalProperty;
        this.object = generalProperty.container.getObject(id);
        if (this.object == null) {

        }
    }

    @Override
    public V getValue() {
        return generalProperty.getValueForObject(object);
    }

    @Override
    public void setValue(V newValue)
            throws com.vaadin.data.Property.ReadOnlyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends V> getType() {
        return generalProperty.getType();
    }

    @Override
    public boolean isReadOnly() {
        return generalProperty.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean newStatus) {
        throw new UnsupportedOperationException();
    }

}
