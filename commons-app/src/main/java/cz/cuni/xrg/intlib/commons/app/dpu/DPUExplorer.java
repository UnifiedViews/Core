package cz.cuni.xrg.intlib.commons.app.dpu;

import java.util.jar.Attributes;

import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.xrg.intlib.commons.app.module.ModuleFacade;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.loader.Load;
import cz.cuni.xrg.intlib.commons.transformer.Transform;

import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsTransformer;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsLoader;

/**
 * Class provides methods that can be used to explore DPU instance.
 * 
 * @author Petyr
 *
 */
public class DPUExplorer {

	/**
	 * Name of property that store DPU type in pom.xml.
	 */
	private static final String DPU_TYPE_NAME = "dpu-type";
	
	/**
	 * Value of {@link @DPU_TYPE_NAME} that identify {@link DPUType#EXTRACTOR}.
	 */
	private static final String DPU_TYPE_EXTRACTOR = "extractor";
	
	/**
	 * Value of {@link @DPU_TYPE_NAME} that identify {@link DPUType#TRANSFORMER}.
	 */	
	private static final String DPU_TYPE_TRANFORMER = "transformer";
	
	/**
	 * Value of {@link @DPU_TYPE_NAME} that identify {@link DPUType#LOADER}.
	 */	
	private static final String DPU_TYPE_LOADER = "loader";
	
	/**
	 * Name of property that store jar-file's description.
	 */
	private static final String DPU_JAR_DESCRIPTION_NAME = "Description";
	
	/**
	 * Module facade used to access the DPU instances.
	 */
	@Autowired
	private ModuleFacade moduleFacade;
		
	/**
	 * Try to find out given DPU instance type. 
	 * @param DPUInstance
	 * @param relativePath Relative path to the DPU.
	 * @return Null if nothing about type can be found.
	 */
	public DPUType getType(Object DPUInstance, String relativePath) {
		if (DPUInstance instanceof Extract) {
			return DPUType.EXTRACTOR;
		} else if (DPUInstance instanceof Transform) {
			return DPUType.TRANSFORMER;
		} else if (DPUInstance instanceof Load) {
			return DPUType.LOADER;
		}
		// we try to use pom.xml information
		Attributes attributes = moduleFacade.getJarProperties(relativePath);
		if (attributes == null) {
			// can't load information .. we run out of options
			return null;
		}
		
		// try use annotations to resolve DPU type
		Class<?> objectClass = DPUInstance.getClass();
		if (objectClass.getAnnotation(AsExtractor.class) != null) {
			return DPUType.EXTRACTOR;
		} else if (objectClass.getAnnotation(AsTransformer.class) != null) {
			return DPUType.TRANSFORMER;
		} else if (objectClass.getAnnotation(AsLoader.class) != null) {
			return DPUType.LOADER;
		}		
		
		// use pom.xml
		String typeName = attributes.getValue(DPU_TYPE_NAME);
		if (DPU_TYPE_EXTRACTOR.compareToIgnoreCase(typeName) == 0) {
			return DPUType.EXTRACTOR;
		} else if (DPU_TYPE_TRANFORMER.compareToIgnoreCase(typeName) == 0) {
			return DPUType.TRANSFORMER;
		} else if (DPU_TYPE_LOADER.compareToIgnoreCase(typeName) == 0) {
			return DPUType.LOADER;
		}
		
		return null;
	}
	
	/**
	 * Return content of manifest for given bundle that is stored in DPU's
	 * directory. This method does not load DPU into system.
	 * 
	 * @param relativePath Relative path in DPU's directory.
	 * @return Description stored in manifest file or null in case of error.
	 */	
	public String getJarDescription(String relativePath) {
		// we try to use pom.xml information
		Attributes attributes = moduleFacade.getJarProperties(relativePath);
		if (attributes == null) {
			// can't load information .. we run out of options
			return null;
		}
		return attributes.getValue(DPU_JAR_DESCRIPTION_NAME);
	}
}
