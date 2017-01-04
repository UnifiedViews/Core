package cz.cuni.mff.xrg.odcs.commons.app.execution.context;

import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;

/**
 * Manage context for a single execution.
 * 
 * @author Petyr
 */
public class ExecutionInfo {

    /**
     * Binded context.
     */
    private final ExecutionContextInfo executionContext;

    /**
     * Create manager class for given execution context.
     * 
     * @param executionContext
     *            Context for which create {@link ExecutionInfo}.
     */
    public ExecutionInfo(ExecutionContextInfo executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * If the context for given DPU has already been created then return it,
     * otherwise return null.
     * 
     * @param dpuInstance
     *            Respective dpu.
     * @return Information about given DPU context or null.
     */
    public DpuContextInfo dpu(DPUInstanceRecord dpuInstance) {
        if (executionContext.getContexts().containsKey(dpuInstance)) {
            return new DpuContextInfo(executionContext, dpuInstance);
        } else {
            return null;
        }
    }

    /**
     * @return DPU instances for which the execution context has been created.
     */
    public Set<DPUInstanceRecord> getDPUIndexes() {
        return executionContext.getContexts().keySet();
    }

    /**
     * Delete data from context info. The state remain unchanged.
     */
    public void clear() {
        executionContext.getContexts().clear();
    }


    public ExecutionContextInfo getExecutionContext() {
        return executionContext;
    }

}
