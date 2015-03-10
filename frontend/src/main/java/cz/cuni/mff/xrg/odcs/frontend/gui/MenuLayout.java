package cz.cuni.mff.xrg.odcs.frontend.gui;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.RequestHolder;
import cz.cuni.mff.xrg.odcs.frontend.auth.AuthenticationService;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Initial;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Login;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PostLogoutCleaner;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Scheduler;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Settings;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.dpu.DPUPresenterImpl;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.executionlist.ExecutionListPresenterImpl;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.pipelinelist.PipelineListPresenterImpl;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigator;
import cz.cuni.mff.xrg.odcs.frontend.navigation.ClassNavigatorHolder;

/**
 * Class represent main application component. The component contains menu bar
 * and a place where to place application view.
 *
 * @author Petyr
 */
public class MenuLayout extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(MenuLayout.class);

    private ClassNavigator navigator;

    /**
     * Authentication context used to render menu with respect to currently
     * logged in user.
     */
    @Autowired
    private AuthenticationContext authCtx;

    /**
     * Authentication service handling logging in and out.
     */
    @Autowired
    private AuthenticationService authService;

    /**
     * Application's configuration.
     */
    @Autowired
    protected AppConfig appConfig;

    /**
     * Used layout.
     */
    private VerticalLayout mainLayout;

    /**
     * Menu bar.
     */
    private MenuBar menuBar;

    /**
     * Layout for application views.
     */
    private Panel viewLayout;

    private Label userName;

    private Button logOutButton;

    private Embedded backendStatus;

    @Value("${header.color0:#0095b7}")
    private String backgroundColor0;

    @Value("${header.color1:#0095b7}")
    private String backgroundColor1;

    @Value("${header.color2:#007089}")
    private String backgroundColor2;

    @Value("${header.color3:#007089}")
    private String backgroundColor3;

    private final HashMap<String, MenuItem> menuItems = new HashMap<>();

    /**
     * Build the layout.
     */
    public void build() {
        setSizeFull();

        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSizeFull();

        // menuBar
        menuBar = new MenuBar();
        menuBar.setSizeFull();
        menuBar.setHtmlContentAllowed(true);

        backendStatus = new Embedded();
        backendStatus.setWidth("16px");
        backendStatus.setHeight("16px");

        userName = new Label(authCtx.getUsername());
        userName.setIcon(new ThemeResource("img/user.svg"));
        userName.setWidth("150px");
        userName.addStyleName("username");

        logOutButton = new Button();
        logOutButton.setCaption(Messages.getString("MenuLayout.logout"));
        logOutButton.setVisible(authCtx.isAuthenticated());
        logOutButton.setStyleName(BaseTheme.BUTTON_LINK);
        logOutButton.addStyleName("logout");
        logOutButton.setIcon(new ThemeResource("img/logout.svg"), Messages.getString("MenuLayout.icon.logout"));
        logOutButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                authService.logout(RequestHolder.getRequest());
                authCtx.clear();
                refreshUserBar();
                String logout_url = null;
                try {
                    logout_url = appConfig.getString(ConfigProperty.LOGOUT_URL);
                } catch (MissingConfigPropertyException e) {
                    //property not found, do nothing
                    ;
                }
                if (logout_url != null)
                    getUI().getPage().setLocation(logout_url);
                else
                    navigator.navigateTo(Login.class);
                doAfterLogoutCleaning();
            }
        });

        final HorizontalLayout headerLine = new HorizontalLayout(menuBar, userName, logOutButton, backendStatus);
        headerLine.setSizeFull();

        headerLine.setComponentAlignment(menuBar, Alignment.MIDDLE_LEFT);
        headerLine.setComponentAlignment(userName, Alignment.MIDDLE_CENTER);
        headerLine.setComponentAlignment(logOutButton, Alignment.MIDDLE_CENTER);
        headerLine.setComponentAlignment(backendStatus, Alignment.MIDDLE_CENTER);
        headerLine.setExpandRatio(menuBar, 1.0f);

        // Custom layout for custom and dynamic background.
        final CssLayout headerLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c == headerLine) {
                    return buildBackgroundCss();
                }
                return super.getCss(c);
            }
        };
        headerLayout.setWidth("100%");
        headerLayout.setHeight("37px");
        headerLayout.addComponent(headerLine);

        mainLayout.addComponent(headerLayout);
        mainLayout.setExpandRatio(headerLayout, 0.0f);

        // viewLayout - in here the content is stored.
        viewLayout = new Panel();
        viewLayout.setSizeFull();
        viewLayout.setStyleName("viewLayout");

        mainLayout.addComponent(viewLayout);
        mainLayout.setExpandRatio(viewLayout, 1.0f);

        refreshBackendStatus(false);

        setCompositionRoot(mainLayout);
    }

    /**
     * @return Generated background css.
     */
    private String buildBackgroundCss() {
        final StringBuilder back = new StringBuilder();
        back.append(String.format("background: -moz-linear-gradient(top, %s 0%%, %s 48%%, %s 51%%, %s 100%%);\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("background: -webkit-gradient(linear, left top, left bottom, color-stop(0%%,%s), color-stop(48%%,%s), color-stop(51%%,%s), color-stop(100%%,%s));\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("background: -webkit-linear-gradient(top, %s 0%%,%s 48%%,%s 51%%,%s 100%%);\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("background: -o-linear-gradient(top, %s 0%%,%s 48%%,%s 51%%,%s 100%%);\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("background: -ms-linear-gradient(top, %s 0%%,%s 48%%,%s 51%%,%s 100%%);\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("background: linear-gradient(to bottom, %s 0%%,%s 48%%,%s 51%%,%s 100%%);\n", backgroundColor0, backgroundColor1, backgroundColor2, backgroundColor3));
        back.append(String.format("filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='%s', endColorstr='%s',GradientType=0 );", backgroundColor0, backgroundColor3));
        return back.toString();
    }

    /**
     * finds all classes that implement PostLogoutCleaner interface
     * and calls doAfterLogout method. These classes need to have
     * session scope
     */
    private void doAfterLogoutCleaning() {
        AppEntry appEntry = (AppEntry) getParent();
        Collection<PostLogoutCleaner> classesToDoCleaning =
                appEntry.getBeans(PostLogoutCleaner.class).values();
        for (PostLogoutCleaner presenterClass : classesToDoCleaning) {
            presenterClass.doAfterLogout();
        }
    }

    /**
     * Return layout for application views.
     *
     * @return layout for application views
     */
    public Panel getViewLayout() {
        return this.viewLayout;
    }

    /**
     * Refresh user bar.
     */
    public void refreshUserBar() {
        userName.setValue(authCtx.getUsername());
        logOutButton.setVisible(authCtx.isAuthenticated());
    }

    /**
     * Refreshes the status of backend. Green/red icon in header.
     *
     * @param isRunning
     */
    public void refreshBackendStatus(boolean isRunning) {
        backendStatus.setDescription(isRunning ? Messages.getString("MenuLayout.backend.online") : Messages.getString("MenuLayout.backend.offline"));
        backendStatus.setSource(new ThemeResource(isRunning ? "icons/online.svg" : "icons/offline.svg"));
    }

    /**
     * Setup navigation and menu.
     *
     * @param navigatorHolder
     */
    public void setNavigation(ClassNavigatorHolder navigatorHolder) {
        this.navigator = navigatorHolder;
        // Use installation name as a name for home button.
        String instalName = Messages.getString("MenuLayout.home");
        try {
            instalName = appConfig.getString(ConfigProperty.INSTALLATION_NAME);
        } catch (MissingConfigPropertyException ex) {
            // using default value ""
            LOG.error("Failed to load frontend property: " + ConfigProperty.INSTALLATION_NAME, ex.getMessage());
        }
        // Add items.
        menuItems.put("", menuBar.addItem(instalName, new NavigateToCommand(Initial.class, navigator)));
        menuItems.put("PipelineList", menuBar.addItem(Messages.getString("MenuLayout.pipelines"), new NavigateToCommand(PipelineListPresenterImpl.class, navigator)));
        menuItems.put("DPURecord", menuBar.addItem(Messages.getString("MenuLayout.dpuTemplates"), new NavigateToCommand(DPUPresenterImpl.class, navigator)));
        menuItems.put("ExecutionList", menuBar.addItem(Messages.getString("MenuLayout.executionMonitor"), new NavigateToCommand(ExecutionListPresenterImpl.class, navigator)));
        menuItems.put("Scheduler", menuBar.addItem(Messages.getString("MenuLayout.scheduler"), new NavigateToCommand(Scheduler.class, navigator)));
        menuItems.put("Administrator", menuBar.addItem(Messages.getString("MenuLayout.settings"), new NavigateToCommand(Settings.class, navigator)));
    }

    /**
     * Sets active menu item.
     *
     * @param viewName
     *            Item to set as active.
     */
    public void setActiveMenuItem(String viewName) {
        for (MenuItem item : menuBar.getItems()) {
            item.setCheckable(true);
            item.setChecked(false);
        }
        MenuItem activeMenu = menuItems.get(viewName);
        if (activeMenu != null) {
            activeMenu.setChecked(true);
        }
    }

    /**
     * Class use as command to change sub-pages.
     *
     * @author Petyr
     */
    private class NavigateToCommand implements Command {

        private final Class<?> clazz;

        private final ClassNavigator navigator;

        public NavigateToCommand(Class<?> clazz, ClassNavigator navigator) {
            this.clazz = clazz;
            this.navigator = navigator;
        }

        @Override
        public void menuSelected(MenuItem selectedItem) {
            navigator.navigateTo(this.clazz);
        }
    }

}
