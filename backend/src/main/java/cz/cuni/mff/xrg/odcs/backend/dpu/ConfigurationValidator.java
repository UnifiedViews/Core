package cz.cuni.mff.xrg.odcs.backend.dpu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import cz.cuni.mff.xrg.odcs.commons.app.communication.EmailSender;

/**
 * As a component periodically ask database for DPUs with possibly invalid
 * configuration.
 * At the end of the check round .. send messages to the owners of DPUs
 * which has invalid configuration.
 * NOT IMPLEMENTED YET
 * 
 * @author Petyr
 */
public class ConfigurationValidator implements ApplicationListener<ApplicationEvent> {

    /**
     * Component used to send email.
     */
    @Autowired
    private EmailSender emailSender;

    /**
     * Facade for access to DPUs.
     */
    @Autowired
    private cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade dpuFacade;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }

}
