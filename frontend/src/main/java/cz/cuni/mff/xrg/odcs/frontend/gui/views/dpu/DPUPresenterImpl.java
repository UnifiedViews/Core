package cz.cuni.mff.xrg.odcs.frontend.gui.views.dpu;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUModuleManipulator;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUReplaceException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUTemplateWrap;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DPUCreate;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.FileUploadReceiver;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.PipelineStatus;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PipelineEdit;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PostLogoutCleaner;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.navigation.Address;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigator;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * @author Bogo
 */
@Component
@Scope("session")
@Address(url = "DPURecord")
public class DPUPresenterImpl implements DPUPresenter, PostLogoutCleaner {

    @Autowired
    private DPUView view;

    @Autowired
    PipelineStatus pipelineStatus;

    @Autowired
    private DPUCreate createDPU;

    ClassNavigator navigator;

    @Autowired
    private DPUModuleManipulator dpuManipulator;

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    /**
     * Evaluates permissions of currently logged in user.
     */
    @Autowired
    private PermissionUtils permissionUtils;

    private DPUTemplateRecord selectedDpu = null;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired
    private DPUFacade dpuFacade;

    private Window.CloseListener createDPUCloseListener;

    private static final Logger LOG = LoggerFactory.getLogger(DPUPresenterImpl.class);

    /**
     * Cache for pipelines using currently selected DPU template.
     */
    private Map<Long, Pipeline> pipelinesWithDPU = new HashMap<>();

    private boolean isLayoutInitialized = false;

