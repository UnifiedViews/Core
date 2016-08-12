package cz.cuni.mff.xrg.odcs.commons.app.module.event;

/**
 * Event indicate that certain DPU needs to be reloaded due it's changes.
 * 
 * @author Petyr
 */
public class ModuleUpdateEvent extends ModuleEvent {

    private final String jarName;

    /**
     * @param source
     *            Event source.
     * @param directoryName
     *            DPU's directory name.
     * @param jarName
     *            New DPU's jar name.
     */
    public ModuleUpdateEvent(Object source,
            String directoryName,
            String jarName) {
        super(source, directoryName);
        this.jarName = jarName;
    }

    /**
     * @return New name of DPU's jar file.
     */
    public String getJarName() {
        return jarName;
    }

}
