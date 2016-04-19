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
