package cz.cuni.mff.xrg.odcs.backend.execution.pipeline.impl;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextFacade;
import cz.cuni.mff.xrg.odcs.backend.execution.pipeline.PostExecutor;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.dataunit.relational.RelationalRepositoryManager;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.ExecutedNode;
import cz.cuni.mff.xrg.odcs.commons.app.rdf.RepositoryManager;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import eu.unifiedviews.commons.rdf.repository.RDFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

/**
 * CleanUp data after execution.
 * 
 * @author Petyr
 */
@Component
class CleanUp implements PostExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CleanUp.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ContextFacade contextFacade;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private RelationalRepositoryManager relationalRepositoryManager;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean postAction(PipelineExecution execution,
            Map<ExecutedNode, Context> contexts,
            DependencyGraph graph) {
        LOG.debug("CleanUp start .. ");

        try {
            this.relationalRepositoryManager.release(execution.getContext().getExecutionId());
        } catch (Exception e) {
            LOG.error("Failed to release relational repository", e);
        }

        // first release contexts
        for (Context item : contexts.values()) {
            if (execution.isDebugging()) {
                // close the data unit
                // the data has already been saved 
                // in DPU post executor after the DPU's execution
                contextFacade.close(item);
            } else {
                // delete data ..
                // but preserve context info as it can be used to examine the 
                // execution
                contextFacade.delete(item, true);
            }
        }
        if (execution.isDebugging()) {
//            rdfDataUnitFactory.release(execution.getContext().generatePipelineId());

            try {
                repositoryManager.release(execution.getContext().getExecutionId());
            } catch (RDFException ex) {
                LOG.error("Can't release repository.", ex);
            }
        } else {
//            rdfDataUnitFactory.clean(execution.getContext().generatePipelineId());

            try {
                repositoryManager.delete(execution.getContext().getExecutionId());
            } catch (RDFException ex) {
                LOG.error("Can't delete repository.", ex);
            }
        }

        // prepare execution root
        File rootDir = new File(
                appConfig.getString(ConfigProperty.GENERAL_WORKINGDIR));

        // get access to the infromation in execution context
        ExecutionInfo info = new ExecutionInfo(execution.getContext());

        if (!execution.isDebugging()) {
            // delete working directory the sub directories should be already deleted by DPU's.
            try {
                CleanupUtils.deleteDirectory(this.resourceManager.getExecutionDir(execution));
            } catch (MissingResourceException ex) {
                LOG.warn("Can't delete directory.", ex);
            }
        }

        // delete result, storage if empty
        try {
            CleanupUtils.deleteDirectoryIfEmpty(this.resourceManager.getExecutionWorkingDir(execution));
        } catch (MissingResourceException ex) {
            LOG.warn("Can't delete directory.", ex);
        }
        try {
            CleanupUtils.deleteDirectoryIfEmpty(this.resourceManager.getExecutionStorageDir(execution));
        } catch (MissingResourceException ex) {
            LOG.warn("Can't delete directory.", ex);
        }
        try {
            CleanupUtils.deleteDirectoryIfEmpty(this.resourceManager.getExecutionDir(execution));
        } catch (MissingResourceException ex) {
            LOG.warn("Can't delete directory.", ex);
        }

        LOG.debug("CleanUp has been finished .. ");
        return true;
    }

}
