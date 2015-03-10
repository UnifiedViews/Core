package cz.cuni.mff.xrg.odcs.frontend.gui.views.dpu;

import java.io.FileNotFoundException;
import java.util.Locale;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.RuntimePropertiesFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.MaxLengthValidator;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUTemplateWrap;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException;
import cz.cuni.mff.xrg.odcs.frontend.gui.AuthAwareUploadSucceededWrapper;
import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DPUTree;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.FileUploadReceiver;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.UploadInfoWindow;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.ActionColumnGenerator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.dpu.DPUPresenter.DPUView;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

/**
 * @author Bogo
 */
@Component
@Scope("prototype")
public class DPUViewImpl extends CustomComponent implements DPUView {

    private static final int COLUMN_ACTIONS_WIDTH = 160;

    private DPUPresenter presenter;

    private static final long serialVersionUID = 1L;

    private VerticalLayout mainLayout;

    private VerticalLayout verticalLayoutData; //Layout contains General tab components of {@link #tabSheet}.

    private VerticalLayout verticalLayoutConfigure;// Layout contains Template Configuration tab components of {@link #tabSheet}.

    private VerticalLayout verticalLayoutInstances;//Layout contains DPU instances tab components of {@link #tabSheet}.

    private VerticalLayout dpuDetailLayout; //Layout contains DPU Template details.

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private DPUTree dpuTree;// Tree contains available DPUs.

    private TextField dpuName; // name of selected DPU Template

    private TextArea dpuDescription; // description of selected DPU Template

    private Upload reloadFile; // button for reload JAR file

    private FileUploadReceiver fileUploadReceiver;

    /**
     * Window with upload info.
     */
    public static UploadInfoWindow uploadInfoWindow;

    private boolean errorExtension = false;

    private Label jarPath;

    /**
     * DPU Template details TabSheet contains General, Template Configuration,
     * DPU instances tabs
     */
    private TabSheet tabSheet;

    private OptionGroup groupVisibility; // Visibility of DPU Template: public or private

    private HorizontalLayout dpuLayout; // Layout contains DPU Templates tree and DPU Template details.

    private HorizontalLayout buttonDpuBar; // Layout contains action buttons of DPU Template details.

    private HorizontalLayout layoutInfo; // Layout with the information that no DPU template was selected.

    /**
     * Table with instances of DPU. Located on {@link #tabSheet} DPU instances
     * tab.
     */
    private IntlibPagedTable instancesTable;

    private IndexedContainer tableData; //container with instancesTable data

    /**
     * Wrap for selected DPUTemplateRecord.
     */
    private DPUTemplateWrap selectedDpuWrap = null;

    private static final Logger LOG = LoggerFactory.getLogger(ViewComponent.class);

    private Button buttonSaveDPU;

    private String tabname;

    private Page.BrowserWindowResizeListener resizeListener = null;

    private Panel dpuTreePanel;

    @Autowired
    private Utils utils;

    /**
     * Constructor.
     */
    public DPUViewImpl() {
    }

    @Override
    public Object enter(DPUPresenter presenter) {
        this.presenter = presenter;
        setupResizeListener();

        if (!presenter.isLayoutInitialized()) {
            buildMainLayout();
        }

        setCompositionRoot(mainLayout);
        return this;
    }

