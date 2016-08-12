package cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

/**
 * Event for closing of detail in PipelineEdit.
 * 
 * @author Bogo
 */
public class DetailClosedEvent extends Event {

    private Class detailClass;

    /**
     * Default constructor.
     * 
     * @param source
     *            Source component of the event.
     * @param klass
     *            Class of the object, which detail was closed.
     */
    public DetailClosedEvent(Component source, Class klass) {
        super(source);
        detailClass = klass;
    }

    /**
     * Get class of detail.
     * 
     * @return class of detail
     */
    public Class getDetailClass() {
        return detailClass;
    }
}
