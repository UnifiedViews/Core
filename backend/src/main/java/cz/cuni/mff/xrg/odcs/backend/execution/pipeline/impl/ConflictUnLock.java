package cz.cuni.mff.xrg.odcs.backend.execution.pipeline.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.execution.pipeline.PostExecutor;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Unlock pipelines that were lock in {@link ConflictLock}.
 * 
 * @author Petyr
 */
@Component
public class ConflictUnLock implements PostExecutor {

    /**
     * The main part of component.
     */
    @Autowired
    private ConflictLock conflictLock;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean postAction(PipelineExecution execution,
            Map<Node, Context> contexts,
            DependencyGraph graph) {
        // just call unlock on ConflictLock
        conflictLock.unlock(execution);

        return true;
    }

}
