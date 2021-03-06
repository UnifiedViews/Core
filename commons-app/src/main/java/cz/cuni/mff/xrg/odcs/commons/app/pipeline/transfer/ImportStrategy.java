package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

/**
 * When importing pipeline, for DPU instance the following conditions apply:
 * <ul>
 * <li>its DPU template is a child (3rd lvl) DPU template</li>
 * <li>DPUInstanceRecord.useTemplateConfig == true</li>
 * <li>for its DPU template, DPU template with matching name was found in current system</li>
 * <li>but the found DPU template has different configuration.</li>
 *  </ul>
 * Describes available import strategies for this child DPU template.
 * 
 * @author mvi
 *
 */
public enum ImportStrategy {
    /**
     * Ignore configuration of imported pipeline, use configuration of DPU template already in system.
     * This strategy can cause that the imported pipeline may not run the same way as in the system it's
     * imported from.
     */
    CHANGE_TO_EXISTING,
    /**
     * Create new child DPU template with different name.
     * This is the least "aggressive" strategy, because it doesn't affect existing pipelines and the imported
     * pipeline will (should) run the same way as in the system it's imported from.
     */
    CREATE_NEW_CHILD,
    /**
     * Overwrite configuration in existing child DPU with the configuration of imported pipeline.
     * This may cause that existing pipeline would be affected.
     */
    OVERWRITE,
    /**
     * Instance DPUInstanceRecord.useTemplateConfig will be changed to false and the template configuration will
     * be copied to the instance. 
     */
    REPLACE_INSTANCE_CONFIG;
}
