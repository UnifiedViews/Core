package cz.cuni.mff.xrg.odcs.frontend;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.frontend.auth.AuthenticationService;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.RefreshManager;
import cz.cuni.mff.xrg.odcs.frontend.gui.MenuLayout;
import cz.cuni.mff.xrg.odcs.frontend.gui.ModifiableComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Initial;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Login;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.monitor.BackendHeartbeat;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigator;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigatorHolder;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;
import virtuoso.jdbc4.VirtuosoException;

import java.util.Map;

/**
 * Frontend application entry point. Also provide access to the application
 * services like database connection. To access the class use
 * ((AppEntry)UI.getCurrent()).
 * 
 * @author Petyr
 */
public class AppEntry extends com.vaadin.ui.UI {

    private static final Logger LOG = LoggerFactory.getLogger(AppEntry.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MenuLayout main;

    @Autowired
    private ClassNavigatorHolder navigatorHolder;

    private RefreshManager refreshManager;

    private String storedNavigation = null;

    private String lastView = null;

    private String actualView = null;

    @Autowired
    private AuthenticationContext authCtx;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private BackendHeartbeat heartbeatService;

    @Override
    protected void init(com.vaadin.server.VaadinRequest request) {
        this.setLocale(LocaleHolder.getLocale());
        // create main application uber-view and set it as app. content
        // in panel, for possible vertical scrolling
        main.build();
        setContent(main);

        // create a navigator to control the views
        // and set it into the navigator holder
        ClassNavigatorImpl navInstance = new ClassNavigatorImpl(this, main.getViewLayout(), context);
        ((ClassNavigatorHolder) navigatorHolder).setNavigator(navInstance);
        main.setNavigation(navigatorHolder);

        ConfirmDialog.Factory df = new DefaultConfirmDialogFactory() {
            // We change the default order of the buttons
            @Override
            public ConfirmDialog create(String caption, String message,
                    String okCaption, String cancelCaption, String notOkCaption) {
                ConfirmDialog d = super.create(caption, message,
                        okCaption,
                        cancelCaption, notOkCaption);
                d.setCloseShortcut(KeyCode.ESCAPE);
                LOG.debug("Dialog info: w:{} {} h:{} {} cap:{}", d.getWidth(),
                        d.getWidthUnits(), d.getHeight(), d.getHeightUnits(),
                        caption != null ? caption.length() : 0);

                // Change the order of buttons
                d.setContentMode(ConfirmDialog.ContentMode.TEXT);

                return d;
            }
        };
        ConfirmDialog.setFactory(df);

        ErrorHandler errorHandler = new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = DecorationHelper.findFinalCause(event.getThrowable());
                if (cause != null) {
                    if (cause.getClass() == VirtuosoException.class && ((VirtuosoException) cause).getErrorCode() == VirtuosoException.IOERROR && cause.getMessage().contains("Connection refused")) {
                        Notification.show(Messages.getString("AppEntry.database.error"), Messages.getString("AppEntry.database.error.description"), Type.ERROR_MESSAGE);
                        return;
                    }

                    if (cause instanceof AccessDeniedException) {
                        Notification.show(Messages.getString("AppEntry.permission.denied"), Messages.getString("AppEntry.permission.denied.description"), Type.ERROR_MESSAGE);
                        LOG.error("Permission denied", cause);
                    } else {
                        // Display the error message in a custom fashion
                        //String text = String.format("Exception: %s, Source: %s", cause.getClass().getName(), cause.getStackTrace().length > 0 ? cause.getStackTrace()[0].toString() : "unknown");
                        //Notification.show(cause.getMessage(), text, Type.ERROR_MESSAGE);
                        Notification.show(Messages.getString("AppEntry.unexpected.error"), Messages.getString("AppEntry.unexpected.error.description"), Type.ERROR_MESSAGE);
                        // and log ...
                        LOG.error("Uncaught exception", cause);
                    }

                } else {
                    // Do the default error handling (optional)
                    doDefault(event);
                }
            }
        };
        // Configure the error handler for the UI
        VaadinSession.getCurrent().setErrorHandler(errorHandler);
        this.setErrorHandler(errorHandler);

        /**
         * Checking user every time request is made.
         */
        navInstance.addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(ViewChangeListener.ViewChangeEvent event) {
                main.refreshUserBar();

                // TODO adjust this once Login screen will be presenters 
                //	to event.getNewView().equals(Login.class)
                if (!(event.getNewView() instanceof Login)
                        && !authCtx.isAuthenticated()
                        && !authService.tryRememberMeLogin(RequestHolder.getRequest())) {

                    storedNavigation = event.getViewName();
                    String parameters = event.getParameters();
                    if (parameters != null) {
                        storedNavigation += "/" + parameters;
                    }
                    navigatorHolder.navigateTo(Login.class);
                    getMain().refreshUserBar();
                    return false;
                }
                if (authCtx.isAuthenticated()) {
                    main.refreshMenuButtons();
                }
                setNavigationHistory(event);

                refreshManager.removeListener(RefreshManager.EXECUTION_MONITOR);
                refreshManager.removeListener(RefreshManager.DEBUGGINGVIEW);
                refreshManager.removeListener(RefreshManager.PIPELINE_LIST);
                refreshManager.removeListener(RefreshManager.SCHEDULER);
                refreshManager.removeListener(RefreshManager.PIPELINE_EDIT);

                return true;
            }

