package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import eu.unifiedviews.helpers.dpu.localization.Messages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.data.DataUnitDescription;
import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitTypeResolver;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationContainer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationGetter;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;

/**
 * Class which provides methods that can be used to explore DPU instance.
 * 
 * @author Petyr
 */
public class DPUExplorer {

    /**
     * Name of property that stores jar-file's description.
     */
    private static final String DPU_JAR_DESCRIPTION_NAME = "Bundle-Description";
    private static final String DPU_NAME = "Bundle-Name";


    /**
     * Module facade used to access the DPU instances.
     */
    @Autowired
    private ModuleFacade moduleFacade;

    /**
     * Try to find out given DPU instance type.
     * 
     * @param DPUInstance
     *            Dpu instance to get type of.
     * @param relativePath
     *            Relative path to the DPU.
     * @return Null if nothing about type can be found.
     */
    public DPUType getType(Object DPUInstance, String relativePath) {
        // try use annotations to resolve DPU type
        Class<?> objectClass = DPUInstance.getClass();
        if (objectClass.getAnnotation(DPU.AsExtractor.class) != null) {
            return DPUType.EXTRACTOR;
        } else if (objectClass.getAnnotation(DPU.AsTransformer.class) != null) {
            return DPUType.TRANSFORMER;
        } else if (objectClass.getAnnotation(DPU.AsLoader.class) != null) {
            return DPUType.LOADER;
        } else if (objectClass.getAnnotation(DPU.AsQuality.class) != null) {
            return DPUType.QUALITY;
        }

        // we do not know
        return null;
    }

    /**
     * Return content of manifest for given bundle that is stored in DPU's
     * directory. This method does not load DPU into system. The description is
     * length is limited based on {@link LenghtLimits}.
     * 
     * @param dpu
     *            {@link DPUTemplateRecord} to explore.
     * @return Description stored in manifest file or null in case of error.
     */
    public String getJarDescription(DPUTemplateRecord dpu) {
        // we try to use pom.xml information
        Dictionary<String, String> attributes = moduleFacade
                .getManifestHeaders(dpu);
        if (attributes == null) {
            // can't load information .. we run out of options
            return "";
        }
        String jarDescription = attributes.get(DPU_JAR_DESCRIPTION_NAME);
        // check for length

        return StringUtils.abbreviate(jarDescription, LenghtLimits.DPU_JAR_DESCRIPTION);
    }


    public String getBundleName(DPUTemplateRecord dpu) {
        // we try to use pom.xml information
        Dictionary<String, String> attributes = moduleFacade
                .getManifestHeaders(dpu);
        if (attributes == null) {
            // can't load information .. we run out of options
            return "";
        }
        String jarDescription = attributes.get(DPU_NAME);
        // check for length

        return StringUtils.abbreviate(jarDescription, LenghtLimits.DPU_NAME);
    }

    /**
     * Return list of input {@link DataUnitInfo}'s descriptions for given DPU.
     * Need to load instance of given DPU.
     * 
     * @param dpu
     *            DPU to get inputs for.
     * @return Does not return null.
     */
    public List<DataUnitDescription> getInputs(DPURecord dpu) {
        // we need to load and get instance
        final Object dpuInstance = getInstance(dpu);
        // get annotations
        List<AnnotationContainer<DataUnit.AsInput>> inputs = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsInput.class);
        // translate to list
        List<DataUnitDescription> result = new ArrayList<>(inputs.size());
        for (AnnotationContainer<DataUnit.AsInput> item : inputs) {
            final DataUnit.AsInput annotation = item.getAnnotation();
            // create description
            result.add(DataUnitDescription.createInput(
                    annotation.name(),
                    DataUnitTypeResolver.resolveClassToType(item.getField().getType()).toString(),
                    annotation.description(),
                    annotation.optional())
                    );
        }
        return result;
    }

    /**
     * Return list of output {@link DataUnitInfo}'s descriptions for given DPU.
     * Need to load instance of given DPU.
     * 
     * @param dpu
     *            DPU to get outputs for.
     * @return Does not return null.
     */
    public List<DataUnitDescription> getOutputs(DPURecord dpu) {
        // we need to load and get instance
        final Object dpuInstance = getInstance(dpu);
        // get annotations
        List<AnnotationContainer<DataUnit.AsOutput>> outputs = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsOutput.class);
        // translate to list
        List<DataUnitDescription> result = new ArrayList<>(outputs.size());
        for (AnnotationContainer<DataUnit.AsOutput> item : outputs) {
            final DataUnit.AsOutput annotation = item.getAnnotation();
            // create description			
            result.add(DataUnitDescription.createOutput(annotation.name(),
                    DataUnitTypeResolver.resolveClassToType(item.getField().getType()).toString(),
                    annotation.description(),
                    annotation.optional()));
        }
        return result;
    }

    /**
     * Load and return instance of given DPU. Does use {@link #moduleFacade}.
     * 
     * @param dpu
     *            DPU to get class of.
     * @return Does not return null.
     */
    private Object getInstance(DPURecord dpu) {
        try {
            // TODO Petyr: Work with exception ..
            dpu.loadInstance(moduleFacade);
        } catch (ModuleException e) {
            throw new RuntimeException(e);
        }
        return dpu.getInstance();
    }
}
