package cz.cuni.mff.xrg.odcs.backend.context;

import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUMessage;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.properties.RuntimeProperty;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPUContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Main class holding context for the executed DPU
 */
public class Context implements DPUContext {

    @Autowired
    private RuntimePropertiesFacade runtimePropertyFacade;

    /**
     * Name of directory for shared DPU's data.
     */
    private static final String DPU_DIR = "dpu";

    /**
     * Name of sub-directory in {@link #DPU_DIR} for user related data storage.
     */
    private static final String USER_DIR = "user";

    /**
     * DPUInstanceRecord as owner of this context.
     */
    protected DPUInstanceRecord dpuInstance;

    /**
     * Manage mapping context into execution's directory.
     */
    protected ExecutionContextInfo contextInfo;

    /**
     * Time of last successful execution. Null if there is no such execution.
     */
    private Date lastSuccExec;

    /**
     * Manager for output DataUnits.
     */
    private DataUnitManager inputsManager;

    /**
     * Manager for output DataUnits.
     */
    private DataUnitManager outputsManager;

    /**
     * Application configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    /**
     * Application event publisher used to publish messages from DPURecord.
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Used to get DPU's directory.
     */
    @Autowired
    private ModuleFacade moduleFacade;

    @Autowired
    private ResourceManager resourceManager;

    /**
     * True if {@link #sendMessage(MessageType, String)} or {@link #sendMessage(MessageType, String, String)} has been used to
     * publish {@link MessageType#WARNING} message.
     */
    private boolean warningMessage;

    /**
     * True if {@link #sendMessage(MessageType, String)} or {@link #sendMessage(MessageType, String, String)} has been used to
     * publish {@link MessageType#ERROR} message.
     */
    private boolean errorMessage;

    /**
     * Set to true if the current DPU execution should be stopped as soon as
     * possible.
     */
    private boolean canceled;

    /**
     * To hold information which data units cannot be optimalized - because e.g. it consumes the output of the previous data unit together with other DU
     */
    private Set<DataUnit> cannotBeOptimalized;

    public void addNonOptimalizableDataUnit(DataUnit du) {
        cannotBeOptimalized.add(du);
    }

    @Override
    public boolean isPerformanceOptimizationEnabled(DataUnit du) {
        return !isDebugging() && !cannotBeOptimalized.contains(du);
    }

    private Locale locale;

    /**
     * True if the execution should be stopped on DPU's request. The execution
     * does not failed instantly by this.
     */
    private boolean stopExecution;

