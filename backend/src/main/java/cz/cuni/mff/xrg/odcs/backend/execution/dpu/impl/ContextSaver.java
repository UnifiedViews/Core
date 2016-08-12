package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextFacade;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPostExecutor;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * Save the context after DPU execution.
 * 
 * @author Petyr
 */
@Component
public class ContextSaver implements DPUPostExecutor {

    public static final int ORDER = 10000;

    @Autowired
    private ContextFacade contextFacade;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean postAction(Node node, Map<Node, Context> contexts, Object dpuInstance, PipelineExecution execution, ProcessingUnitInfo unitInfo) {
        // get the context
        Context context = contexts.get(node);
        // save it
        contextFacade.save(context);
        // and return true
        return true;
    }

}
