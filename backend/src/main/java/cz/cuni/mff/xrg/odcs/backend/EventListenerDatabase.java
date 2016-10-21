package cz.cuni.mff.xrg.odcs.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineEvent;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;

/**
 * Store all DPURecord and Pipeline related events into database.
 * 
 * @author Petyr
 */
public class EventListenerDatabase implements ApplicationListener<ApplicationEvent> {

    @Autowired
    private DPUFacade dpuFacade;

    /**
     * Take care about DPUEvent. Store event into database.
     * 
     * @param event
     */
    private void onDPUEvent(DPUEvent event) {
        MessageRecord record = event.getRecord();
        dpuFacade.save(record);
    }

    /**
     * Take care about PipelineEvent. Store event into database
     * 
     * @param event
     */
    private void onPipelineEvent(PipelineEvent event) {
        MessageRecord record = event.getRecord();
        dpuFacade.save(record);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // base on type .. 
        if (event instanceof DPUEvent) {
            onDPUEvent((DPUEvent) event);
        } else if (event instanceof PipelineEvent) {
            onPipelineEvent((PipelineEvent) event);
        } else {
            // unknown event ..
        }
    }

}
