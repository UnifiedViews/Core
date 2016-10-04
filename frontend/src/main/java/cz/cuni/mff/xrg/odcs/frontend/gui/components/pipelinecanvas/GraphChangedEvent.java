package cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

/**
 * Canvas event informing that the pipeline graph has changed.
 * 
 * @author Bogo
 */
public class GraphChangedEvent extends Event {

    private boolean isUndo;

    /**
     * Default constructor.
     * 
     * @param source
     *            Source component.
     * @param isUndoable
     *            Whether or not is given graph change undoable. (Undo
     *            button is enabled if it is.)
     */
    public GraphChangedEvent(Component source, boolean isUndoable) {
        super(source);
        isUndo = isUndoable;
    }

    /**
     * If the change is undo able.
     * 
     * @return If the change is undo able
     */
    public boolean isUndoable() {
        return isUndo;
    }
}
