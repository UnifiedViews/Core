package cz.cuni.mff.xrg.odcs.commons.app.module.event;

import org.springframework.context.ApplicationEvent;

/**
 * Base class for modules related events.
 * 
 * @author Petyr
 */
public abstract class ModuleEvent extends ApplicationEvent {

    /**
     * DPU's relative directory name.
     */
    private final String directoryName;

    /**
     * @param source
     *            Event source.
     * @param directoryName
     *            DPU's directory name.
     */
    public ModuleEvent(Object source, String directoryName) {
        super(source);
        this.directoryName = directoryName;
    }

    /**
     * @return DPU's directory name.
     */
    public String getDirectoryName() {
        return directoryName;
    }

}
