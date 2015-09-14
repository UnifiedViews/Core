/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
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
