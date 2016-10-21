package cz.cuni.mff.xrg.odcs.commons.app.module;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * Interface for service that enable publishing of notification about DPU
 * updates and changes.
 * 
 * @author Petyr
 */
public interface ModuleChangeNotifier {

    /**
     * Mark given DPU as updated. So the given {@link DPUTemplateRecord} should contains new jar-file name.
     * 
     * @param dpu
     *            DPU that has been updated.
     */
    public void updated(DPUTemplateRecord dpu);

    /**
     * Mark given DPU as new. This says that the {@link DPUTemplateRecord} is
     * new and it should be loaded from database into application.
     * 
     * @param dpu
     *            DPU that has been newly added.
     */
    public void created(DPUTemplateRecord dpu);

    /**
     * Notify listeners that given DPU should be unloaded from system.
     * 
     * @param dpu
     *            DPU that has been deleted.
     */
    public void deleted(DPUTemplateRecord dpu);

}
