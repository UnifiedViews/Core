package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.Validator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.transfer.ImportService;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUCreateException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUModuleManipulator;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ArchiveStructure;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ZipCommons;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUTemplateWrap;
import cz.cuni.mff.xrg.odcs.frontend.gui.AuthAwareButtonClickWrapper;
import cz.cuni.mff.xrg.odcs.frontend.gui.dialogs.SimpleDialog;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Dialog for the DPU template creation. Allows to upload a JAR file and on base
 * of it create a new DPU template that will be stored to the DPU template tree.
 *
 * @author Maria Kukhar
 */
@Component
@Scope("prototype")
public class DPUCreate extends Window {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PermissionUtils permissionUtils;

    @Autowired
    private Utils utils;
    
    @Autowired
    private ResourceManager resourceManager;

    private static final long serialVersionUID = 5345488404880242019L;

    private static final Logger LOG = LoggerFactory.getLogger(DPUCreate.class);

    /**
     * @return the uploadInfoWindow
     */
    public static UploadInfoWindow getUploadInfoWindow() {
        return uploadInfoWindow;
    }

    /**
     * @param aUploadInfoWindow
     *            the uploadInfoWindow to set
     */
    public static void setUploadInfoWindow(UploadInfoWindow aUploadInfoWindow) {
        uploadInfoWindow = aUploadInfoWindow;
    }

    /**
     * @return the fl
     */
    public static int getFl() {
        return fl;
    }

    /**
     * @param aFl
     *            the fl to set
     */
    public static void setFl(int aFl) {
        fl = aFl;
    }

    private OptionGroup groupVisibility;

    private OptionGroup groupVisibilityZip;

    private FileUploadReceiver fileUploadReceiver;

    private FileUploadReceiver fileUploadReceiverZip;

    private static UploadInfoWindow uploadInfoWindow;

    private GridLayout dpuGeneralSettingsLayout;

    private GridLayout dpuGeneralSettingsLayoutZip;

    private DPUTemplateRecord dpuTemplate;

    private TextField uploadFile;

    private TextField uploadFileZip;

    private static int fl = 0;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private DPUModuleManipulator dpuManipulator;

    @Autowired
    private ImportService dpuImportService;

    /**
     * Basic constructor.
     */
    public DPUCreate() {
    }

    @PostConstruct
    private void init() {

        this.setResizable(false);
        this.setModal(true);
        this.setCaption(Messages.getString("DPUCreate.create"));

        TabSheet tabs = new TabSheet();
        tabs.addTab(createJarTab(), "jar");
        tabs.addTab(createZipTab(), "zip");

        this.setContent(tabs);
        setSizeUndefined();
        setWidth("500px");
        
        this.addCloseListener(new CloseListener() {
            private static final long serialVersionUID = 1277786180063490433L;

            @Override
            public void windowClose(CloseEvent e) {
                cleanup(fileUploadReceiver, fileUploadReceiverZip);
                close();
            }
        });
    }

    private com.vaadin.ui.Component createZipTab() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setStyleName("dpuDetailMainLayout");
        mainLayout.setMargin(true);

        dpuGeneralSettingsLayoutZip = new GridLayout(2, 4);
        dpuGeneralSettingsLayoutZip.setSpacing(true);
        dpuGeneralSettingsLayoutZip.setWidth("400px");
        dpuGeneralSettingsLayoutZip.setHeight("200px");

        Label help = new Label(Messages.getString("DPUCreate.create.description")
                + Messages.getString("DPUCreate.create.description2")
                + Messages.getString("DPUCreate.create.description3"));
        help.setWidth("310px");
        help.setHeight("60px");
        dpuGeneralSettingsLayoutZip.addComponent(help, 1, 0);

        //Visibility of DPU Template: label & OptionGroup
        groupVisibilityZip = createVisibilityOption(dpuGeneralSettingsLayoutZip, 1);

        Label selectLabel = new Label(Messages.getString("DPUCreate.select.zip"));
        selectLabel.setImmediate(false);
        selectLabel.setWidth("-1px");
        selectLabel.setHeight("-1px");
        dpuGeneralSettingsLayoutZip.addComponent(selectLabel, 0, 2);

        fileUploadReceiverZip = new FileUploadReceiver();

        uploadFileZip = new TextField();
        HorizontalLayout uploadFileLayout = buildUploadLayout(dpuGeneralSettingsLayoutZip,
                fileUploadReceiverZip, uploadFileZip, "zip", 2);

