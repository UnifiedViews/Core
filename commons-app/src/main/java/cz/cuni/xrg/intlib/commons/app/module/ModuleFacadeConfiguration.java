package cz.cuni.xrg.intlib.commons.app.module;

import cz.cuni.xrg.intlib.commons.app.conf.AppConfiguration;
import cz.cuni.xrg.intlib.commons.app.conf.ConfProperty;
import java.util.Properties;

/**
 * Contains settings for ModuleFacade;
 *
 * @author Petyr
 *
 */
public class ModuleFacadeConfiguration {

	/**
	 * Folder with dpu to load during execution.
	 */
	private String dpuFolder;

	/**
	 * List package that should be expose from application.
	 */
	private String packagesToExpose = "";
	
	/**
	 * Folder with dpu libraries.
	 */
	private String dpuLibsFolder = "";

	/**
	 * Module configuration is constructed directly from {@link AppConfiguration}.
	 *
	 * @param conf
	 */
	public ModuleFacadeConfiguration(AppConfiguration conf) {
		dpuFolder = conf.getString(ConfProperty.MODULE_PATH);
		packagesToExpose = conf.getString(ConfProperty.MODULE_EXPOSE);
		dpuLibsFolder = conf.getString(ConfProperty.MODULE_LIBS);
	}

	public String getDpuFolder() {
		return dpuFolder;
	}

	public void setDpuFolder(String dpuFolder) {
		this.dpuFolder = dpuFolder;
	}

	public String getPackagesToExpose() {
		return packagesToExpose;
	}

	public void setPackagesToExpose(String packagesToExpose) {
		this.packagesToExpose = packagesToExpose;
	}

	public String getDpuLibsFolder() {
		return dpuLibsFolder;
	}

	public void setDpuLibsFolder(String dpuLibsFolder) {
		this.dpuLibsFolder = dpuLibsFolder;
	}
}
