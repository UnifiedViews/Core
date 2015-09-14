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
package cz.cuni.mff.xrg.odcs.frontend.gui.dialog;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.DpuItem;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportService;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportStrategy;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportedFileInformation;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.VersionConflictInformation;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.FileUploadReceiver;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.UploadInfoWindow;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Dialog for pipeline import.
 * 
 * @author Škoda Petr
 */
public class PipelineImport extends Window {
    private static final long serialVersionUID = -8324764730976125541L;

    private static final Logger LOG = LoggerFactory.getLogger(
            PipelineImport.class);

    private TextField txtUploadFile;

    private Pipeline importedPipeline = null;

    private Table usedDpusTable = new Table();

    private Table missingDpusTable = new Table();

    private Button btnImport = new Button();

    private Panel panelMissingDpus = new Panel();

    private CheckBox chbImportDPUData = new CheckBox(Messages.getString("PipelineImport.import.usersData"));

    private CheckBox chbImportSchedule = new CheckBox(Messages.getString("PipelineImport.import.schedule"));

    /**
     * Receive uploaded file.
     */
    private FileUploadReceiver fileUploadReceiver;

    /**
     * Dialog with information about file upload process.
     */
    private UploadInfoWindow uploadInfoWindow;

    /**
     * Service used to import pipelines.
     */
    private final ImportService importService;

    private Set<String> toDecideDpus;
    
    public PipelineImport(ImportService importService) {
        super(Messages.getString("PipelineImport.pipeline.import"));
        this.importService = importService;
        init();
    }