            @Override
            public void afterViewChange(ViewChangeListener.ViewChangeEvent event) {
            }
        });

        // attach a listener so that we'll get asked isViewChangeAllowed?
        navInstance.addViewChangeListener(new ViewChangeListener() {
            private String pendingViewAndParameters;

            private ModifiableComponent lastView;

            boolean forceViewChange = false;

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                if (forceViewChange) {
                    forceViewChange = false;
                    pendingViewAndParameters = null;
                    return true;
                }

                if (event.getOldView() instanceof ModifiableComponent
                        && ((ModifiableComponent) event.getOldView()).isModified()) {

                    // save the View where the user intended to go
                    lastView = (ModifiableComponent) event.getOldView();
                    pendingViewAndParameters = event.getViewName();
                    if (event.getParameters() != null) {
                        pendingViewAndParameters += "/";
                        pendingViewAndParameters += event
                                .getParameters();
                    }
                    // Prompt the user to save or cancel if the name is changed
                    ConfirmDialog cd = ConfirmDialog.getFactory().create(
                            Messages.getString("AppEntry.confirmDialog.name"),
                            Messages.getString("AppEntry.confirmDialog.text"),
                            Messages.getString("AppEntry.confirmDialog.save"),
                            Messages.getString("AppEntry.confirmDialog.discard"),
                            Messages.getString("AppEntry.confirmDialog.cancel"));
                    cd.show(getUI(),
                            new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {
                                        if (lastView.saveChanges()) {
                                            forceViewChange = true;
                                            navigatorHolder.navigateTo(pendingViewAndParameters);
                                        }
                                    } else if (cd.isCanceled()) {
                                        forceViewChange = true;
                                        navigatorHolder.navigateTo(pendingViewAndParameters);
                                    }
                                }
                            }, true);
                    main.setActiveMenuItem(null);
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
                pendingViewAndParameters = null;
                main.setActiveMenuItem(event.getViewName());
            }
        });

        Refresher refresher = new Refresher();
        refresher.setRefreshInterval(RefreshManager.REFRESH_INTERVAL);
        addExtension(refresher);
        refreshManager = new RefreshManager(refresher);
        refreshManager.addListener(RefreshManager.BACKEND_STATUS,
                new Refresher.RefreshListener() {
                    private boolean lastBackendStatus = false;

                    @Override
                    public void refresh(Refresher source) {
                        boolean isRunning = heartbeatService.checkIsAlive();
                        if (lastBackendStatus != isRunning) {
                            lastBackendStatus = isRunning;
                            main.refreshBackendStatus(lastBackendStatus);
                        }
                    }
                });
    }

    /**
     * Return to page which user tried to accessed before redirecting to login
     * page.
     */
    public void navigateAfterLogin() {
        if (storedNavigation == null) {
            navigatorHolder.navigateTo(Initial.class);
        } else {
            String navigationTarget = storedNavigation;
            storedNavigation = null;
            navigatorHolder.navigateTo(navigationTarget);
        }
    }

    private void setNavigationHistory(ViewChangeListener.ViewChangeEvent event) {
        lastView = actualView;
        actualView = event.getViewName();
        if (event.getParameters() != null) {
            actualView += "/" + event.getParameters();
        }
    }

    /**
     * Navigate to previous view.
     */
    public void navigateToLastView() {
        if (lastView != null) {
            navigatorHolder.navigateTo(lastView);
        } else {
            navigatorHolder.navigateTo("");
        }
    }

    /**
     * Get current navigation.
     * 
     * @return Navigator.
     */
    public ClassNavigator getNavigation() {
        return navigatorHolder;
    }

    /**
     * Fetches spring bean. For cases when auto-wiring is not a possibility.
     * 
     * @param <T>
     * @param type
     *            Class of the bean to fetch.
     * @return bean
     */
    public <T extends Object> T getBean(Class<T> type) {
        return context.getBean(type);
    }

    /**
     * Fetches spring beans. For cases when auto-wiring is not a possibility.
     * 
     * @param <T>
     * @param type
     *            Class of the bean to fetch.
     * @return bean
     */
    public <T extends Object> Map<String, T> getBeans(Class<T> type) {
        return context.getBeansOfType(type);
    }

    /**
     * Get main layout.
     * 
     * @return Main layout.
     */
    public MenuLayout getMain() {
        return main;
    }

    /**
     * Get refresh manager.
     * 
     * @return Refresh manager.
     */
    public RefreshManager getRefreshManager() {
        return refreshManager;
    }

    /**
     * Set URI fragment.
     * 
     * @param uriFragment
     *            New URI fragment.
     * @param throwEvents
     *            True to fire event.
     */
    public void setUriFragment(String uriFragment, boolean throwEvents) {
        Page.getCurrent().setUriFragment(uriFragment, throwEvents);
        if (uriFragment.length() > 0) {
            actualView = uriFragment.substring(1);
        }
    }

}
