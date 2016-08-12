package cz.cuni.mff.xrg.odcs.frontend.auxiliaries;

import java.io.FileNotFoundException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeCompiler;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUInstanceWrap;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.vaadin.AbstractConfigDialog;

/**
 * @author Bogo
 */
public class PipelineValidator {

    @Autowired
    private DPUExplorer dpuExplorer;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Utils utils;

    private static final Logger LOG = LoggerFactory.getLogger(PipelineValidator.class);

    /**
     * Validate edges of graph.
     *
     * @param graph
     *            Graph to validate.
     * @return Is graph valid.
     * @throws cz.cuni.mff.xrg.odcs.frontend.auxiliaries.PipelineValidator.PipelineValidationException
     */
    public boolean validateGraphEdges(PipelineGraph graph) throws PipelineValidationException {
        EdgeCompiler edgeCompiler = new EdgeCompiler();
        String result = edgeCompiler.checkMandatoryInputsAndOutputs(graph, dpuExplorer);
        if (result != null) {
            LOG.debug("Mandatory input/output check FAILED for following: " + result);
            throw new PipelineValidationException(result);
        }
        return true;
    }

    /**
     * Validate graph.
     *
     * @param graph
     *            Graph to validate.
     * @return Is graph valid.
     */
    public boolean validateGraph(PipelineGraph graph) {
        boolean isGraphValid = true;
        for (Node node : graph.getNodes()) {
            DPUInstanceRecord dpu = node.getDpuInstance();
            boolean isValid = checkDPUValidity(dpu);
            isGraphValid &= isValid;
        }
        try {
            return isGraphValid & validateGraphEdges(graph);
        } catch (PipelineValidationException ex) {
            return false;
        }
    }

    /**
     * Validate DPU.
     *
     * @param dpu
     *            DPU to validate.
     * @return Is DPU valid.
     */
    public boolean checkDPUValidity(DPUInstanceRecord dpu) {
        LOG.debug("DPU mandatory fields check starting for DPU: " + dpu.getName());

        DPUInstanceWrap dpuInstance = new DPUInstanceWrap(dpu, dpuFacade, Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE)),
                appConfig, this.utils.getUser());

        // load instance
        AbstractConfigDialog<?> confDialog;
        try {
            confDialog = dpuInstance.getDialog();
            if (confDialog == null) {
            } else {
                // configure
                dpuInstance.configuredDialog();
            }
            dpuInstance.saveConfig();
        } catch (ModuleException | FileNotFoundException | DPUWrapException | DPUConfigException e) {
            LOG.debug("DPU mandatory fields check FAILED for DPU: " + dpu.getName());
            return false;
        }
        LOG.debug("DPU mandatory fields check OK for DPU: " + dpu.getName());
        return true;
    }

    /**
     * Exception in pipeline validation.
     */
    public class PipelineValidationException extends Exception {
        /**
         * Constructor.
         *
         * @param report
         *            Report from validation.
         */
        public PipelineValidationException(String report) {
            super(report);
        }
    }
}
