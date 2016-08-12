package cz.cuni.mff.xrg.odcs.backend.execution.pipeline;

import java.util.Map;

import org.springframework.core.Ordered;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Provide action that should be perform before starts the the pipeline
 * execution. Must not execute the DPU. The PreExecutors can be called in random
 * order, but they all will be called before the call of {@link Executor}.
 * The {@link PreExecutor}s are used as a singletons, so they
 * must be able to run concurrently on multiple instances.
 * The PreExecutors are executed in order that is defined by {@link Ordered}
 * 
 * @author Petyr
 */
public interface PreExecutor extends Ordered {

    /**
     * Should perform pre-execution actions. If return false then the execution
     * is cancelled. In such case it should publish instance of {@link cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineEvent} with problem description.
     * 
     * @param execution
     * @param contexts
     * @param graph
     *            Dependency graph used for execution.
     * @param success
     *            False if the execution it self will not be executed.
     * @return False if the post-executor failed.
     */
    public boolean preAction(PipelineExecution execution,
            Map<Node, Context> contexts,
            DependencyGraph graph,
            boolean success);

}
