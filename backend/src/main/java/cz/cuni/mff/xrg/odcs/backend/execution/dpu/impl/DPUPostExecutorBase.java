package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPostExecutor;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.ExecutedNode;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Extended base implementation {@link DPUPostExecutor}. Offers execution only for
 * given {@link DPUExecutionState}s.
 * Put the post-executor code into {@link #execute(Node, Map, Object, PipelineExecution, ProcessingUnitInfo)} method.
 * 
 * @author Petyr
 */
abstract class DPUPostExecutorBase implements DPUPostExecutor {

    /**
     * Contains states on which this execution will be executed, other states
     * are ignored.
     */
    private final List<DPUExecutionState> executionStates;

    /**
     * @param executionStates
     *            List of {@link DPUExecutionState} on which run {@link #execute(Node, Map, Object, PipelineExecution, ProcessingUnitInfo)}
     */
    public DPUPostExecutorBase(List<DPUExecutionState> executionStates) {
        this.executionStates = executionStates;
    }

    @Override
    public boolean postAction(ExecutedNode node,
            Map<ExecutedNode, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo) {
        // shall we execute ?
        if (executionStates.contains(unitInfo.getState())) {
            return execute(node, contexts, dpuInstance, execution, unitInfo);
        } else {
            return true;
        }
    }

    /**
     * Execute executor's code.
     * 
     * @param node
     * @param contexts
     * @param dpuInstance
     * @param execution
     * @param unitInfo
     * @return
     */
    protected abstract boolean execute(ExecutedNode node,
            Map<ExecutedNode, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo);

}
