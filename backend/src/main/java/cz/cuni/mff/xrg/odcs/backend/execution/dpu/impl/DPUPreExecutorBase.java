package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.util.List;
import java.util.Map;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPreExecutor;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Extended base implementation {@link DPUPreExecutor}. Offers execution only for
 * given {@link DPUExecutionState}s.
 * Put the pre-executor code into {@link #execute(Node, Map, Object, PipelineExecution, ProcessingUnitInfo)} method.
 * 
 * @author Petyr
 */
public abstract class DPUPreExecutorBase implements DPUPreExecutor {

    /**
     * Contains states on which this execution will be executed, other states
     * are ignored.
     */
    private final List<DPUExecutionState> executionStates;

    /**
     * @param executionStates
     *            List of {@link DPUExecutionState} on which run {@link #execute(Node, Map, Object, PipelineExecution, ProcessingUnitInfo)}
     */
    public DPUPreExecutorBase(List<DPUExecutionState> executionStates) {
        this.executionStates = executionStates;
    }

    @Override
    public boolean preAction(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo,
            boolean willExecute) {
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
     * @return False in case of failure.
     */
    protected abstract boolean execute(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo);

}
