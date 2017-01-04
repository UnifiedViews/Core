package cz.cuni.mff.xrg.odcs.frontend.gui.dialogs;

import java.io.FileNotFoundException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUInstanceWrap;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DPUConfigHolder;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DPUGeneralDetail;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.SPARQLValidationException;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Detail of selected DPU. Consists of common properties, name and description
 * and configuration dialog specific for DPU, which is loaded from DPU's jar
 * file.
 *
 * @author Škoda Petr
 * @author Bogo
 */
public class DPUDetail extends Window {

    private final static Logger LOG = LoggerFactory.getLogger(DPUDetail.class);

    /**
     * Current DPU instance.
     */
    private DPUInstanceWrap dpuInstance;

    private final DPUFacade dpuFacade;

    private DPUGeneralDetail generalDetail;

    private DPUConfigHolder configHolder;

    private AppConfig appConfig;

    private Button btnSaveAsNew;

    private Button btnSaveAndCommit;

    private Button btnCancel;

    private Button btnCopyFromTemplate;

    private boolean result;

    private PermissionUtils permissionUtils;

    private Utils utils;

    /**
     * Basic constructor, takes DPUFacade. In order to generate the layout call {@link #build()}. The build function has to be called before any other
     * function.
     *
     * @param dpuFacade
     */
    public DPUDetail(DPUFacade dpuFacade, AppConfig appConfig, Utils utils, PermissionUtils permissionUtils) {
        this.appConfig = appConfig;
        this.dpuFacade = dpuFacade;
        this.utils = utils;
        this.permissionUtils = permissionUtils;
        // build the layout
        build();
        // set dialog properties
        setModal(true);
        setResizable(true);
        // set initial size
        setWidth("75%");
        setHeight("75%");
    }

    /**
     * Construct page layout.
     */
    private void build() {
        // panel for DPU detail dialog
        configHolder = new DPUConfigHolder();

        // the DPU general info
        generalDetail = new DPUGeneralDetail(this.configHolder, this.appConfig);

        HorizontalLayout buttonBar = buildFooter();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setStyleName("dpuDetailMainLayout");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setHeight("100%");
        mainLayout.setWidth("100%");

        mainLayout.addComponent(generalDetail);
        mainLayout.setExpandRatio(generalDetail, 0.0f);

        mainLayout.addComponent(configHolder);
        mainLayout.setExpandRatio(configHolder, 1.0f);

        mainLayout.addComponent(buttonBar);
        mainLayout.setExpandRatio(buttonBar, 0.0f);

        setContent(mainLayout);
    }

    /**
     * Build the footer line with buttons.
     *
     * @return The main layout of this section.
     */
    public HorizontalLayout buildFooter() {
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setStyleName("dpuDetailButtonBar");
        buttonBar.setSpacing(true);
        buttonBar.setWidth("100%");

        btnSaveAndCommit = new Button(Messages.getString("DPUDetail.save"));
        btnSaveAndCommit.setWidth("90px");
        buttonBar.addComponent(btnSaveAndCommit);

        btnCancel = new Button(Messages.getString("DPUDetail.cancel"));
        btnCancel.setWidth("90px");
        buttonBar.addComponent(btnCancel);

        btnCopyFromTemplate = new Button(Messages.getString("DPUDetail.copy"));
        btnCopyFromTemplate.setWidth("160px");
        buttonBar.addComponent(btnCopyFromTemplate);
        buttonBar.setExpandRatio(btnCopyFromTemplate, 1.0f);
        buttonBar.setComponentAlignment(btnCopyFromTemplate, Alignment.MIDDLE_RIGHT);

        btnCopyFromTemplate.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                DPUInstanceRecord instance = dpuInstance.getDPUInstanceRecord();
                String oldConf = instance.getRawConf();
                // set template conf to reload gui
                instance.setRawConf(instance.getTemplate().getRawConf());
                reloadConfDialog();
                // change it back
                instance.setRawConf(oldConf);
            }
        });

        btnSaveAsNew = new Button(Messages.getString("DPUDetail.saveAs"));
        btnSaveAsNew.setWidth("160px");
        btnSaveAsNew.setVisible(permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_CREATE_FROM_INSTANCE));
        buttonBar.addComponent(btnSaveAsNew);
