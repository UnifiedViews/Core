package cz.cuni.mff.xrg.odcs.frontend;

import com.vaadin.server.UICreateEvent;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringUIProvider;

public class UnifiedViewsUIProvider extends SpringUIProvider {

    @Override
    public String getTheme(UICreateEvent event) {
        AppConfig appConfig = (AppConfig) SpringApplicationContext.getApplicationContext().getBean("configuration");
        return appConfig.getString(ConfigProperty.FRONTEND_THEME);
    }
}