    @Override
    public boolean saveDPUEventHandler(DPUTemplateWrap dpuWrap) {
        // saving configuration
        try {
            dpuWrap.saveConfig();
            Notification.show(Messages.getString("DPUPresenterImpl.dpurecord.saved"), Notification.Type.HUMANIZED_MESSAGE);
        } catch (DPUConfigException e) {
            Notification.show(Messages.getString("DPUPresenterImpl.configuration.save.failed"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return false;
        } catch (DPUWrapException e) {
            Notification.show(
                    Messages.getString("DPUPresenterImpl.unExpected.error"),
                    e.getMessage(), Notification.Type.WARNING_MESSAGE);
            LOG.error("Unexpected error while saving configuration for {}", dpuWrap.getDPUTemplateRecord().getId(), e);
            return false;
        }

        // store into DB
        dpuFacade.save(dpuWrap.getDPUTemplateRecord());
        return true;
    }

    void copyDPU(DPUTemplateRecord selectedDpu) {
        int i = 1;
        boolean found = true;
        String nameOfDpuCopy = "";
        List<DPUTemplateRecord> allDpus = dpuFacade.getAllTemplates();
        while (found) {
            found = false;
            nameOfDpuCopy = Messages.getString("DPUPresenterImpl.copy.of") + " " + selectedDpu.getName();
            if (i > 1) {
                nameOfDpuCopy = nameOfDpuCopy + " " + i;
            }

            for (DPUTemplateRecord dpu : allDpus) {
                if (dpu.getName().equals(nameOfDpuCopy)) {
                    found = true;
                    break;
                }
            }
            i++;
        }
        nameOfDpuCopy = StringUtils.abbreviate(nameOfDpuCopy, LenghtLimits.DPU_NAME);

        DPUTemplateRecord copyDpuTemplate = dpuFacade.createCopy(selectedDpu);
        copyDpuTemplate.setName(nameOfDpuCopy);
        copyDpuTemplate.setMenuName(nameOfDpuCopy);
        if (selectedDpu.getParent() != null)
            copyDpuTemplate.setParent(selectedDpu.getParent());
        else
            copyDpuTemplate.setParent(selectedDpu);
        dpuFacade.save(copyDpuTemplate);
    }

    /**
     * Delete DPU Template if it's unused by any pipeline
     */
    boolean deleteDPU(DPUTemplateRecord dpu) {

        List<Pipeline> pipelines = pipelineFacade.getPipelinesUsingDPU(dpu);

        //If DPU Template is unused by any pipeline
        if (pipelines.isEmpty()) {
            //find if DPU Template has child elements
            List<DPUTemplateRecord> childDpus = dpuFacade.getChildDPUs(dpu);
            if (!childDpus.isEmpty()) {
                Notification.show(Messages.getString("DPUPresenterImpl.cannot.remove.dpu"), Notification.Type.ERROR_MESSAGE);
                return false;
            }

            //if DPU Template hasn't child elements then delete it.
            if (dpu.getParent() == null) {
                // first level DPU .. delete it completely
                dpuManipulator.delete(dpu);
            } else {
                // 2+ level DPU .. just delete the database record
                dpuFacade.delete(dpu);
            }

            Notification.show(Messages.getString("DPUPresenterImpl.dpu.removed"),
                    Notification.Type.HUMANIZED_MESSAGE);
            return true;
        } //If DPU Template it used by any pipeline, than show the names of this pipelines.
        else if (pipelines.size() == 1) {
            Notification.show(Messages.getString("DPUPresenterImpl.dpu.not.removed"), pipelines.get(0).getName(), Notification.Type.WARNING_MESSAGE);
        } else {
            Iterator<Pipeline> iterator = pipelines.iterator();
            StringBuilder names = new StringBuilder(iterator.next().getName());
            while (iterator.hasNext()) {
                names.append(", ");
                names.append(iterator.next().getName());
            }
            names.append('.');
            Notification.show(Messages.getString("DPUPresenterImpl.dpu.used"), names.toString(), Notification.Type.WARNING_MESSAGE);
        }
        return false;
    }

    void deletePipeline(Long pipeId) {
    }

    Pipeline getPipeline(Long pipeId) {
        return pipelinesWithDPU.get(pipeId);
    }

    IndexedContainer getPipelinesForDpu(DPUTemplateRecord dpu) {
        IndexedContainer result = new IndexedContainer();
        // visible columns of instancesTable
        String[] visibleCols = new String[] { "id", "actions", "name" };

        for (String p : visibleCols) {
            // setting type of the columns
            if (p.equals("id")) {
                result.addContainerProperty(p, Long.class, null);
            } else {
                result.addContainerProperty(p, String.class, "");
            }
        }
        // getting all Pipelines with specified DPU in it
        List<Pipeline> pipelines = this.pipelineFacade.getPipelinesUsingDPU(dpu);

        pipelinesWithDPU = new HashMap<>();
        for (Pipeline pipeline : pipelines) {

            // add pipeline to cache
            pipelinesWithDPU.put(pipeline.getId(), pipeline);

            Item item = result.addItem(pipeline.getId());
            if (item != null) {
                item.getItemProperty("id").setValue(pipeline.getId());
                item.getItemProperty("name").setValue(pipeline.getName());
                //item.getItemProperty("description").setValue(pipeline.getDescription());
                //item.getItemProperty("author").setValue(pipeline.getOwner().getUsername());
            }
        }

        return result;
    }

    DPUTemplateRecord dpuUploaded(File file, DPUTemplateRecord dpu) {
        copyToTarget(file, dpu);
        return dpuFacade.getTemplate(dpu.getId());
    }

    /**
     * Reload DPU. The new DPU's jar file is accessible through the {@link FileUploadReceiver#getFile()}. The current DPU, which is being replaced
     * , is assumed to be stored in {@link selectedDpu}.
     */
    private void copyToTarget(File newJar, DPUTemplateRecord dpu) {
        if (newJar == null) {
            // we have no file, end 
            return;
        }

        try {
            dpuManipulator.replace(dpu, newJar);
        } catch (DPUReplaceException e) {
            Notification.show(Messages.getString("DPUPresenterImpl.replace.failed"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
        }

        // and show message to the user that the replace has been successful
        Notification.show(Messages.getString("DPUPresenterImpl.replace.finished"), Notification.Type.HUMANIZED_MESSAGE);
    }

    @Override
    public void selectDPUEventHandler(final DPUTemplateRecord dpu, final Object oldValue) {
        //if the previous selected
        try {
            if (selectedDpu != null && selectedDpu.getId() != null && view.isChanged() && hasPermission(EntityPermissions.DPU_TEMPLATE_EDIT)) {

                //open confirmation dialog
                ConfirmDialog.show(UI.getCurrent(), Messages.getString("DPUPresenterImpl.unsaved.changes"),
                        Messages.getString("DPUPresenterImpl.unsaved.changes.dialog"),
                        Messages.getString("DPUPresenterImpl.unsaved.changes.save"), Messages.getString("DPUPresenterImpl.unsaved.changes.discard"),
                        Messages.getString("DPUPresenterImpl.unsaved.changes.cancel"),
                        new ConfirmDialog.Listener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onClose(ConfirmDialog cd) {
                                if (cd.isConfirmed()) {
                                    view.saveDPUTemplate();
                                    view.refresh();
                                    selectedDpu = dpu;
                                    view.selectNewDPU(dpu);
                                } else if (cd.isCanceled()) {
                                    selectedDpu = dpu;
                                    view.selectNewDPU(dpu);
                                } else {
                                    view.treeSetValue(oldValue);
                                }
                            }
                        });

            } else {
                selectedDpu = dpu;
                view.selectNewDPU(dpu);
            }
        } catch (final DPUWrapException ex) {
            //open confirmation dialog
            final DPUTemplateRecord oldSelectedDpu = selectedDpu;
            ConfirmDialog cd = ConfirmDialog.getFactory().create(Messages.getString("DPUPresenterImpl.unsaved.changes"),
                    Messages.getString("DPUPresenterImpl.unsaved.changes.dialog"),
                    Messages.getString("DPUPresenterImpl.unsaved.changes.save"), Messages.getString("DPUPresenterImpl.unsaved.changes.discard"), Messages.getString("DPUPresenterImpl.unsaved.changes.cancel")
                    );
            cd.show(UI.getCurrent(), new ConfirmDialog.Listener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(ConfirmDialog cd) {
                    if (cd.isConfirmed()) {
                        Notification.show(Messages.getString("DPUPresenterImpl.configuration.save.failed"), ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        LOG.error("hasConfigChanged() throws for DPU '{}'",
                                dpu.getId(), ex);
                        view.treeSetValue(oldValue);
                    } else if (cd.isCanceled()) {
                        selectedDpu = dpu;
                        view.selectNewDPU(dpu);
                    } else {
                        view.treeSetValue(oldValue);
                    }
                }
            }, true);
        }
    }

    @Override
    public void dpuUploadedEventHandler(File file) {
        DPUTemplateRecord dpu = dpuUploaded(file, selectedDpu);
        view.refresh();
        view.selectNewDPU(dpu);
    }

    @Override
    public boolean hasPermission(String type) {
        return permissionUtils.hasPermission(selectedDpu, type);
    }

    /**
     * Return container with data used in table with instances of given DPU.
     * 
     * @param dpu
     * @return result IndexedContainer for table
     */
    @Override
    public IndexedContainer getTableData(DPUTemplateRecord dpu) {
        return getPipelinesForDpu(dpu);
    }

    @Override
    public Object enter() {
        navigator = ((AppEntry) UI.getCurrent()).getNavigation();
//        if (!isLayoutInitialized) {
//        	selectedDpu = null;
//		}
        Object viewObject = view.enter(this);
        isLayoutInitialized = true;

        createDPUCloseListener = new Window.CloseListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void windowClose(Window.CloseEvent e) {
                //refresh DPU tree after closing DPU Template creation dialog 
                view.refresh();
            }
        };

        return viewObject;
    }

    @Override
    public void setParameters(Object configuration) {
        // we do not care about parameters, we always do the same job .. 
    }

    @Override
    public void openDPUCreateEventHandler() {
        //Open the dialog for DPU Template creation
        if (!UI.getCurrent().getWindows().contains(createDPU)) {
            createDPU.initClean();
            UI.getCurrent().addWindow(createDPU);
            createDPU.removeCloseListener(createDPUCloseListener);
            createDPU.addCloseListener(createDPUCloseListener);
        } else {
            createDPU.bringToFront();
        }
    }

    @Override
    public void importDPUTemplateEventHandler() {
        throw new UnsupportedOperationException(Messages.getString("DPUPresenterImpl.not.supported")); //To change body of generated methods, choose Tools | Templates. 
    }

    @Override
    public void exportAllEventHandler() {
        throw new UnsupportedOperationException(Messages.getString("DPUPresenterImpl.not.supported")); //To change body of generated methods, choose Tools | Templates. 
    }

    @Override
    public void copyDPUEventHandler() {
        copyDPU(selectedDpu);
    }

    @Override
    public void deleteDPUEventHandler() {
        boolean isDeleted = deleteDPU(selectedDpu);
        if (isDeleted) {
            // and refresh the layout
            view.refresh();
            view.selectNewDPU(null);
        }
    }

    @Override
    public void pipelineDetailEventHandler(Long id) {
        // navigate to PIPELINE_EDIT
        navigator.navigateTo(PipelineEdit.class, id.toString());
    }

    @Override
    public void pipelineDeleteEventHandler(Long id) {
        final Pipeline pipe = pipelineFacade.getPipeline(id);
        List<PipelineExecution> executions = pipelineFacade.getExecutions(pipe, PipelineExecutionStatus.QUEUED);
        if (executions.isEmpty()) {
            executions = pipelineFacade.getExecutions(pipe, PipelineExecutionStatus.RUNNING);
        }
        if (!executions.isEmpty()) {
            Notification.show(Messages.getString("DPUPresenterImpl.pipeline.running", pipe.getName()), Notification.Type.WARNING_MESSAGE);
            return;
        }

        String message = Messages.getString("DPUPresenterImpl.delete.dialog", pipe.getName());
        ConfirmDialog.show(UI.getCurrent(), Messages.getString("DPUPresenterImpl.delete.dialog.confirmation"), message, Messages.getString("DPUPresenterImpl.delete.dialog.confirmation.delete"), Messages.getString("DPUPresenterImpl.delete.dialog.confirmation.cancel"), new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog cd) {
                if (cd.isConfirmed()) {
                    pipelinesWithDPU.remove(pipe.getId());
                    pipelineFacade.delete(pipe);
                    view.removePipelineFromTable(pipe.getId());
                }
            }
        });
    }

