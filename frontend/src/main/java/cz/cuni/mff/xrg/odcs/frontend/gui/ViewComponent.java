package cz.cuni.mff.xrg.odcs.frontend.gui;

import com.vaadin.navigator.View;
import com.vaadin.ui.CustomComponent;

/**
 * Base abstract class for views. Provide functionality like CustomComponent but
 * also include support for {@link com.vaadin.navigator.Navigator} class.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
public abstract class ViewComponent extends CustomComponent implements View, ModifiableComponent {

    /**
     * Default dummy implementation of modification status.
     * 
     * @return always not modified
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Default dummy implementation of save handler.
     * 
     * @return always success
     */
    @Override
    public boolean saveChanges() {
        return true;
    }
}
