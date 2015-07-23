/*******************************************************************************
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
 *******************************************************************************/
/*******************************************************************************
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
 *******************************************************************************/
package cz.cuni.mff.xrg.odcs.rdf.i18n;

import java.text.MessageFormat;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import eu.unifiedviews.commons.i18n.DataunitLocaleHolder;

/**
 * Class responsible for retrieving internationalized messages.
 * Use this class only for internationalization of dataunit-rdf module!
 * This is because it looks only into dataunit-rdf resource bundles located in ../dataunit-rdf/src/main/resources.
 * Locale used in retrieving messages comes from LocaleHolder @see {@link eu.unifiedviews.commons.i18n.DataunitLocaleHolder}
 *
 * @author mva
 */
public class Messages {

    public static final String BUNDLE_NAME = "rdf-messages";

    public static final ReloadableResourceBundleMessageSource MESSAGE_SOURCE = initializeResourceBundle();

    /**
     * Get the resource bundle string stored under key, formatted using {@link MessageFormat}.
     *
     * @param key  resource bundle key
     * @param args parameters to formatting routine
     * @return formatted string, returns "!key!" when the value is not found in bundle
     */
    public static String getString(final String key, final Object... args) {
        try {
            return MESSAGE_SOURCE.getMessage(key, args, DataunitLocaleHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Initialize resource bundle.
     *
     * @return ResourceBundle
     */
    private static ReloadableResourceBundleMessageSource initializeResourceBundle() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setBasename("classpath:" + BUNDLE_NAME);
        return ms;
    }
}
