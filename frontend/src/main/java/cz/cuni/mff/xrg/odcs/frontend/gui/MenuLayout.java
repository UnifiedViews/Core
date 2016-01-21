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
package cz.cuni.mff.xrg.odcs.frontend.gui;

import java.util.Collection;
import java.util.HashMap;

import com.vaadin.server.ExternalResource;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.pipelinecanvas.SessionRefresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.BaseTheme;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
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

    @Autowired
    private PermissionUtils permissionUtils;

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

        final String userDisplayName = getDisplayUserName();
        userName = new Label(userDisplayName);
        userName.setIcon(new ThemeResource("img/user.svg"));
        userName.setWidth("150px");
        userName.addStyleName("username");

        logOutButton = new Button();
        logOutButton.setCaption(Messages.getString("MenuLayout.logout"));
        logOutButton.setHtmlContentAllowed(true);
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

        if(appConfig.contains(ConfigProperty.RENEW_IFRAME_ENABLED) && appConfig.getBoolean(ConfigProperty.RENEW_IFRAME_ENABLED)) {
            final SessionRefresh sessionRefresh = new SessionRefresh();
            final BrowserFrame renewFrame = new BrowserFrame("",new ExternalResource(appConfig.getString(ConfigProperty.RENEW_IFRAME_SRC)));
            renewFrame.setId("renewFrame");
            renewFrame.setVisible(true);
            renewFrame.setWidth(0, Unit.PIXELS);
            renewFrame.setHeight(0, Unit.PIXELS);
            headerLine.addComponents(sessionRefresh, renewFrame);
        }

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
        userName.setValue(getDisplayUserName());
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

    public void refreshMenuButtons() {
        if (!this.permissionUtils.hasUserAuthority(EntityPermissions.DPU_TEMPLATE_SHOW_SCREEN)) {
            if (this.menuItems.containsKey("DPURecord")) {
                this.menuBar.removeItem(this.menuItems.remove("DPURecord"));
            }
        } else {
            if (!this.menuItems.containsKey("DPURecord")) {
                this.menuItems.put("DPURecord", this.menuBar.addItem(Messages.getString("MenuLayout.dpuTemplates"),
                        new NavigateToCommand(DPUPresenterImpl.class, this.navigator)));
            }
        }
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

        try {
            final String externalLinkName = this.appConfig.getString(ConfigProperty.EXTERNAL_MENU_LINK_NAME);
            final String externalLinkURL = this.appConfig.getString(ConfigProperty.EXTERNAL_MENU_LINK_URL);

            MenuItem item = this.menuBar.addItem(externalLinkName, new Command() {

                @Override
                public void menuSelected(MenuItem selectedItem) {
                    Page.getCurrent().open(externalLinkURL, null);

                }
            });
            item.setIcon(new ThemeResource("icons/external_link.png"));
            this.menuItems.put("External", item);

        } catch (MissingConfigPropertyException e) {
            // ignore, optional configuration
        }
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

    private String getDisplayUserName() {

        if (this.authCtx.getUser() != null) {
            String userName = (this.authCtx.getUser().getFullName() != null && !this.authCtx.getUser().getFullName().equals(""))
                    ? this.authCtx.getUser().getFullName() : this.authCtx.getUsername();
            if (this.authCtx.getUser().getUserActor() != null) {
                return userName + " (" + this.authCtx.getUser().getUserActor().getName() + ")";
            }
            return userName;
        }

        return this.authCtx.getUsername();
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
