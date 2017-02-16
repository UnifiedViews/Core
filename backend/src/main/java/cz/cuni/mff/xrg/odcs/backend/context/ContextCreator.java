package cz.cuni.mff.xrg.odcs.backend.context;

import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitFactory;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import eu.unifiedviews.dataunit.DataUnit;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Component that is used to create {@link Context} for {@link DPUInstanceRecord} and {@link ExecutionContextInfo}.
 * If context has some previous data ie. {@link ExecutionContextInfo} is not empty, data is not loaded.
 * To load data use {@link ContextRestorer}
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
        newContext.setLocale(LocaleHolder.getLocale());

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
