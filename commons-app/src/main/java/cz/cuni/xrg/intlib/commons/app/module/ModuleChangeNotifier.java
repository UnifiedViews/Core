package cz.cuni.xrg.intlib.commons.app.module;

import cz.cuni.xrg.intlib.commons.app.dpu.DPUTemplateRecord;

/**
 * Enables mark given DPU as updated. Such DPU will be updated
 * in backend before next use.
 * 
 * @author Petyr
 *
 */
public interface ModuleChangeNotifier {

	/**
	 * Mark given DPU as updated. The given {@link DPUTemplateRecord}
	 * should contains new jar-file name.
	 * @param dpu
	 */
	public void updated(DPUTemplateRecord dpu);
	
}