    /**
     * Initialise user interface.
     */
    private void init() {
        this.setResizable(false);
        this.setModal(true);
        this.setWidth("500px");
        this.setHeight("520px");
        this.markAsDirtyRecursive();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        // upload settings
        final GridLayout detailLayout = new GridLayout(2, 3);
        detailLayout.setMargin(false);
        detailLayout.setSpacing(true);
        detailLayout.setSizeFull();

        detailLayout.setRowExpandRatio(0, 0);
        detailLayout.setRowExpandRatio(1, 1);

        detailLayout.setColumnExpandRatio(0, 0);
        detailLayout.setColumnExpandRatio(1, 1);

        {
            Label lbl = new Label(Messages.getString("PipelineImport.zip.archive"));
            lbl.setWidth("-1px");
            detailLayout.addComponent(lbl, 0, 0);
        }

        txtUploadFile = new TextField();
        txtUploadFile.setWidth("100%");
        txtUploadFile.setReadOnly(true);
        txtUploadFile.setRequired(true);
        detailLayout.addComponent(txtUploadFile, 1, 0);
        detailLayout.setComponentAlignment(txtUploadFile, Alignment.MIDDLE_LEFT);

        fileUploadReceiver = new FileUploadReceiver();
        final Upload upload = new Upload(null, fileUploadReceiver);
        upload.setImmediate(true);
        upload.setButtonCaption(Messages.getString("PipelineImport.upload.file"));
        // modify the look so the upload component is more user friendly
        upload.addStyleName("horizontalgroup");

        // create dialog for upload process
        uploadInfoWindow = new UploadInfoWindow(upload);

        // assign action to upload
        upload.addStartedListener(new Upload.StartedListener() {
            private static final long serialVersionUID = -2658235355133996796L;

            @Override
            public void uploadStarted(Upload.StartedEvent event) {
                ResourceManager.cleanupQuietly(fileUploadReceiver.getParentDir());
                
                String ext = FilenameUtils.getExtension(event.getFilename());
                missingDpusTable.removeAllItems();
                usedDpusTable.removeAllItems();
                btnImport.setEnabled(false);

                if (ext.compareToIgnoreCase("zip") != 0) {
                    upload.interruptUpload();
                    Notification.show(Messages.getString("PipelineImport.not.zip.file"),
                            Notification.Type.ERROR_MESSAGE);
                } else {
                    // show upload process dialog
                    if (uploadInfoWindow.getParent() == null) {
                        UI.getCurrent().addWindow(uploadInfoWindow);
                    }
                    uploadInfoWindow.setClosable(false);
                }
            }
        });
        upload.addFailedListener(new Upload.FailedListener() {
            private static final long serialVersionUID = 6971823433495364693L;

            @Override
            public void uploadFailed(Upload.FailedEvent event) {
                txtUploadFile.setReadOnly(false);
                txtUploadFile.setValue("");
                txtUploadFile.setReadOnly(true);
                // close upload info dialog
                uploadInfoWindow.setClosable(true);
                uploadInfoWindow.close();
            }
        });
        upload.addFinishedListener(new Upload.FinishedListener() {
            private static final long serialVersionUID = -1262281366130671667L;

            @Override
            public void uploadFinished(Upload.FinishedEvent event) {
                txtUploadFile.setReadOnly(false);
                txtUploadFile.setValue(event.getFilename());
                txtUploadFile.setReadOnly(true);
                // close upload info dialog
                uploadInfoWindow.setClosable(true);
                uploadInfoWindow.close();
                // hide uploader
                File zippedFile = fileUploadReceiver.getFile();
                // disable import buttons
                
                try {
                    final ImportedFileInformation result = importService.getImportedInformation(zippedFile);
                    final List<DpuItem> usedDpus = result.getUsedDpus();
                    final Map<String, DpuItem> missingDpus = result.getMissingDpus();
                    toDecideDpus = Collections.emptySet();

                    chbImportDPUData.setValue(false);
                    chbImportSchedule.setValue(false);

                    if (result.isUserDataFile()) {
                        chbImportDPUData.setEnabled(true);
                    } else {
                        chbImportDPUData.setEnabled(false);
                    }

                    if (result.isScheduleFile()) {
                        chbImportSchedule.setEnabled(true);
                    } else {
                        chbImportSchedule.setEnabled(false);
                    }

                    if (usedDpus == null) {
                        String msg = Messages.getString("PipelineImport.read.file.fail");
                        LOG.warn(msg);
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                    } else {
                        // show result on table  these dpus which are in use
                        for (DpuItem entry : usedDpus) {
                            usedDpusTable.addItem(new Object[] { entry.getDpuName(), entry.getJarName(), entry.getVersion() }, null);
                        }
                    }
                    
                    if (result.getOldDpus().isEmpty() && missingDpus.isEmpty()) {
                        btnImport.setEnabled(true);
                    }

                    // show result on table - these dpus which have older version installed
                    for (VersionConflictInformation value : result.getOldDpus().values()) {
                        RowTooltip tooltip = new RowTooltip(Messages.getString("PipelineImport.outdated.version.tooltip", value.getCurrentVersion(), value.getUsedDpuVersion()));
                        missingDpusTable.addItem(new Object[] { value.getDpuItem().getDpuName(), Messages.getString("PipelineImport.outdated.version") }, tooltip);
                    }
                    
                    // show result on table - these dpus which are missing
                    for (DpuItem value : missingDpus.values()) {
                        RowTooltip tooltip = new RowTooltip(Messages.getString("PipelineImport.missing.dpu.tooltip", value.getJarName()));
                        missingDpusTable.addItem(new Object[] { value.getDpuName(), Messages.getString("PipelineImport.missing.dpu") }, tooltip);
                    }
                    
                    toDecideDpus = result.getToDecideDpus();

                } catch (ImportException e) {
                    Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
                    LOG.error("reading of pipeline from zip: {} failed", zippedFile, e);
                } catch (Exception e) {
                    Notification.show(Messages.getString("PipelineImport.read.info.fail"), Type.ERROR_MESSAGE);
                    LOG.error("reading of pipeline from zip: {} failed", zippedFile, e);
                }
            }
        });

        detailLayout.addComponent(upload, 1, 1);
        detailLayout.setComponentAlignment(upload, Alignment.TOP_LEFT);
        final HorizontalLayout checkBoxesLayout = new HorizontalLayout();
        checkBoxesLayout.setWidth("100%");
        checkBoxesLayout.setMargin(true);
        this.chbImportSchedule.setEnabled(false);
        this.chbImportSchedule.setVisible(this.importService.hasUserPermission(EntityPermissions.PIPELINE_IMPORT_SCHEDULE_RULES)
                && this.importService.hasUserPermission(EntityPermissions.SCHEDULE_RULE_CREATE));
        this.chbImportDPUData.setEnabled(false);
        this.chbImportDPUData.setVisible(this.importService.hasUserPermission(EntityPermissions.PIPELINE_IMPORT_USER_DATA));
        checkBoxesLayout.addComponent(chbImportSchedule);
        checkBoxesLayout.setComponentAlignment(chbImportSchedule, Alignment.MIDDLE_LEFT);
        checkBoxesLayout.addComponent(chbImportDPUData);
        checkBoxesLayout.setComponentAlignment(chbImportDPUData, Alignment.MIDDLE_RIGHT);

        final VerticalLayout usedJarsLayout = new VerticalLayout();
        usedJarsLayout.setWidth("100%");

        Panel panel = new Panel(Messages.getString("PipelineImport.dpu.used"));
        panel.setWidth("100%");
        panel.setHeight("150px");

        usedDpusTable.addContainerProperty(Messages.getString("PipelineImport.dpu.template"), String.class, null);
        usedDpusTable.addContainerProperty(Messages.getString("PipelineImport.dpu.jarName"), String.class, null);
        usedDpusTable.addContainerProperty(Messages.getString("PipelineImport.dpu.version"), String.class, null);

        usedDpusTable.setWidth("100%");
        usedDpusTable.setHeight("130px");

        panel.setContent(usedDpusTable);
        usedJarsLayout.addComponent(panel);

        final VerticalLayout missingJarsLayout = new VerticalLayout();
        missingJarsLayout.setWidth("100%");

        panelMissingDpus = new Panel(Messages.getString("PipelineImport.missing.dpus"));
        panelMissingDpus.setWidth("100%");
        panelMissingDpus.setHeight("150px");

        missingDpusTable.addContainerProperty(Messages.getString("PipelineImport.missing.dpu.template"), String.class, null);
        missingDpusTable.addContainerProperty(Messages.getString("PipelineImport.missing.descr"), String.class, null);

        missingDpusTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            private static final long serialVersionUID = -403713439427197149L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                if (itemId instanceof RowTooltip) {
                    return ((RowTooltip) itemId).tooltip;
                }
                return null;
            }
        });
        
        missingDpusTable.setWidth("100%");
        missingDpusTable.setHeight("130px");
        panelMissingDpus.setContent(missingDpusTable);
        missingJarsLayout.addComponent(panelMissingDpus);

        // bottom buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");

        btnImport = new Button(Messages.getString("PipelineImport.import"), new Button.ClickListener() {
            private static final long serialVersionUID = 2121846748744770895L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!txtUploadFile.isValid()) {
                    Notification.show(Messages.getString("PipelineImport.archive.notSelected"),
                            Notification.Type.ERROR_MESSAGE);
                } else {
                    if (!toDecideDpus.isEmpty()) {
                        openChooseImportStrategyDialog(toDecideDpus);
                    } else {
                        startImport(new HashMap<String, ImportStrategy>(0));
                    }
                }
            }
        });
        btnImport.setEnabled(false);
        buttonLayout.addComponent(btnImport);
        buttonLayout.setComponentAlignment(btnImport, Alignment.MIDDLE_LEFT);

        Button btnCancel = new Button(Messages.getString("PipelineImport.cancel"), new Button.ClickListener() {
            private static final long serialVersionUID = 6897248228251332624L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        buttonLayout.addComponent(btnCancel);
        buttonLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_RIGHT);

        // add to the main layout
        mainLayout.addComponent(detailLayout);
        mainLayout.setExpandRatio(detailLayout, 1);
        mainLayout.addComponent(checkBoxesLayout);
        mainLayout.setExpandRatio(checkBoxesLayout, 1);

        mainLayout.addComponent(usedJarsLayout);
        mainLayout.setExpandRatio(usedJarsLayout, 3);
        mainLayout.addComponent(missingJarsLayout);
        mainLayout.setExpandRatio(missingJarsLayout, 3);
        mainLayout.addComponent(buttonLayout);
        mainLayout.setExpandRatio(buttonLayout, 0);
        setContent(mainLayout);
        
        this.addCloseListener(new CloseListener() {
            private static final long serialVersionUID = -1993655234546491505L;

            @Override
            public void windowClose(CloseEvent e) {
                ResourceManager.cleanupQuietly(fileUploadReceiver.getParentDir());
                close();
            }
        });
    }
    
    private void startImport(Map<String, ImportStrategy> choosenStrategies) {
        // import
        final File zipFile = fileUploadReceiver.getFile();
        try {
            importedPipeline = importService.importPipeline(zipFile, chbImportDPUData.getValue(), chbImportSchedule.getValue(), choosenStrategies);
            close();
        } catch (ImportException | IOException ex) {
            LOG.error("Import failed.", ex);
            Notification.show(Messages.getString("PipelineImport.import.fail") + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        }
    }

    final void openChooseImportStrategyDialog(Set<String> toDecideDpus) {
        final ChooseImportStrategyDialog dialog = new ChooseImportStrategyDialog(toDecideDpus);
        dialog.addCloseListener(new Window.CloseListener() {
            private static final long serialVersionUID = -5040883763888691197L;

            @Override
            public void windowClose(CloseEvent e) {
                Map<String, ImportStrategy> choosenStrategies = dialog.getChoices();
                if (choosenStrategies.isEmpty()) {
                    Notification.show(Messages.getString("PipelineImport.import.canceled"));
                } else {
                    startImport(choosenStrategies);
                }
            }
        });
        
        UI.getCurrent().addWindow(dialog);
        dialog.bringToFront();
    }

    /**
     * Return imported pipeline or null if no pipeline has been imported.
     * 
     * @return
     */
    public Pipeline getImportedPipeline() {
        return importedPipeline;
    }

    private class RowTooltip {
        public String tooltip;

        public RowTooltip(String string) {
            this.tooltip = string;
        }
    }
}
