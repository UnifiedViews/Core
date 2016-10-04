package cz.cuni.mff.xrg.odcs.frontend.dpu.wrap;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.UI;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.dpu.dialog.ConfigDialogContextImpl;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.vaadin.AbstractConfigDialog;
import eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext;
import eu.unifiedviews.dpu.config.vaadin.ConfigDialogProvider;
import eu.unifiedviews.dpu.config.vaadin.InitializableConfigDialog;

/**
 * Class wrap {@line DPURecord} and provide functions that enable easy work with
 * configuration and configuration dialog.
 *
 * @author Petyr
 */
public class DPURecordWrap {

    private static final Logger LOG = LoggerFactory.getLogger(DPURecordWrap.class);

    /**
     * Wrapped DPU.
     */
    private DPURecord dpuRecord = null;

    /**
     * DPU's configuration dialog.
     */
    private AbstractConfigDialog<?> configDialog = null;

    /**
     * True if represents the template.
     */
    private final boolean isTemplate;

    private Locale locale;

    private AppConfig appConfig;

    private User loggedUser;

    /**
     * True if the {@link #configuredDialog()} has been called.
     */
    private boolean dialogConfigured = false;

    protected DPURecordWrap(DPURecord dpuRecord, boolean isTemplate, Locale locale, AppConfig appConfig, User user) {
        this.dpuRecord = dpuRecord;
        this.isTemplate = isTemplate;
        this.locale = locale;
        this.appConfig = appConfig;
        this.loggedUser = user;
    }

    /**
     * Try to save configuration from {@link #configDialog} into {@link #dpuRecord}. If the {@link #configDialog} is null nothing happen.
     * This function does not save data into database.
     *
     * @throws DPUConfigException
     * @throws cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException
     */
    public void saveConfig() throws DPUConfigException, DPUWrapException {
        if (configDialog == null) {
            return;
        }
        try {
            final String config = configDialog.getConfig();
            dpuRecord.setRawConf(config);
        } catch (DPUConfigException e) {
            throw e;
        } catch (Throwable e) {
            throw new DPUWrapException(Messages.getString("DPURecordWrap.save"), e);
        }
    }

    /**
     * Return configuration dialog for wrapped DPU. The configuration is not
     * set. To set dialog configuration call {@link #configuredDialog}
     *
     * @return configuration dialog for wrapped DPU
     * @throws ModuleException
     * @throws FileNotFoundException
     */
    public AbstractConfigDialog<?> getDialog()
            throws ModuleException, FileNotFoundException, DPUWrapException {
        // load configuration dialog
        try {
            loadConfigDialog();
        } catch (ModuleException | FileNotFoundException e) {
            throw e;
        } catch (Throwable e) {
            throw new DPUWrapException(Messages.getString("DPURecordWrap.load"), e);
        }
        return configDialog;
    }

    /**
     * If respective configuration dialog for wrapped DPU exist, then set it's
     * configuration. Otherwise do nothing.
     *
     * @throws DPUConfigException
     * @throws cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException
     */
    public void configuredDialog()
            throws DPUConfigException, DPUWrapException {
        // set dialog configuration
        try {
            loadConfigIntoDialog();
        } catch (DPUConfigException e) {
            throw e;
        } catch (Throwable e) {
            throw new DPUWrapException(Messages.getString("DPURecordWrap.configure"), e);
        }
    }

    /**
     * Check if the configuration in configuration dialog has been changed.
     * The configuration is assumed to be changed if it satisfy all the
     * following conditions:
     * <ul>
     * <li>DPU has configuration dialog.</li>
     * <li>The dialog has been obtained by calling {@link #getDialog().</li> <li><li><li> The dialog has been configurated by calling {@link #configuredDialog()}</li>
     * </ul>
     *
     * @return True if the configuration changed.
     * @throws cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPUWrapException
     */
    public boolean hasConfigChanged() throws DPUWrapException {
        if (configDialog == null || !dialogConfigured) {
            return false;
        }
        // ok we satisfy necesary conditions, we may ask the dialog
        // for changes
        try {
            final boolean isChanged = configDialog.hasConfigChanged();
            return isChanged;
        } catch (Exception ex) {
            throw new DPUWrapException(Messages.getString("DPURecordWrap.exception"), ex);
        }
    }

    /**
     * Return description from the dialog.
     *
     * @return Null in case of no dialog.
     */
    public String getDescription() {
        if (configDialog == null) {
            return null;
        }
        return configDialog.getDescription();
    }

    /**
     * Load the configuration dialog for {@link #dpuRecord} and store it into {@link #configDialog}. If the dialog is already loaded
     * ({@link #configDialog} is not null) then nothing is done. If the {@link #dpuRecord} does not provide configuration dialog set {@link #configDialog} to
     * null.
     * Can possibly emit runtime exception.
     *
     * @throws ModuleException
     * @throws FileNotFoundException
     * @throws DPUWrapException
     */
    @SuppressWarnings("unchecked")
    private void loadConfigDialog() throws ModuleException, FileNotFoundException, DPUWrapException {
        if (configDialog == null) {
            // continue and load the dialog
        } else {
            // already loaded ..
            return;
        }
        // first we need load instance of the DPU
        dpuRecord.loadInstance(((AppEntry) UI.getCurrent()).getBean(ModuleFacade.class));
        Object instance = dpuRecord.getInstance();
        // now try to load the dialog
        if (instance instanceof ConfigDialogProvider<?>) {
            ConfigDialogProvider<?> dialogProvider;
            // 'unchecked casting' .. we check type in condition above
            dialogProvider = (ConfigDialogProvider<?>) instance;

            try {
                java.lang.reflect.Method method = dialogProvider.getClass().getMethod("getConfigurationDialog");
                final Object result = method.invoke(dialogProvider);
                configDialog = (AbstractConfigDialog<?>) result;
            } catch (NoSuchMethodException | SecurityException ex) {
                LOG.error("Can't get method.", ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.error("Can't call method.", ex);
            }
            // get configuration dialog
            //configDialog = (AbstractConfigDialog<?>)dialogProvider.getConfigurationDialog();
            if (configDialog != null) {
                // setup the dialog
                final ConfigDialogContext context = new ConfigDialogContextImpl(isTemplate, locale, appConfig, loggedUser);
                configDialog.setContext(context);
                if (configDialog instanceof InitializableConfigDialog) {
                    try {
                        ((InitializableConfigDialog) configDialog).initialize();
                    } catch (DPUConfigException ex) {
                        throw new DPUWrapException("Can't initialize dialog.");
                    }
                }
            }
        } else {
            // no configuration dialog
            configDialog = null;
        }

    }

    /**
     * Try to load configuration from {@link #dpuRecord} into {@link #configDialog}. Can possibly emit runtime exception.
     *
     * @throws DPUConfigException
     */
    private void loadConfigIntoDialog() throws DPUConfigException {
        if (configDialog == null) {
            // no dialog .. nothing to do
            return;
        }
        // we try to configure the dialog
        dialogConfigured = true;
        configDialog.setConfig(dpuRecord.getRawConf());
    }

}
