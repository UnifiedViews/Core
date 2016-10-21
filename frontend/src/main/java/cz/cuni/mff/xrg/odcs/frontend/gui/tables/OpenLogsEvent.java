package cz.cuni.mff.xrg.odcs.frontend.gui.tables;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

/**
 * Event for passing request to show logs with preselected DPU.
 * 
 * @author Bogo
 */
public class OpenLogsEvent extends Event {

    private Long dpuId;

    /**
     * Constructor.
     * 
     * @param source
     *            Source of the event.
     * @param dpuId
     *            Id of DPU which logs should be opened.
     */
    public OpenLogsEvent(Component source, Long dpuId) {
        super(source);
        this.dpuId = dpuId;
    }

    /**
     * Get Id of DPU which logs should be opened.
     * 
     * @return Id of DPU which logs should be opened
     */
    public Long getDpuId() {
        return dpuId;
    }

}