//        buttonBar.setExpandRatio(btnSaveAsNew, 1.0f);
        buttonBar.setComponentAlignment(btnSaveAsNew, Alignment.MIDDLE_RIGHT);

        btnSaveAndCommit.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (saveDPUInstance()) {
                    result = true;
                    close();
                }
            }
        });

        btnCancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                result = false;
                close();
            }
        });

        btnSaveAsNew.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(),
                        Messages.getString("DPUDetail.saveAs.dialog"),
                        new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog cd) {
                        if (cd.isConfirmed() && saveDpuAsNew()) {
                            result = true;
                            close();
                        }
                    }
                });
            }
        });

        return buttonBar;
    }

    /**
     * Show DPU detail.
     *
     * @param dpu
     * @param readOnly
     */
    public void showDpuDetail(DPUInstanceRecord dpu, boolean readOnly) {
        this.dpuInstance = new DPUInstanceWrap(dpu, dpuFacade, Locale.forLanguageTag(appConfig.getString(ConfigProperty.LOCALE)),
                appConfig, this.utils.getUser());
        this.setCaption(Messages.getString("DPUDetail.detail", dpu.getName().trim(),
                readOnly ? Messages.getString("DPUDetail.read-only.mode") : ""));

        generalDetail.loadFromDPU(dpu, false);

        btnSaveAndCommit.setEnabled(!readOnly);
        btnSaveAsNew.setEnabled(!readOnly);

        reloadConfDialog();
    }

    private void reloadConfDialog() {
        Component confDialog = null;
        try {
            confDialog = dpuInstance.getDialog();
            dpuInstance.configuredDialog();
        } catch (ModuleException e) {
            Notification.show(Messages.getString("DPUDetail.configuration.loadFail.dialog"), e
                    .getMessage(), Type.ERROR_MESSAGE);
            LOG.error("Failed to load dialog for {}", dpuInstance
                    .getDPUInstanceRecord().getId(), e);
        } catch (FileNotFoundException e) {
            Notification.show(Messages.getString("DPUDetail.missing.jar"), e.getMessage(),
                    Type.ERROR_MESSAGE);
        } catch (DPUWrapException e) {
            Notification.show(Messages.getString("DPUDetail.dpu.loadFail.dialog"), e.getMessage(),
                    Type.ERROR_MESSAGE);
        } catch (DPUConfigException e) {
            Notification.show(
                    Messages.getString("DPUDetail.configuration.problem"),
                    e.getMessage(), Type.WARNING_MESSAGE);
            LOG.error("Problem with configuration for {}", dpuInstance
                    .getDPUInstanceRecord().getId(), e);
        }

        // add to the component
        configHolder.setConfigComponent(confDialog);
    }

    /**
     * Saves configuration of DPURecord Instance which was set in detail dialog.
     *
     * @return True if save was successful, false otherwise.
     */
    protected boolean saveDPUInstance() {
        try {
            if (!validate()) {
                return false;
            }
            // validate can throw, so should be before any actual saving
            dpuInstance.saveConfig();
            generalDetail.saveToDPU(dpuInstance.getDPUInstanceRecord(), dpuInstance);
        } catch (SPARQLValidationException e) {
            Notification.show(Messages.getString("DPUDetail.query.validator"),
                    Messages.getString("DPUDetail.query.validator.description", e.getQueryNumber(), e.getMessage()),
                    Notification.Type.ERROR_MESSAGE);
            return false;
        } catch (DPUConfigException e) {
            LOG.error("saveDPUInstance", e);
            Notification.show(Messages.getString("DPUDetail.save.fail.dialog"), e
                    .getMessage(), Type.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            LOG.error("saveDPUInstance", e);

            Throwable rootCause = DecorationHelper.findFinalCause(e);
            String text = Messages.getString("DPUDetail.exception.message",
                    rootCause.getClass().getName(), rootCause.getMessage());
            Notification.show(
                    Messages.getString("DPUDetail.configuration.store.fail"),
                    text, Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Creates new DPU in tree with prefilled configuration taken from current
     * configuration of this DPU.
     *
     * @return True if save was successful, false otherwise.
     */
    protected boolean saveDpuAsNew() {
        if (saveDPUInstance()) {
            DPUTemplateRecord newDPU = dpuFacade.createTemplateFromInstance(
                    dpuInstance.getDPUInstanceRecord());
            dpuInstance.getDPUInstanceRecord().setTemplate(newDPU);
            dpuFacade.save(newDPU);
            return true;
        }
        return false;
    }

    private boolean validate() {
        return generalDetail.validate();
    }

    /**
     * True in case that the dialog save some changes.
     *
     * @return
     */
    public boolean getResult() {
        return result;
    }

}
