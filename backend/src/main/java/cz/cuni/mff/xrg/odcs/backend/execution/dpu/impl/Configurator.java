package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPreExecutor;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;

/**
 * Load configuration into DPU.
 * If the DPU does not implements {@link DPUConfigurable} interface immediately
 * return true.
 * Executed for every state.
 * 
 * @author Petyr
 */
@Component
class Configurator implements DPUPreExecutor {

    public static final int ORDER = AnnotationsInput.ORDER + 1000;

    private static final Logger LOG = LoggerFactory
            .getLogger(Configurator.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public int getOrder() {
        // execute after ContextPreparator
        return DPUPreExecutorContextPreparator.ORDER + 10;
    }

    @Override
    public boolean preAction(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo,
            boolean willExecute) {
        // get current context and DPUInstanceRecord
        Context context = contexts.get(node);
        DPUInstanceRecord dpu = node.getDpuInstance();

        if (dpuInstance instanceof DPUConfigurable) {
            // can be configured
        } else {
            // do not configure
            LOG.debug("DPU {} is not configurable.", dpu.getName());
            return true;
        }
        @SuppressWarnings("unchecked")
        DPUConfigurable configurable = (DPUConfigurable) dpuInstance;
        try {
            String conf = dpu.isUseTemplateConfig() ? dpu.getTemplate().getRawConf() : dpu.getRawConf();
            configurable.configure(conf);

            LOG.debug("DPU {} has been configured.", dpu.getName());
        } catch (DPUConfigException e) {
            eventPublisher.publishEvent(DPUEvent.createPreExecutorFailed(
                    context, this, Messages.getString("Configurator.configuration.fail"), e));
            // stop the execution
            return false;
        } catch (Throwable t) {
            eventPublisher.publishEvent(DPUEvent.createPreExecutorFailed(
                    context, this, Messages.getString("Configurator.configException"), t));
            // stop the execution
            return false;
        }
        // continue execution
        return true;
    }

}
