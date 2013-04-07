package cz.cuni.xrg.intlib.commons.app.pipeline.event;

import cz.cuni.xrg.intlib.commons.app.pipeline.Pipeline;
import cz.cuni.xrg.intlib.commons.event.ProcessingContext;

//import at.punkt.lodms.impl.ETLPipelineImpl;

/**
 * Published when a component {@link Extract}, {@link Transform},
 * {@link Load} requests cancellation of the pipeline through
 * {@link ProcessingContext#cancelPipeline(java.lang.String)}.<br/>
 * The pipeline exits directly after this event is published.
 *
 * @see ProcessingContext#cancelPipeline(java.lang.String)
 * @author Alex Kreiser (akreiser@gmail.com)
 */
public class PipelineAbortedEvent extends PipelineEvent {

    private final String message;

    public PipelineAbortedEvent(String message, Pipeline pipeline, String id, Object source) {
        super(pipeline, id, source);
        this.message = message;
    }

    /**
     * Returns the cancellation message
     *
     * @return
     */
    public String getMessage() {
        return message;
    }
}