        dpuGeneralSettingsLayoutZip.addComponent(uploadFileLayout, 1, 2);

        dpuGeneralSettingsLayoutZip.setMargin(new MarginInfo(false, false, true, false));
        mainLayout.addComponent(dpuGeneralSettingsLayoutZip);

        //Layout with buttons Save and Cancel
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setStyleName("dpuDetailButtonBar");
        buttonBar.setMargin(new MarginInfo(true, false, false, false));

        buttonBar.addComponent(createSaveZipButton());

        buttonBar.addComponent(createCancelButton());

        mainLayout.addComponent(buttonBar);

        return mainLayout;
    }

    private TextArea createDpuDescription(GridLayout layout, int row) {
        Label descriptionLabel = new Label(Messages.getString("DPUCreate.description"));
        descriptionLabel.setImmediate(false);
        descriptionLabel.setWidth("-1px");
        descriptionLabel.setHeight("-1px");
        layout.addComponent(descriptionLabel, 0, row);

        TextArea dpuDescription = new TextArea();
        dpuDescription.setImmediate(false);
        dpuDescription.setWidth("310px");
        dpuDescription.setHeight("60px");
        layout.addComponent(dpuDescription, 1, row);
        return dpuDescription;
    }

    public com.vaadin.ui.Component createJarTab() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setStyleName("dpuDetailMainLayout");
        mainLayout.setMargin(true);

        dpuGeneralSettingsLayout = new GridLayout(2, 4);
        dpuGeneralSettingsLayout.setSpacing(true);
        dpuGeneralSettingsLayout.setWidth("400px");
        dpuGeneralSettingsLayout.setHeight("200px");

        //Visibility of DPU Template: label & OptionGroup
        groupVisibility = createVisibilityOption(dpuGeneralSettingsLayout, 2);

        Label selectLabel = new Label(Messages.getString("DPUCreate.select.jar"));
        selectLabel.setImmediate(false);
        selectLabel.setWidth("-1px");
        selectLabel.setHeight("-1px");
        dpuGeneralSettingsLayout.addComponent(selectLabel, 0, 3);

        fileUploadReceiver = new FileUploadReceiver();

        uploadFile = new TextField();
        HorizontalLayout uploadFileLayout = buildUploadLayout(dpuGeneralSettingsLayout, fileUploadReceiver, uploadFile, "jar", 3);

        dpuGeneralSettingsLayout.addComponent(uploadFileLayout, 1, 3);

        dpuGeneralSettingsLayout.setMargin(new MarginInfo(false, false, true,
                false));
        mainLayout.addComponent(dpuGeneralSettingsLayout);

        //Layout with buttons Save and Cancel
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setStyleName("dpuDetailButtonBar");
        buttonBar.setMargin(new MarginInfo(true, false, false, false));

        buttonBar.addComponent(createSaveJarButton());

        buttonBar.addComponent(createCancelButton());

        mainLayout.addComponent(buttonBar);

        return mainLayout;
    }

    private OptionGroup createVisibilityOption(GridLayout layout, int row) {
        Label visibilityLabel = new Label(Messages.getString("DPUCreate.visibility"));
        visibilityLabel.setImmediate(false);
        visibilityLabel.setWidth("-1px");
        visibilityLabel.setHeight("-1px");
        layout.addComponent(visibilityLabel, 0, row);

        OptionGroup grVis = new OptionGroup();
        grVis.addStyleName("horizontalgroup");
        grVis.addItem(ShareType.PRIVATE);
        grVis.setItemCaption(ShareType.PRIVATE, Messages.getString(ShareType.PRIVATE.name()));
        grVis.addItem(ShareType.PUBLIC_RO);
        grVis.setItemCaption(ShareType.PUBLIC_RO, Messages.getString(ShareType.PUBLIC_RO.name()));
        grVis.setValue(ShareType.PUBLIC_RO);

        layout.addComponent(grVis, 1, row);
        return grVis;
    }

    private Button createSaveZipButton() {
        Button saveButton = new Button(Messages.getString("DPUCreate.save"));
        saveButton.setWidth("90px");

        saveButton.addClickListener(new AuthAwareButtonClickWrapper(new ClickListener() {
            /**
             * After pushing the button Save will be checked validation of the
             * mandatory fields: Name, Description and uploadFile. JAR file will
             * be copied from template folder to the /target/dpu/ folder if
             * there no conflicts. After getting all information from JAR file
             * needed to store new DPUTemplateRecord, the record in Database
             * will be created
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                // checking validation of the mandatory fields
                if ((!uploadFileZip.isValid())) {
                    Notification.show(Messages.getString("DPUCreate.save.failed"),
                            Messages.getString("DPUCreate.save.failed.description"),
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                final File sourceFile = fileUploadReceiverZip.getFile();
                Collection<File> dpus;
                List<DPUTemplateRecord> dpusFromXmlFile;
                File tmpDir = null;
                
                try {
                    tmpDir = resourceManager.getNewImportTempDir();
                    ZipCommons.unpack(sourceFile, tmpDir);
                    String[] extensions = { "jar" };
                    dpus = FileUtils.listFiles(tmpDir, extensions, true);

                    dpusFromXmlFile = importFromLstFile(tmpDir);
                } catch (IOException e) {
                    String msg = Messages.getString("DPUCreate.load.failed") + sourceFile.getName();
                    LOG.error(msg);
                    Notification.show(msg, e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ResourceManager.cleanupQuietly(tmpDir);
                    return;
                } catch (ImportException e) {
                    String msg = Messages.getString("DPUCreate.load.failed.list");
                    LOG.error(msg);
                    Notification.show(msg, e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ResourceManager.cleanupQuietly(tmpDir);
                    return;
                } catch (MissingResourceException e) {
                    String msg = Messages.getString("DPUCreate.temp.dir.fail");
                    LOG.error(msg);
                    Notification.show(msg, e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ResourceManager.cleanupQuietly(tmpDir);
                    return;
                }

                if ((dpus == null) || dpus.isEmpty()) {
                    String msg = Messages.getString("DPUCreate.jars.empty") + sourceFile.getName();
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                    LOG.error(msg);
                    ResourceManager.cleanupQuietly(tmpDir);
                    return;
                }

                List<DPUTemplateRecord> templates;
                List<DPUCreateException> caughtExceptions = new ArrayList<DPUCreateException>();

                for (final File fileEntry : dpus) {
                    templates = null;
                    try {
                        if (dpusFromXmlFile != null) {
                            // get only dpu template and children for jar file
                            templates = getTemplatesForJar(fileEntry.getName(), dpusFromXmlFile);
                            if (templates.size() == 0) {
                                continue; // there is xml file but there is no record for dpu
                            }
                        } // else there is no xml file and we import jar files without additional info
                        importDPUsFromZip(fileEntry, templates, caughtExceptions);
                    } catch (DPUCreateException e) {
                        caughtExceptions.add(e);
                    }
                }

                // exception that we caught will be shown in one notification
                if (caughtExceptions.size() != 0) {
                    dpuGeneralSettingsLayoutZip.removeComponent(1, 2);
                    uploadFileZip = new TextField();
                    dpuGeneralSettingsLayoutZip.addComponent(buildUploadLayout(dpuGeneralSettingsLayoutZip, fileUploadReceiverZip, uploadFileZip, "zip", 2), 1, 2);
                    showResultExceptions(caughtExceptions);
                }

                ResourceManager.cleanupQuietly(tmpDir);
                // and at the end we can close the dialog ..
                close();
            }
        }));
        return saveButton;
    }
    
    /**
     * Deletes quietly files or directories (without throwing exception if failed)
     * 
     * @param filesOrDirs
     */
    private static void cleanup(FileUploadReceiver... filesOrDirs) {
        for (FileUploadReceiver receiver : filesOrDirs) {
            ResourceManager.cleanupQuietly(receiver.getParentDir());
        }
    }

    private void showResultExceptions(List<DPUCreateException> caughtExceptions) {
        VerticalLayout content = new VerticalLayout();
        content.setHeight(99, Unit.PERCENTAGE); // get rid of scrollbar
        content.setWidth(100, Unit.PERCENTAGE);
        StringBuilder builder = new StringBuilder();
        for (DPUCreateException exc : caughtExceptions) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append("* ").append(exc.getMessage());
        }

        TextArea text = new TextArea(null, builder.toString());
        text.setSizeFull();
        content.addComponent(text);
        Button btnClose = new Button(Messages.getString("DPUCreate.close"));
        content.addComponent(btnClose);
        content.setComponentAlignment(btnClose, Alignment.BOTTOM_CENTER);
        content.setExpandRatio(text, 1.0f);

        final Window resultDialog = new SimpleDialog(content);
        resultDialog.setCaption(Messages.getString("DPUCreate.result.log"));
        if (!UI.getCurrent().getWindows().contains(resultDialog)) {
            UI.getCurrent().addWindow(resultDialog);
        }

        btnClose.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                resultDialog.close();
            }
        });
    }

    /**
     * Find corresponding dpu template from lst file for dpuJarFileName
     *
     * @param dpuJarFileName
     * @param dpusFromXML
     * @return dpu template, null if there is not one
     */
    protected List<DPUTemplateRecord> getTemplatesForJar(String dpuJarFileName, List<DPUTemplateRecord> dpusFromXML) {
        List<DPUTemplateRecord> returnList = new ArrayList<DPUTemplateRecord>();
        for (DPUTemplateRecord dpuTemplateRecord : dpusFromXML) {
            if (dpuJarFileName.equals(dpuTemplateRecord.getJarName())) {
                returnList.add(dpuTemplateRecord);
            }
        }
        return returnList;
    }

    protected List<DPUTemplateRecord> importFromLstFile(File parentDir) throws ImportException {
        File lstFile = new File(parentDir, ArchiveStructure.DPU_TEMPLATE.getValue());
        if (lstFile.exists()) {
            return dpuImportService.importDPUs(lstFile);
        }
        return null;
    }

    private Button createSaveJarButton() {
        Button saveButton = new Button(Messages.getString("DPUCreate.save.jar"));
        saveButton.setWidth("90px");

        saveButton.addClickListener(new AuthAwareButtonClickWrapper(new ClickListener() {
            /**
             * After pushing the button Save will be checked validation of the
             * mandatory fields: Name, Description and uploadFile. JAR file will
             * be copied from template folder to the /target/dpu/ folder if
             * there no conflicts. After getting all information from JAR file
             * needed to store new DPUTemplateRecord, the record in Database
             * will be created
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                // checking validation of the mandatory fields
                if (!uploadFile.isValid()) {
                    Notification.show(Messages.getString("DPUCreate.save.failed"),
                            Messages.getString("DPUCreate.save.failed.description"),
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                final File sourceFile = fileUploadReceiver.getFile();
                try {
                    importDPU(sourceFile);
                } catch (DPUCreateException e) {
                    dpuGeneralSettingsLayout.removeComponent(1, 3);
                    uploadFile = new TextField();
                    dpuGeneralSettingsLayout.addComponent(buildUploadLayout(dpuGeneralSettingsLayout, fileUploadReceiver, uploadFile, "jar", 3), 1, 3);
                    Notification.show(Messages.getString("DPUCreate.create.failed"),
                            e.getMessage(),
                            Notification.Type.ERROR_MESSAGE);
                    ResourceManager.cleanupQuietly(sourceFile);
                    return;
                }
                close();
            }
        }));
        return saveButton;
    }

    private Button createCancelButton() {
        Button cancelButton = new Button(Messages.getString("DPUCreate.cancel"), new Button.ClickListener() {
            /**
             * Closes DPU Template creation window
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        cancelButton.setWidth("90px");
        return cancelButton;
    }

    private void importDPUsFromZip(File fileEntry, List<DPUTemplateRecord> templates, List<DPUCreateException> caughtExceptions) throws DPUCreateException {
        if (templates == null) { // there was no xml file with templates
            importDPU(fileEntry, null);
            return;
        }

        // at first the parent if exists
        for (DPUTemplateRecord dpuTemplateRecord : templates) {
            if (dpuTemplateRecord.getParent() == null) { // dpu is parent (in imported)
                if (dpuFacade.getByJarName(fileEntry.getName()) == null) { // parent doesnt exist in system
                    importDPU(fileEntry, dpuTemplateRecord);
                    templates.remove(dpuTemplateRecord);
                }
                break;
            }
        }

        // now children
        for (DPUTemplateRecord dpuTemplateRecord : templates) {
            try {
                importDPU(fileEntry, dpuTemplateRecord);
            } catch (DPUCreateException e) {
                caughtExceptions.add(e);
            }
        }
    }

    private void importDPU(File jarFile, DPUTemplateRecord template) throws DPUCreateException {
        String name = null;

        if (template != null && template.getName() != null) {
            name = template.getName();
        }

        DPUTemplateWrap dpuWrap;
        dpuWrap = new DPUTemplateWrap(dpuManipulator.create(jarFile, name), Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE)),
                this.appConfig, this.utils.getUser());

        // set additional variables
        dpuTemplate = dpuWrap.getDPUTemplateRecord();
        // now we know all, we can update the DPU template
        dpuTemplate.setShareType((ShareType) groupVisibilityZip.getValue());

        if (template != null) {
            String value = template.getDescription();
            if (value != null && !value.isEmpty()) {
                dpuTemplate.setDescription(value);
            }
            value = template.getRawConf();
            if (value != null && !value.isEmpty()) {
                dpuTemplate.setRawConf(value);
            }
        }

        dpuFacade.save(dpuTemplate);
    }

    private void importDPU(File fileEntry) throws DPUCreateException {
        DPUTemplateWrap dpuWrap;
        String name = null;

        dpuWrap = new DPUTemplateWrap(dpuManipulator.create(fileEntry, name), Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE)),
                appConfig, this.utils.getUser());
        // set additional variables
        dpuTemplate = dpuWrap.getDPUTemplateRecord();
        // now we know all, we can update the DPU template
        dpuTemplate.setShareType((ShareType) groupVisibility.getValue());
        dpuFacade.save(dpuTemplate);
    }

    private HorizontalLayout buildUploadLayout(final GridLayout layout,
            final FileUploadReceiver fileUploadReceiver,
            final TextField uploadFile,
            final String fileExtension,
            final int row) {

        HorizontalLayout uploadFileLayout = new HorizontalLayout();
        uploadFileLayout.setSpacing(true);

        //JAR file uploader
        final Upload selectFile = new Upload(null, fileUploadReceiver);
        selectFile.setImmediate(true);
        selectFile.setButtonCaption(Messages.getString("DPUCreate.file.choose"));
        selectFile.addStyleName("horizontalgroup");
        selectFile.setHeight("40px");
        
        selectFile.addStartedListener(new StartedListener() {
            /**
             * Upload start listener. If selected file has JAR extension then an
             * upload status window with upload progress bar will be shown. If
             * selected file has other extension, then upload will be
             * interrupted and error notification will be shown.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadStarted(final StartedEvent event) {
                cleanup(fileUploadReceiver);
                
                String filename = event.getFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

                if (!fileExtension.equals(extension)) {
                    selectFile.interruptUpload();
                    Notification.show(
                            Messages.getString("DPUCreate.selected.file", fileExtension),
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (getUploadInfoWindow().getParent() == null) {
                    UI.getCurrent().addWindow(getUploadInfoWindow());
                }
                getUploadInfoWindow().setClosable(false);
            }
        });

        //If upload failed, upload window will be closed
        selectFile.addFailedListener(new Upload.FailedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadFailed(FailedEvent event) {

                getUploadInfoWindow().setClosable(true);
                getUploadInfoWindow().close();
                layout.removeComponent(1, row);
                layout.addComponent(buildUploadLayout(layout, fileUploadReceiver, uploadFile, fileExtension, row), 1, row);

            }
        });

        //If upload finish successful, upload window will be closed and the name
        //of the uploaded file will be shown
        selectFile.addSucceededListener(new Upload.SucceededListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadSucceeded(final SucceededEvent event) {

                getUploadInfoWindow().setClosable(true);
                getUploadInfoWindow().close();
                uploadFile.setReadOnly(false);
                uploadFile.setValue(event.getFilename());
                uploadFile.setReadOnly(true);

            }
        });
        // Upload status window
        setUploadInfoWindow(new UploadInfoWindow(selectFile));

        uploadFileLayout.addComponent(selectFile);

        uploadFile.setWidth("210px");
        uploadFile.setReadOnly(true);
        //set mandatory to uploadFile text field.
        uploadFile.addValidator(new Validator() {
            private static final long serialVersionUID = -1928722403511645932L;

            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException(Messages.getString("DPUCreate.upload"));

            }
        });

        uploadFileLayout.addComponent(uploadFile);

        return uploadFileLayout;

    }

    /**
     * Reset the component to empty values.
     */
    public void initClean() {
        groupVisibility.setValue(ShareType.PUBLIC_RO);
        groupVisibility.setEnabled(this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_SET_VISIBILITY));
        uploadFile.setReadOnly(false);
        uploadFile.setValue("");
        uploadFile.setReadOnly(true);
        // clean zip version
        groupVisibilityZip.setValue(ShareType.PUBLIC_RO);
        groupVisibilityZip.setEnabled(this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_SET_VISIBILITY));
        uploadFileZip.setReadOnly(false);
        uploadFileZip.setValue("");
        uploadFileZip.setReadOnly(true);
    }
}
