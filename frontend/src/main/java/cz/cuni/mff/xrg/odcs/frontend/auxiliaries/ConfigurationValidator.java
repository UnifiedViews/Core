package cz.cuni.mff.xrg.odcs.frontend.auxiliaries;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;

/**
 * Checks configuration of frontend application
 * This bean throws exception during Spring context initialization, so if application configuration
 * is not valid, application is not started
 */
public class ConfigurationValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationValidator.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private DPUFacade dpuFacade;

    private boolean dpuInstanceTemplateConfigEnabled = true;

    @PostConstruct
    public void init() throws ApplicationConfigurationException {
        try {
            this.dpuInstanceTemplateConfigEnabled = this.appConfig.getBoolean(ConfigProperty.DPU_INSTANCE_USE_TEMPLATE_CONFIG);
        } catch (MissingConfigPropertyException e) {
            // ignore, optional config
        }
        checkFrontendConfiguration();
    }

    public void checkFrontendConfiguration() throws ApplicationConfigurationException {
        if (!this.dpuInstanceTemplateConfigEnabled) {
            boolean isDPUConfiguredWithTemplate = false;
            List<DPUInstanceRecord> dpuInstances = this.dpuFacade.getAllDPUInstances();
            for (DPUInstanceRecord dpu : dpuInstances) {
                if (dpu.isUseTemplateConfig()) {
                    isDPUConfiguredWithTemplate = true;
                    break;
                }
            }
            if (isDPUConfiguredWithTemplate) {
                LOG.error("Template configuration of DPU instances is disabled in configuration but database contains "
                        + "DPU instances configured to use template configuration. "
                        + "Check all DPUs instances in all pipelines and reconfigure DPUs with template configuration "
                        + "before disabling template configuration in frontend.properties file");
                throw new ApplicationConfigurationException("Template configuration of DPU instances is disabled in configuration but database contains "
                        + "DPU instances configured to use template configuration.");
            }
        }
    }

}
