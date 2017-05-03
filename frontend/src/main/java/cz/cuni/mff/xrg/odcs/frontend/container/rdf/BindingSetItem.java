package cz.cuni.mff.xrg.odcs.frontend.container.rdf;

import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Implementation of {@link Item} interface for underlying {@link BindingSet} object.
 * 
 * @author Bogo
 */
public class BindingSetItem implements Item {

    private BindingSet binding;

    private List<String> headers;

    private int id;

    /**
     * Constructor.
     * 
     * @param headers
     *            List of headers.
     * @param binding
     *            Binding.
     * @param id
     *            Id.
     */
    public BindingSetItem(List<String> headers, BindingSet binding, int id) {
        this.headers = headers;
        this.binding = binding;
        this.id = id;
    }

    @Override
    public Property getItemProperty(Object id) {
        if ("id".equals(id)) {
            return new ObjectProperty(this.id);
        }
        if (id.getClass() != String.class) {
            return null;
        }
        String sId = (String) id;
        if (binding.hasBinding(sId)) {
            return new ObjectProperty(binding.getValue(sId));
        }
        return null;
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return headers;
    }

    @Override
    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Messages.getString("BindingSetItem.add.exception"));
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Messages.getString("BindingSetItem.remove.exception"));
    }
}
