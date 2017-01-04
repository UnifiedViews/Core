package cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Event for debug request on {@link PipelineCanvas}.
 * 
 * @author Bogo
 */
public class ShowDebugEvent extends Event {
    private Node debugNode;

    /**
     * Default constructor. Inform, that debug was requested for current {@link Pipeline} and given {@link Node}.
     * 
     * @param source
     *            Source component.
     * @param debugNode
     *            {@link Node} where debug should end.
     */
    public ShowDebugEvent(Component source, Node debugNode) {
        super(source);
        this.debugNode = debugNode;
    }

    /**
     * Get debug node.
     * 
     * @return debug node
     */
    public Node getDebugNode() {
        return debugNode;
    }
}