    @Override
    public void pipelineStatusEventHandler(Long pipelineId) {
        Pipeline pipe = getPipeline(pipelineId);
        pipelineStatus.setSelectedPipeline(pipe);
        // open the window with status parameters.
        UI.getCurrent().addWindow(pipelineStatus);
    }

    @Override
    public boolean showPipelineDeleteButton(long pipelineId) {
        Pipeline pipe = getPipeline(pipelineId);

        String adminPermission = appConfig.getString(ConfigProperty.ADMIN_PERMISSION);

        boolean isAdmin = permissionUtils.hasPermission(pipe, adminPermission);
        boolean canDelete = permissionUtils.hasPermission(pipe, EntityPermissions.PIPELINE_DELETE);
        return isAdmin || canDelete;
    }

    @Override
    public boolean showPipelineDetailButton(long pipelineId) {
        Pipeline pipe = getPipeline(pipelineId);
        return permissionUtils.hasPermission(pipe, EntityPermissions.PIPELINE_READ);
    }

    @Override
    public boolean showPipelineStatusButton(long pipelineId) {
        Pipeline pipe = getPipeline(pipelineId);
        return permissionUtils.hasPermission(pipe, EntityPermissions.PIPELINE_EXECUTION_READ);
    }

    @Override
    public void doAfterLogout() {
        isLayoutInitialized = false;
    }

    @Override
    public boolean isLayoutInitialized() {
        return isLayoutInitialized;
    }
}
