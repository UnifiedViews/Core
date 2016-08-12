package cz.cuni.mff.xrg.odcs.commons.app.module.event;

/**
 * Event indicate that DPU from given directory should be uninstalled from
 * the system.
 * 
 * @author Petyr
 */
public class ModuleDeleteEvent extends ModuleEvent {

    /**
     * @param source
     *            Event source.
     * @param directoryName
     *            DPU's directory.
     */
    public ModuleDeleteEvent(Object source, String directoryName) {
        super(source, directoryName);
    }

}
