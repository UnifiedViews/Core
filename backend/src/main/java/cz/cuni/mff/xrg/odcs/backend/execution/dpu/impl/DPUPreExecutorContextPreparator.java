package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextException;
import cz.cuni.mff.xrg.odcs.backend.context.ContextFacade;
import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Examine the {@link DependencyGraph} for given {@link PipelineExecution}. Add
 * data from precedents' context to the context of the current DPU, that is
 * specified by {@link Node}.
 * We execute this only for {@link DPUExecutionState#PREPROCESSING} state as for any other state the context has been already prepared.
 * 
 * @author Petyr
 */
@Component
class DPUPreExecutorContextPreparator extends DPUPreExecutorBase {

    /**
     * Pre-executor order.
     */
    public static final int ORDER = 0;

    /**
     * Event publisher used to publish error event.
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ContextFacade contextFacade;

    public DPUPreExecutorContextPreparator() {
        super(Arrays.asList(DPUExecutionState.PREPROCESSING));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * In case of error log the error, publish message and the return false.
     */
    @Override
    protected boolean execute(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo) {
        // get current context
        Context context = contexts.get(node);

        // ! ! ! !
        // the context can contains data from previous 
        // PREPROCESSING phase that has been interrupted
        // so some DataUnit can already been created and may contains some
        // data .. we solve this in contextFacade.merge
        // which take care about this

        // looks for edges that lead to our node
        List<Edge> edges = new ArrayList<>(execution.getPipeline().getGraph().getEdges());
        Collections.sort(edges, new Comparator<Edge>() {

            @Override
            public int compare(Edge o1, Edge o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        for (Edge edge : edges) {
            if (edge.getTo() == node) {
                // we are the target .. add data
                Node sourceNode = edge.getFrom();
                Context sourceContext = contexts.get(sourceNode);
                if (sourceContext == null) {
                    // publish message
                    eventPublisher.publishEvent(
                            DPUEvent.createPreExecutorFailed(context, this, Messages.getString("DPUPreExecutorContextPreparator.missing.context", sourceNode.getDpuInstance().getName())));
                    return false;
                }
                // else add data
                try {
                    contextFacade.merge(context, sourceContext, edge.getScript());
                } catch (ContextException e) {
                    eventPublisher.publishEvent(
                            DPUEvent.createPreExecutorFailed(context, this,
                                    Messages.getString("DPUPreExecutorContextPreparator.merge.failed"), e));
                    return false;
                }
            }
        }
        return true;
    }

}
