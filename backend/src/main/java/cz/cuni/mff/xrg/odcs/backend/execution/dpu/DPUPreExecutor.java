package cz.cuni.mff.xrg.odcs.backend.execution.dpu;

import java.util.Map;

import org.springframework.core.Ordered;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Provide action that should be perform before every DPU execution. Must not
 * execute the DPU. T
 * The {@link DPUPreExecutor}s are used as a singletons, so they
 * must be able to run concurrently on multiple instances.
 * The PreExecutors are executed in order that is defined by {@link Ordered} As the {@link DPUPreExecutor}s are executed on every DPU .. even on
 * those that have been finished previously (pause/resume .. or backend
 * has been shutdown) .. they should not modify the content. If they
 * do please filter their usage for non {@link DPUExecutionState#FINISHED} state.
 * The willExecute carry the authoritative information about execution,
 * but the PreExecutors should rather use {@link DPUExecutionState} to determine if run or not.
 * 
 * @author Petyr
 */
public interface DPUPreExecutor extends Ordered {

    /**
     * Should perform pre-execution actions. If return false then the execution
     * is cancelled. In such case the PreExutor should publish respective {@link cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent} with problem description.
     * 
     * @param node
     *            Node that will be executed.
     * @param contexts
     *            List of context, also contain context for current node.
     * @param dpuInstance
     *            DPU instance.
     * @param execution
     *            Respective execution.
     * @param unitInfo
     *            DPU's ProcessingUnitInfo.
     * @param willExecute
     *            False it the DPU will not be executed.
     * @return False if the pre-executor failed.
     */
    public boolean preAction(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo,
            boolean willExecute);

}
