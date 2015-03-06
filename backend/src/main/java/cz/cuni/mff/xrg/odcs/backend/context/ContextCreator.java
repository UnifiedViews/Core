package cz.cuni.mff.xrg.odcs.backend.context;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitFactory;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;
import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;
import eu.unifiedviews.dataunit.DataUnit;


/**
 * Component that is used to create {@link Context} for give {@link DPUInstanceRecord} and {@link ExecutionContextInfo}.
 * If context has some previous data ie. {@link ExecutionContextInfo} is not
 * empty data are not loaded. To load data use {@link ContextRestore}
 *
 * @author Petyr
 */
abstract class ContextCreator {

    /**
     * Factory used to create {@link DataUnit}s.
     */
    @Autowired
    private DataUnitFactory dataUnitFactory;

    @Autowired
    private ResourceManager resourceManager;

    /**
     * Runtime properties facade.
     */
    @Autowired
    protected RuntimePropertiesFacade runtimePropertiesFacade;

    /**
     * Create context for given {@link DPUInstanceRecord} and {@link ExecutionContextInfo}. The context is ready for use. Data from {@link ExecutionContextInfo}
     * are not loaded into context.
     *
     * @param dpuInstance
     * @param contextInfo
     * @param lastSuccExec
     * @return
     */
    public Context createContext(DPUInstanceRecord dpuInstance, ExecutionContextInfo contextInfo, Date lastSuccExec) {
        // create empty context
        Context newContext = createPureContext();
        // fill context with data

        newContext.setDPU(dpuInstance);
        newContext.setContextInfo(contextInfo);
        newContext.setLastSuccExec(lastSuccExec);
        String localeName = "en_US";
        RuntimeProperty localeNameProperty = runtimePropertiesFacade.getByName("locale");
        if (localeNameProperty != null) {
            localeName = localeNameProperty.getValue();
        }
        newContext.setLocale(new Locale(localeName));

        newContext.setInputsManager(DataUnitManager.createInputManager(dpuInstance, dataUnitFactory, contextInfo, resourceManager));
        newContext.setOutputsManager(DataUnitManager.createOutputManager(dpuInstance, dataUnitFactory, contextInfo, resourceManager));

        return newContext;
    }

    /**
     * Method for spring that create new {@link Context}.
     *
     * @return
     */
    protected abstract Context createPureContext();

}
