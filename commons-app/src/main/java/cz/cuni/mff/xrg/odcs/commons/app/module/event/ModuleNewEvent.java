package cz.cuni.mff.xrg.odcs.commons.app.module.event;

/**
 * Event indicate that there is new directory in DPU's directory. So there
 * is possibility that new DPU has been loaded into system.
 * 
 * @author Petyr
 */
public class ModuleNewEvent extends ModuleEvent {

    /**
     * @param source
     *            Event source.
     * @param directoryName
     *            DPU's directory name.
     */
    public ModuleNewEvent(Object source, String directoryName) {
        super(source, directoryName);
    }

}
