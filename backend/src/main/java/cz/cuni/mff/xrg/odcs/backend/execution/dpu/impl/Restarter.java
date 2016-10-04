package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Reset the DPU state from {@link DPUExecutionState#RUNNING} to {@link DPUExecutionState#PREPROCESSING}.
 * Executed only for {@link DPUExecutionState#RUNNING}.
 * 
 * @author Petyr
 */
@Component
public class Restarter extends DPUPreExecutorBase {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

    private static final Logger LOG = LoggerFactory.getLogger(Restarter.class);

    @Autowired
    private ResourceManager resourceManager;

    public Restarter() {
        super(Arrays.asList(DPUExecutionState.RUNNING));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    protected boolean execute(Node node, Map<Node, Context> contexts,
            Object dpuInstance, PipelineExecution execution,
            ProcessingUnitInfo unitInfo) {
        LOG.info("Restarting DPU from RUNNING -> PREPROCESSING");
        unitInfo.setState(DPUExecutionState.PREPROCESSING);
        // get current context
        Context context = contexts.get(node);
        // we delete data from output dataUnits
        for (ManagableDataUnit dataUnit : context.getOutputs()) {
            try {
                dataUnit.clear();
            } catch (DataUnitException ex) {
                LOG.error("Can't clear data unit.", ex);
            }
        }
        // we also have to delete DPU's temporary directory
        try {
            final File dpuTmpDir = resourceManager.getDPUWorkingDir(execution, node.getDpuInstance());
            LOG.debug("Deleting: {}", dpuTmpDir.toString());
            FileUtils.deleteDirectory(dpuTmpDir);
        } catch (IOException | MissingResourceException e) {
            LOG.warn("Can't delete directory after execution", e);
        }

        // all is done .. we can start DPU .. 

        return true;
    }

}