    public Context() {
        this.dpuInstance = null;
        this.contextInfo = null;
        this.lastSuccExec = null;
        this.inputsManager = null;
        this.outputsManager = null;
        this.warningMessage = false;
        this.errorMessage = false;
        this.canceled = false;
        this.stopExecution = false;
        this.locale = null;
        this.cannotBeOptimalized = new HashSet<>();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public DPUInstanceRecord getDPU() {
        return dpuInstance;
    }

    void setDPU(DPUInstanceRecord dpu) {
        this.dpuInstance = dpu;
    }

    public ExecutionContextInfo getContextInfo() {
        return contextInfo;
    }

    void setContextInfo(ExecutionContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public Date getLastSuccExec() {
        return lastSuccExec;
    }

    void setLastSuccExec(Date lastSuccExec) {
        this.lastSuccExec = lastSuccExec;
    }

    DataUnitManager getOutputsManager() {
        return outputsManager;
    }

    void setOutputsManager(DataUnitManager manager) {
        this.outputsManager = manager;
    }

    DataUnitManager getInputsManager() {
        return inputsManager;
    }

    void setInputsManager(DataUnitManager manager) {
        this.inputsManager = manager;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - //
    /**
     * Create required {@link ManagableDataUnit} and add it to the context if
     * not exist, if the {@link ManagableDataUnit} with given type is already in
     * context then the existing instance is returned.
     * 
     * @param type
     *            Type of {@link ManagableDataUnit} to create.
     * @param name
     *            DataUnit name.
     * @return Created DataUni.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public ManagableDataUnit addOutputDataUnit(ManagableDataUnit.Type type, String name) throws DataUnitException {
        return outputsManager.addDataUnit(type, name);
    }

    /**
     * Set cancel flag for execution to true. This command DPU to stop as soon
     * as possible. Can be called from other then DPU's execution thread.
     */
    public void cancel() {
        this.canceled = true;
    }

    /**
     * @return Respective {@link PipelineExecution}.
     */
    public PipelineExecution getExecution() {
        return contextInfo.getExecution();
    }

    /**
     * @return List of all input {@link ManagableDataUnit}s.
     */
    public List<ManagableDataUnit> getInputs() {
        return inputsManager.getDataUnits();
    }

    /**
     * @return List of all output {@link ManagableDataUnit}s.
     */
    public List<ManagableDataUnit> getOutputs() {
        return outputsManager.getDataUnits();
    }

    /**
     * @return True if the warning message has been publish using this context.
     */
    public boolean warningMessagePublished() {
        return this.warningMessage;
    }

    /**
     * @return True if the error message has been publish using this context.
     */
    public boolean errorMessagePublished() {
        return this.errorMessage;
    }

    /**
     * @return True if the execution should be stopped but not failed instantly.
     */
    public boolean shouldStopExecution() {
        return stopExecution;
    }

    /**
     * @return Engine's general working directory.
     */
    private File getGeneralWorkingDir() {
        return new File(appConfig.getString(ConfigProperty.GENERAL_WORKINGDIR));
    }

    /**
     * Return identification of single DPU template shared by all templates with
     * same name.
     * 
     * @return DPU template identification.
     */
    private String getTemplateIdentification() {
        return dpuInstance.getTemplate().getJarDirectory();
    }

    @Override
    public String getPipelineOwner() {
        return this.contextInfo.getExecution().getPipeline().getOwner().getUsername();
    }

    @Override
    public String getPipelineExecutionOwner() {
        return this.contextInfo.getExecution().getOwner().getUsername();
    }

    @Override
    public String getPipelineExecutionOwnerExternalId() {
        return this.contextInfo.getExecution().getOwner().getExternalIdentifier();
    }

    @Override
    public String getOrganization() {
        return "";
    }

    @Override
    public String getPipelineExecutionActorExternalId() {
        if (this.contextInfo.getExecution().getOwner().getUserActor() != null) {
            return this.contextInfo.getExecution().getOwner().getUserActor().getExternalId();
        }
        return null;
    }

    // - - - - - - - - - - ProcessingContext - - - - - - - - - - //

    @Override
    public void sendMessage(DPUContext.MessageType type, String shortMessage) {
        // jest re-call the other function
        sendMessage(type, shortMessage, "");
    }

    @Override
    public void sendMessage(DPUContext.MessageType type,
            String shortMessage,
            String fullMessage) {
        sendMessage(type, shortMessage, fullMessage, null);
    }

    @Override
    public void sendMessage(DPUContext.MessageType type,
            String shortMessage,
            String fullMessage,
            Exception exception) {
        if (exception != null) {
            // add log to the fullMessage
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            fullMessage = fullMessage + "<br/><br/>Exception:<br/>" + sw.toString();
        }
        //cut short message to 128 characters
        if (shortMessage != null && shortMessage.length() > 128) {
            shortMessage = shortMessage.substring(0, 125) + "...";
        }
        eventPublisher.publishEvent(new DPUMessage(shortMessage, fullMessage,
                type, this, this));
        // set warningMessage and errorMessage
        switch (type) {
            case WARNING:
                this.warningMessage = true;
                break;
            case ERROR:
                this.errorMessage = true;
                break;
            default:
        }
    }

    @Override
    public boolean isDebugging() {
        return contextInfo.getExecution().isDebugging();
    }

    @Override
    public boolean canceled() {
        return canceled;
    }

    @Override
    public File getWorkingDir() {
        try {
            final File dir = resourceManager.getDPUWorkingDir(contextInfo.getExecution(), dpuInstance);
            dir.mkdirs();
            return dir;
        } catch (MissingResourceException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public File getResultDir() {
        try {
            final File dir = resourceManager.getDPUStorageDir(contextInfo.getExecution(), dpuInstance);
            dir.mkdirs();
            return dir;
        } catch (MissingResourceException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public File getJarPath() {
        File path = new File(moduleFacade.getDPUDirectory() + File.separator + dpuInstance.getJarPath());
        return path;
    }

    @Override
    public Date getLastExecutionTime() {
        return lastSuccExec;
    }

    @Override
    public File getGlobalDirectory() {
        // TODO Petr: Move into ResourceManager
        File result = new File(getGeneralWorkingDir(), DPU_DIR + File.separator
                + getTemplateIdentification());
        result.mkdirs();
        return result;
    }

    @Override
    public File getUserDirectory() {
        // TODO Petr: Move into ResourceManager
        User owner = getExecution().getOwner();
        String userId;
        if (owner == null) {
            userId = "default";
        } else {
            // user name is unique .. we can use it
            userId = owner.getUsername();
        }

        File result = new File(getGeneralWorkingDir(), USER_DIR + File.separator
                + userId + File.separator + getTemplateIdentification());
        result.mkdirs();
        return result;
    }

    @Override
    public String getDpuInstanceDirectory() {
        // TODO Petr: Move into ResourceManager and change format
        File result = new File(getGeneralWorkingDir(), "dpu_instance_" + String.valueOf(dpuInstance.getId()));
        result.mkdirs();
        return result.toURI().toASCIIString();
    }

    @Override
    public Map<String, String> getEnvironment() {
        Map<String, String> result = new HashMap<>();

        //get properties from config.properties
        for (Map.Entry<Object, Object> entry : appConfig.getProperties().entrySet()) {
            result.put((String) entry.getKey(), (String) entry.getValue());
        }

        //get properties from runtime properties
        //if the map already contains value for that from config.properties, it is overwritten
        for (RuntimeProperty property : runtimePropertyFacade.getAllRuntimeProperties()) {
            result.put((String) property.getName(), (String) property.getValue());
        }

        return result;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Long getPipelineId() {
        return this.contextInfo.getExecution().getPipeline().getId();
    }

    @Override
    public Long getPipelineExecutionId() {
        return this.contextInfo.getExecutionId();
    }

    @Override
    public Long getDpuInstanceId() {
        return dpuInstance.getId();
    }

}
