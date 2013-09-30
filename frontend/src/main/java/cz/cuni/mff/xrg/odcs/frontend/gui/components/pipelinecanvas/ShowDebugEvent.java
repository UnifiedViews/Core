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
	
	private Pipeline pipeline;
	private Node debugNode;
	
	
	/**
	 * Default constructor. Inform, that debug was requested for given {@link Pipeline} and
	 * {@link Node}.
	 *
	 * @param source Source component.
	 * @param pipeline {@link Pipeline} to debug.
	 * @param debugNode {@link Node} where debug should end.
	 */
	public ShowDebugEvent(Component source, Pipeline pipeline, Node debugNode) {
		super(source);	
		this.pipeline = pipeline;
		this.debugNode = debugNode;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}
	
	public Node getDebugNode() {
		return debugNode;
	}
}
