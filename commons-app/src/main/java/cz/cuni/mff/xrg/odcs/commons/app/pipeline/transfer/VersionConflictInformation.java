package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarNameFormatException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarUtils;

public class VersionConflictInformation {

    private DpuItem dpuItem;
    /**
     * currently installed DPU template 
     */
    private DPUTemplateRecord currentDpuTemplate;
    /**
     * DPU template used in imported pipeline, that has version newer than currentDpuTemplate
     */
    private DPUTemplateRecord usedDpuTemplate;

    public VersionConflictInformation(DpuItem dpuItem, DPUTemplateRecord currentDpuTemplate, DPUTemplateRecord usedDpuTemplate) {
        this.dpuItem = dpuItem;
        this.currentDpuTemplate = currentDpuTemplate;
        this.usedDpuTemplate = usedDpuTemplate;
    }
    
    public String getCurrentJarName() {
        return currentDpuTemplate.getJarName();
    }
    
    public String getUsedJarName() {
        return usedDpuTemplate.getJarName();
    }

    public String getCurrentVersion() {
        try {
            return DPUJarUtils.parseVersionStringFromJarName(currentDpuTemplate.getJarName());
        } catch (DPUJarNameFormatException e) {
            return null;
        }                
    }
    
    public String getUsedDpuVersion() {
        try {
            return DPUJarUtils.parseVersionStringFromJarName(usedDpuTemplate.getJarName());
        } catch (DPUJarNameFormatException e) {
            return null;
        }
    }

    public DpuItem getDpuItem() {
        return dpuItem;
    }

    @Override
    public String toString() {
        return "VersionConflictInformation [dpuItem=" + dpuItem + ", currentDpuTemplate=" + currentDpuTemplate + ", usedDpuTemplate=" + usedDpuTemplate + "]";
    }
}