    /**
     * Layout contains DPU Templates page elements: buttons on the top: "Create
     * DPU", "Import DPU", "Export All"; layout with DPU Templates tree {@link DPUTree} and DPU Template details
     *
     * @return mainLayout VerticalLayout with all components of DPU Templates
     *         page.
     */
    private VerticalLayout buildMainLayout() {
        // top-level component properties
        setSizeFull();

        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(true);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();
        mainLayout.setStyleName("mainLayout");

        // Buttons on the top: "Create DPU", "Import DPU", "Export All"
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setSpacing(true);

        Button buttonCreateDPU = new Button();
        buttonCreateDPU.setCaption(Messages.getString("DPUViewImpl.create.template"));
        buttonCreateDPU.setHeight("25px");
        buttonCreateDPU.setWidth("150px");
        buttonCreateDPU.addStyleName("v-button-primary");
        buttonCreateDPU.setVisible(utils.hasUserAuthority("dpuTemplate.create"));
        buttonCreateDPU.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.openDPUCreateEventHandler();
            }
        });
        buttonBar.addComponent(buttonCreateDPU);

        Button buttonImportDPU = new Button();
        buttonImportDPU.setVisible(false);
        buttonImportDPU.setCaption(Messages.getString("DPUViewImpl.import.template"));
        buttonImportDPU.setHeight("25px");
        buttonImportDPU.setWidth("150px");
        buttonImportDPU.addStyleName("v-button-primary");
        buttonImportDPU.setEnabled(false);
        buttonImportDPU.setVisible(utils.hasUserAuthority("dpuTemplate.import"));
        buttonImportDPU.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.importDPUTemplateEventHandler();
            }
        });
        buttonBar.addComponent(buttonImportDPU);

        Button buttonExportAll = new Button();
        buttonExportAll.setVisible(false);
        buttonExportAll.setCaption(Messages.getString("DPUViewImpl.export.all"));
        buttonExportAll.setHeight("25px");
        buttonExportAll.setWidth("150px");
        buttonExportAll.addStyleName("v-button-primary");
        buttonExportAll.setEnabled(false);
        buttonExportAll.setVisible(utils.hasUserAuthority("dpuTemplate.export"));
        buttonExportAll
                .addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        presenter.exportAllEventHandler();
                    }
                });
        buttonBar.addComponent(buttonExportAll);

        mainLayout.addComponent(buttonBar);
        mainLayout.setExpandRatio(buttonBar, 0.0f);

        //layout with  DPURecord tree and DPURecord details
        dpuLayout = buildDpuLayout();
        mainLayout.addComponent(dpuLayout);
        mainLayout.setExpandRatio(dpuLayout, 1.0f);

        return mainLayout;
    }

    /**
     * Builds layout contains DPU Templates tree {@link DPUTree} and DPU
     * Template details. Calls from {@link #buildMainLayout}
     *
     * @return dpuLayout GridLayout contains {@link DPUTree} and {@link #buildDPUDetailLayout}.
     */
    private HorizontalLayout buildDpuLayout() {
        dpuLayout = new HorizontalLayout();
        dpuLayout.setSizeFull();
        dpuLayout.setSpacing(true);

        // Layout with the information that no DPU template was selected.
        layoutInfo = new HorizontalLayout();
        layoutInfo.setHeight("100%");
        layoutInfo.setWidth("100%");

        Label infoLabel = new Label();
        infoLabel.setImmediate(false);
        infoLabel.setWidth("-1px");
        infoLabel.setHeight("-1px");
        infoLabel.setValue(Messages.getString("DPUViewImpl.select.dpu.info"));
        infoLabel.setContentMode(ContentMode.HTML);

        layoutInfo.addComponent(infoLabel);
        layoutInfo.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);

        //DPU Template Tree
        dpuTree.setExpandable(false);
        dpuTree.fillTree();
        if (dpuTree.getListeners(ItemClickEvent.class).isEmpty()) {
            dpuTree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void itemClick(final ItemClickEvent event) {
                    if (event.getItemId().getClass() == DPUTemplateRecord.class) {
                        presenter.selectDPUEventHandler((DPUTemplateRecord) event.getItemId());
                    }
                }
            });
        }
        dpuTree.setSizeUndefined();

        dpuTreePanel = new Panel();
        setDpuTreeMaxHeight(Page.getCurrent().getBrowserWindowHeight());
        dpuTreePanel.setWidth("-1px");
        dpuTreePanel.setContent(dpuTree);

        dpuLayout.addComponent(dpuTreePanel);
        dpuLayout.setExpandRatio(dpuTreePanel, 0.0f);

        dpuLayout.addComponent(layoutInfo);
        dpuLayout.setExpandRatio(layoutInfo, 1.0f);

        return dpuLayout;
    }

    /**
     * Builds layout with DPU Template details of DPU selected in the tree. DPU
     * Template details represents by {@link #tabSheet}. Calls from {@link #buildDpuLayout}
     *
     * @return dpuDetailLayout VerticalLayout with {@link #tabSheet} that
     *         contain all DPU Template details components.
     */
    private VerticalLayout buildDPUDetailLayout() {

        dpuDetailLayout = new VerticalLayout();
        dpuDetailLayout.setImmediate(true);
        dpuDetailLayout.setStyleName("dpuDetailLayout");
        dpuDetailLayout.setMargin(true);
        dpuDetailLayout.setSizeFull();

        //DPU Details TabSheet
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {

                if (buttonSaveDPU != null) {
                    tabname = event.getTabSheet().getSelectedTab().getCaption();
                    if (isChanged() || tabname.equals("configuration")) {
                        buttonSaveDPU.setEnabled(presenter.hasPermission("dpuTemplate.save"));
                    } else {
                        buttonSaveDPU.setEnabled(false);
                    }
                }
            }
        });

        //General tab. Contains informations: name, description, visibility,
        //information about JAR file.
        verticalLayoutData = buildVerticalLayoutData();

        verticalLayoutData.setCaption("general");
        TabSheet.Tab dataTab = tabSheet.addTab(verticalLayoutData, Messages.getString("DPUViewImpl.general"));
        //Template Configuration tab. Contains information about configuration
        //from JAR file
        verticalLayoutConfigure = new VerticalLayout();

        verticalLayoutConfigure.setImmediate(false);
        verticalLayoutConfigure.setMargin(true);
        verticalLayoutConfigure.setCaption("configuration");
        tabSheet.addTab(verticalLayoutConfigure, Messages.getString("DPUViewImpl.template.configuration"));
        tabSheet.setSelectedTab(dataTab);
        if (selectedDpuWrap != null) {
            AbstractConfigDialog<?> configDialog = null;
            //getting configuration dialog of selected DPU Template
            try {
                configDialog = selectedDpuWrap.getDialog();
            } catch (ModuleException ex) {
                Notification.show(
                        Messages.getString("DPUViewImpl.fileNotFound.configuration.load.fail"),
                        ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                LOG.error("Can't load DPU '{}'", selectedDpuWrap.getDPUTemplateRecord().getId(), ex);
            } catch (FileNotFoundException ex) {
                Notification.show(
                        Messages.getString("DPUViewImpl.fileNotFound"),
                        ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                LOG.error("Can't load DPU '{}'", selectedDpuWrap.getDPUTemplateRecord().getId(), ex);
            } catch (Exception ex) {
                Notification.show(Messages.getString("DPUViewImpl.exception.configuration.fail"), ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                LOG.error("Can't load DPU '{}'", selectedDpuWrap.getDPUTemplateRecord().getId(), ex);
            }

            verticalLayoutConfigure.removeAllComponents();
            if (configDialog == null) {
                // use some .. dummy component
            } else {
                // configure
                configureDPUDialog();
                verticalLayoutConfigure.addComponent(configDialog);
            }
        }
        //DPU instances tab. Contains pipelines using the given DPU.
        verticalLayoutInstances = buildVerticalLayoutInstances();

        verticalLayoutInstances.setCaption("instances");
        tabSheet.addTab(verticalLayoutInstances, Messages.getString("DPUViewImpl.dpu.instances"));

        dpuDetailLayout.addComponent(tabSheet);
        dpuDetailLayout.setExpandRatio(tabSheet, 1.0f);

        buttonDpuBar = buildDPUButtonBar();
        dpuDetailLayout.addComponent(buttonDpuBar);
        dpuDetailLayout.setExpandRatio(buttonDpuBar, 0.0f);

        return dpuDetailLayout;
    }

    /**
     * Building layout contains action buttons of DPU Template details. Copy,
     * Delete, Export, Save.
     *
     * @return buttonDpuBar HorizontalLayout contains action buttons.
     */
    private HorizontalLayout buildDPUButtonBar() {

        buttonDpuBar = new HorizontalLayout();
        buttonDpuBar.setSizeUndefined();
        buttonDpuBar.setHeight("30px");
        buttonDpuBar.setSpacing(true);

        final DPUTemplateRecord selectedDpu = selectedDpuWrap.getDPUTemplateRecord();

        // Copy DPU Template Button, may copy only DPU of 3 level.
        Button buttonCopyDPU = new Button();
        buttonCopyDPU.setCaption(Messages.getString("DPUViewImpl.copy"));
        buttonCopyDPU.setHeight("25px");
        buttonCopyDPU.setWidth("100px");
        buttonCopyDPU.setEnabled(presenter.hasPermission("dpuTemplate.copy"));
        buttonCopyDPU.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.copyDPUEventHandler();

                // refresh data in dpu tree
                dpuTree.refresh();

            }
        });
        buttonDpuBar.addComponent(buttonCopyDPU);
        buttonDpuBar.setComponentAlignment(buttonCopyDPU, Alignment.BOTTOM_LEFT);

        // Delete DPU Template Button
        Button buttonDeleteDPU = new Button();
        buttonDeleteDPU.setCaption(Messages.getString("DPUViewImpl.delete"));
        buttonDeleteDPU.setHeight("25px");
        buttonDeleteDPU.setWidth("100px");
        buttonDeleteDPU.setEnabled(presenter.hasPermission("dpuTemplate.delete"));
        buttonDeleteDPU.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //open confirmation dialog
                ConfirmDialog.show(
                        UI.getCurrent(),
                        Messages.getString("DPUViewImpl.delete.confirmation"),
                        Messages.getString("DPUViewImpl.delete.confirmation.description", selectedDpu.getName().toString()), Messages.getString("DPUViewImpl.delete.confirmation.deleteButton"), Messages.getString("DPUViewImpl.delete.confirmation.cancelButton"),
                        new ConfirmDialog.Listener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onClose(ConfirmDialog cd) {
                                if (cd.isConfirmed()) {
                                    presenter.deleteDPUEventHandler();
                                }
                            }
                        });

            }
        });
        buttonDpuBar.addComponent(buttonDeleteDPU);
        buttonDpuBar.setComponentAlignment(buttonDeleteDPU,
                Alignment.BOTTOM_LEFT);

        // Export DPU Template Button
        Button buttonExportDPU = new Button();
        buttonExportDPU.setVisible(false);
        buttonExportDPU.setCaption(Messages.getString("DPUViewImpl.export"));
        buttonExportDPU.setHeight("25px");
        buttonExportDPU.setWidth("100px");
        buttonExportDPU.setEnabled(presenter.hasPermission("dpuTemplate.export"));
        buttonExportDPU.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
            }
        });
        buttonDpuBar.addComponent(buttonExportDPU);
        buttonDpuBar.setComponentAlignment(buttonExportDPU,
                Alignment.BOTTOM_LEFT);

        // Save DPU Template Button
        buttonSaveDPU = new Button();
        buttonSaveDPU.setCaption(Messages.getString("DPUViewImpl.save"));
        buttonSaveDPU.setHeight("25px");
        buttonSaveDPU.setWidth("100px");
        buttonSaveDPU.setEnabled(false);
        buttonSaveDPU.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveDPUTemplate();
                //refresh data in dialog and dpu tree
                dpuTree.refresh();
                setGeneralTabValues();
                if (!"configuration".equals(tabname)) {
                    buttonSaveDPU.setEnabled(false);
                }
                // refresh configuration
                configureDPUDialog();
            }
        });
        buttonDpuBar.addComponent(buttonSaveDPU);
        buttonDpuBar.setComponentAlignment(buttonSaveDPU,
                Alignment.BOTTOM_RIGHT);
        dpuDetailLayout.addComponent(buttonDpuBar);

        return buttonDpuBar;
    }

    /**
     * Set values to components {@link #dpuName}, {@link #dpuDescription}, {@link #groupVisibility}
     */
    public void setGeneralTabValues() {

        String selectedDpuName = selectedDpuWrap.getDPUTemplateRecord().getName();
        String selecteDpuDescription =
                selectedDpuWrap.getDPUTemplateRecord().isUseDPUDescription() ? "" :
                        selectedDpuWrap.getDPUTemplateRecord().getDescription();
        ShareType selecteDpuVisibility = selectedDpuWrap.getDPUTemplateRecord().getShareType();
        dpuName.setValue(selectedDpuName);
        dpuName.setReadOnly(!presenter.hasPermission("dpuTemplate.save"));
        dpuDescription.setValue(selecteDpuDescription);
        dpuDescription.setReadOnly(!presenter.hasPermission("dpuTemplate.save"));

        groupVisibility.setValue(selecteDpuVisibility);
        if (selecteDpuVisibility == ShareType.PUBLIC_RO) {
            groupVisibility.setValue(selecteDpuVisibility);
            groupVisibility.setEnabled(false);
        } else {
            groupVisibility.setValue(selecteDpuVisibility);
            groupVisibility.setEnabled(true);
            groupVisibility.addValueChangeListener(new Property.ValueChangeListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    buttonSaveDPU.setEnabled(presenter.hasPermission("dpuTemplate.save"));
                }
            });
        }
    }

    /**
     * Builds layout contains General tab of {@link #tabSheet}. Calls from {@link #buildDPUDetailLayout}
     *
     * @return verticalLayoutData VerticalLayout with all components of General
     *         tab.
     */
    private VerticalLayout buildVerticalLayoutData() {

        // common part: create layout
        verticalLayoutData = new VerticalLayout();
        verticalLayoutData.setImmediate(false);
        verticalLayoutData.setWidth("100.0%");
        verticalLayoutData.setMargin(true);

        //Layout contains name description and visibility of DPU Template
        GridLayout dpuSettingsLayout = new GridLayout(2, 5);
        dpuSettingsLayout.setStyleName("dpuSettingsLayout");
        dpuSettingsLayout.setMargin(true);
        dpuSettingsLayout.setSpacing(true);
        dpuSettingsLayout.setWidth("100%");
        dpuSettingsLayout.setHeight("100%");
        dpuSettingsLayout.setColumnExpandRatio(0, 0.0f);
        dpuSettingsLayout.setColumnExpandRatio(1, 1.0f);

        //Name of DPU Template: label & TextField
        Label nameLabel = new Label(Messages.getString("DPUViewImpl.name"));
        nameLabel.setImmediate(false);
        nameLabel.setWidth("-1px");
        nameLabel.setHeight("-1px");
        dpuSettingsLayout.addComponent(nameLabel, 0, 0);
        dpuName = new TextField();
        dpuName.setImmediate(true);
        dpuName.setWidth("100%");
        dpuName.setHeight("-1px");
        //settings of mandatory
        dpuName.addValidator(new Validator() {

            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException(Messages.getString("DPUViewImpl.name.empty"));
            }
        });
        dpuName.addValidator(new MaxLengthValidator(LenghtLimits.DPU_NAME));
        dpuSettingsLayout.addComponent(dpuName, 1, 0);
        dpuName.addTextChangeListener(new FieldEvents.TextChangeListener() {

            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                buttonSaveDPU.setEnabled(presenter.hasPermission("save"));
            }
        });

        //Description of DPU Template: label & TextArea
        Label descriptionLabel = new Label(Messages.getString("DPUViewImpl.description"));
        descriptionLabel.setImmediate(false);
        descriptionLabel.setWidth("-1px");
        descriptionLabel.setHeight("-1px");
        dpuSettingsLayout.addComponent(descriptionLabel, 0, 1);
        dpuDescription = new TextArea();
        dpuDescription.addValidator(new MaxLengthValidator(MaxLengthValidator.DESCRIPTION_LENGTH));
        dpuDescription.setImmediate(true);
        dpuDescription.setWidth("100%");
        dpuDescription.setHeight("60px");
        dpuDescription.addTextChangeListener(new FieldEvents.TextChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                buttonSaveDPU.setEnabled(presenter.hasPermission("dpuTemplate.save"));
            }
        });
        dpuSettingsLayout.addComponent(dpuDescription, 1, 1);

        //Visibility of DPU Template: label & OptionGroup
        Label visibilityLabel = new Label(Messages.getString("DPUViewImpl.visibility"));
        dpuSettingsLayout.addComponent(visibilityLabel, 0, 2);
        groupVisibility = new OptionGroup();
        groupVisibility.addStyleName("horizontalgroup");
        groupVisibility.addItem(ShareType.PRIVATE);
        groupVisibility.setItemCaption(ShareType.PRIVATE, Messages.getString(ShareType.PRIVATE.name()));
        groupVisibility.addItem(ShareType.PUBLIC_RO);
        groupVisibility.setItemCaption(ShareType.PUBLIC_RO, Messages.getString(ShareType.PUBLIC_RO.name()));
        dpuSettingsLayout.addComponent(groupVisibility, 1, 2);

        // JAR path of DPU Template.
        HorizontalLayout jarPathLayout = new HorizontalLayout();
        jarPathLayout.setImmediate(false);
        jarPathLayout.setSpacing(true);
        jarPathLayout.setHeight("100%");
        dpuSettingsLayout.addComponent(new Label(Messages.getString("DPUViewImpl.jar.path")), 0, 3);

        jarPath = new Label();
        String jarPathText = selectedDpuWrap.getDPUTemplateRecord().getJarPath();
        if (jarPathText.length() > 64) {
            jarPath.setValue("..." + jarPathText.substring(jarPathText.length() - 61));
        } else {
            jarPath.setValue(jarPathText);
        }
        jarPath.setDescription(jarPathText);

        //reload JAR file button
        fileUploadReceiver = new FileUploadReceiver();
        reloadFile = new Upload(null, fileUploadReceiver);
        reloadFile.setImmediate(true);
        reloadFile.setButtonCaption(Messages.getString("DPUViewImpl.replace"));
        reloadFile.addStyleName("horizontalgroup");
        reloadFile.setHeight("40px");
        reloadFile.setEnabled(presenter.hasPermission("dpuTemplate.save"));
        reloadFile.addStartedListener(new Upload.StartedListener() {
            /**
             * Upload start presenter. If selected file has JAR extension then
             * an upload status window with upload progress bar will be shown.
             * If selected file has other extension, then upload will be
             * interrupted and error notification will be shown.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadStarted(final Upload.StartedEvent event) {
                String filename = event.getFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
                String jar = "jar";

                if (!jar.equals(extension)) {
                    reloadFile.interruptUpload();
                    errorExtension = true;
                    Notification.show(Messages.getString("DPUViewImpl.file.not.jar"), Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (uploadInfoWindow.getParent() == null) {
                    UI.getCurrent().addWindow(uploadInfoWindow);
                }
                uploadInfoWindow.setClosable(false);
            }
        });

        reloadFile.addSucceededListener(new AuthAwareUploadSucceededWrapper(new Upload.SucceededListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadSucceeded(Upload.SucceededEvent event) {
                uploadInfoWindow.close();
                if (!errorExtension) {
                    presenter.dpuUploadedEventHandler(fileUploadReceiver.getFile());
                }
            }
        }));

        reloadFile.addFailedListener(new Upload.FailedListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void uploadFailed(Upload.FailedEvent event) {
                uploadInfoWindow.close();
                if (errorExtension) {
                    errorExtension = false;
                }

                Notification.show(Messages.getString("DPUViewImpl.uploding.failed", event.getFilename()), Notification.Type.ERROR_MESSAGE);
            }
        });

        // Upload status window
        uploadInfoWindow = new UploadInfoWindow(reloadFile);

        jarPathLayout.addComponent(jarPath);
        jarPathLayout.addComponent(reloadFile);
        dpuSettingsLayout.addComponent(jarPathLayout, 1, 3);

        // Description of JAR of DPU Template.
        dpuSettingsLayout.addComponent(new Label(Messages.getString("DPUViewImpl.jar.description")), 0, 4);
        RichTextArea jDescription = new RichTextArea();
        jDescription.setValue(selectedDpuWrap.getDPUTemplateRecord().getJarDescription());
        jDescription.setReadOnly(true);
        jDescription.setWidth("100%");

        dpuSettingsLayout.addComponent(jDescription, 1, 4);

        verticalLayoutData.addComponent(dpuSettingsLayout);

        return verticalLayoutData;
    }

    @Override
    public void refresh() {
        dpuTree.refresh();
    }

    @Override
    public boolean isChanged() {
        if (selectedDpuWrap == null) {
            return false;
        }
        boolean configChanged = false;
        try {
            configChanged = selectedDpuWrap.hasConfigChanged();
        } catch (DPUWrapException e) {
            Notification.show(
                    Messages.getString("DPUViewImpl.dpu.configuration.changes"),
                    e.getMessage(), Notification.Type.ERROR_MESSAGE);
            LOG.error("hasConfigChanged() throws for DPU '{}'",
                    selectedDpuWrap.getDPUTemplateRecord().getId(), e);
            return true;
        }

        DPUTemplateRecord selectedDpu = selectedDpuWrap.getDPUTemplateRecord();

        if (!dpuName.getValue().equals(selectedDpu.getName())) {
            return true;
        } else if (
        // we are not in dpuDescriptionMode
        !(dpuDescription.getValue().isEmpty() && selectedDpu.isUseDPUDescription())
                &&
                !dpuDescription.getValue().equals(selectedDpu.getDescription())) {
            return true;
        } else if (!groupVisibility.getValue().equals(selectedDpu.getShareType())) {
            return true;
        } else if (!jarPath.getDescription().equals(selectedDpu.getJarPath())) {
            return true;
        } else if (configChanged) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Store DPU Template record to DB
     */
    @Override
    public void saveDPUTemplate() {
        //control of the validity of Name field.
        if (!validate()) {
            //Notification.show("Failed to save DPURecord", "Mandatory fields should be filled", Notification.Type.ERROR_MESSAGE);
            return;
        }
        //saving Name, Description and Visibility
        if (selectedDpuWrap != null
                && selectedDpuWrap.getDPUTemplateRecord().getId() != null) {
            selectedDpuWrap.getDPUTemplateRecord().setName(dpuName.getValue().trim());

            if (dpuDescription.getValue() == null ||
                    dpuDescription.getValue().isEmpty()) {
                selectedDpuWrap.getDPUTemplateRecord().setUseDPUDescription(true);
                try {
                    selectedDpuWrap.getDPUTemplateRecord().setDescription(
                            selectedDpuWrap.getDialog().getDescription());
                } catch (ModuleException | DPUWrapException | FileNotFoundException ex) {
                    LOG.error("Can't get DPU description. Empty used as default.", ex);
                    selectedDpuWrap.getDPUTemplateRecord().setDescription("");
                }
            } else {
                selectedDpuWrap.getDPUTemplateRecord().setUseDPUDescription(false);
                selectedDpuWrap.getDPUTemplateRecord().setDescription(dpuDescription
                        .getValue().trim());
            }

            selectedDpuWrap.getDPUTemplateRecord()
                    .setShareType((ShareType) groupVisibility
                            .getValue());
            presenter.saveDPUEventHandler(selectedDpuWrap);
        }
    }

    @Override
    public void selectNewDPU(DPUTemplateRecord dpu) {
        //If DPURecord that != null was selected then it's details will be shown.
        if (dpu != null && dpu.getId() != null) {
            // crate new wrap
            selectedDpuWrap = new DPUTemplateWrap(dpu, Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE)));

            if (dpuDetailLayout != null) {
                dpuLayout.removeComponent(dpuDetailLayout);
            }
            dpuLayout.removeComponent(layoutInfo);
            dpuDetailLayout = buildDPUDetailLayout();
            dpuLayout.addComponent(dpuDetailLayout, 1);
            dpuLayout.setExpandRatio(dpuDetailLayout, 5);

            // show/hide replace button
            reloadFile.setVisible(selectedDpuWrap.getDPUTemplateRecord().jarFileReplacable());

            setGeneralTabValues();
            //Otherwise, the information layout will be shown.
        } else {
            if (dpuDetailLayout != null) {
                dpuLayout.removeComponent(dpuDetailLayout);
            }
            dpuLayout.removeComponent(layoutInfo);
            dpuLayout.addComponent(layoutInfo, 1);
            dpuLayout.setExpandRatio(layoutInfo, 1.0f);
        }
    }

    /**
     * Builds layout contains DPU instances tab of {@link #tabSheet}. Calls from {@link #buildDPUDetailLayout}
     *
     * @return verticalLayoutInstances VerticalLayout with all components of DPU
     *         instances tab.
     */
    private VerticalLayout buildVerticalLayoutInstances() {

        // common part: create layout
        verticalLayoutInstances = new VerticalLayout();
        verticalLayoutInstances.setImmediate(false);
        verticalLayoutInstances.setWidth("100.0%");
        verticalLayoutInstances.setMargin(true);

        tableData = presenter.getTableData(selectedDpuWrap.getDPUTemplateRecord());

        //Table with instancesof DPU
        instancesTable = new IntlibPagedTable();
        instancesTable.setSelectable(true);
        instancesTable.setCaption(Messages.getString("DPUViewImpl.pipelines"));
        instancesTable.setContainerDataSource(tableData);

        //sorting by id
        Object property = "id";
        instancesTable.setSortContainerPropertyId(property);
        instancesTable.setSortAscending(true);
        instancesTable.sort();
        instancesTable.setPageLength(utils.getPageLength());
        instancesTable.setWidth("100%");
        instancesTable.setImmediate(true);
//		instancesTable.setVisibleColumns((Object[]) visibleCols);
//		instancesTable.setColumnHeaders(headers);

        instancesTable.setColumnWidth("actions", COLUMN_ACTIONS_WIDTH);
        instancesTable.addGeneratedColumn("actions",
                createActionColumn());

        instancesTable.setVisibleColumns("actions", "name");
        instancesTable.setColumnCollapsingAllowed(true);

        verticalLayoutInstances.addComponent(instancesTable);
        verticalLayoutInstances.addComponent(instancesTable.createControls());
        instancesTable.setFilterFieldVisible("actions", false);

        return verticalLayoutInstances;
    }

    /**
     * Validate DPU detail.
     *
     * @return If DPU detail is valid
     */
    public boolean validate() {
        try {
            dpuName.validate();
            dpuDescription.validate();
        } catch (Validator.InvalidValueException e) {
            Notification.show(Messages.getString("DPUViewImpl.validation.error"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Load configuration into DPU's configuration dialog. In case of exception
     * show notification.
     */
    private void configureDPUDialog() {
        // refresh configuration
        try {
            selectedDpuWrap.configuredDialog();
        } catch (DPUConfigException e) {
            Notification.show(
                    Messages.getString("DPUViewImpl.configuration.problem"),
                    e.getMessage(), Notification.Type.WARNING_MESSAGE);
            LOG.error("Failed to load configuration for {}", selectedDpuWrap.getDPUTemplateRecord().getId(), e);
        } catch (DPUWrapException e) {
            Notification.show(
                    Messages.getString("DPUViewImpl.unexpected.error"),
                    e.getMessage(), Notification.Type.WARNING_MESSAGE);
            LOG.error("Unexpected error while loading dialog for {}", selectedDpuWrap.getDPUTemplateRecord().getId(), e);
        }
    }

    /**
     * Generate column in table {@link #instancesTable}. with buttons:Detail,
     * Delete, Status.
     *
     * @author Maria Kukhar
     */
    private ActionColumnGenerator createActionColumn() {
        ActionColumnGenerator generator = new ActionColumnGenerator();

        // prepare pipeline detail button
        ActionColumnGenerator.Action detailAction = new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.pipelineDetailEventHandler(id);
            }
        };
        ActionColumnGenerator.ButtonShowCondition detailShowCondition = new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.showPipelineDetailButton(id);
            }
        };

        // prepare pipeline delete button
        ActionColumnGenerator.Action deleteAction = new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.pipelineDeleteEventHandler(id);
            }
        };
        ActionColumnGenerator.ButtonShowCondition deleteShowCondition = new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.showPipelineDeleteButton(id);
            }
        };

        // add buttons to column generator
        generator.addButton(Messages.getString("DPUViewImpl.actionColumn.detail"), null, detailAction, detailShowCondition, new ThemeResource("icons/gear.svg"));
        generator.addButton(Messages.getString("DPUViewImpl.actionColumn.delete"), null, deleteAction, deleteShowCondition, new ThemeResource("icons/trash.svg"));
        generator.addButton(Messages.getString("DPUViewImpl.actionColumn.status"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.pipelineStatusEventHandler(id);
            }
        }, new ThemeResource("icons/log.svg"));

        return generator;
    }

    @Override
    public void removePipelineFromTable(long id) {
        tableData.removeItem(id);
    }

    private void setupResizeListener() {
        if (resizeListener == null) {
            resizeListener = new Page.BrowserWindowResizeListener() {
                @Override
                public void browserWindowResized(Page.BrowserWindowResizeEvent event) {
                    setDpuTreeMaxHeight(event.getHeight());
                }
            };
        }
        Page.getCurrent().removeBrowserWindowResizeListener(resizeListener);
        Page.getCurrent().addBrowserWindowResizeListener(resizeListener);
    }

    private void setDpuTreeMaxHeight(int windowHeight) {
        dpuTreePanel.setHeight(windowHeight - 120, Unit.PIXELS);
    }
}
