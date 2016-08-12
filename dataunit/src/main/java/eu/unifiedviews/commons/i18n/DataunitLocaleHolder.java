package eu.unifiedviews.commons.i18n;

import java.util.Locale;

/**
 * Holder for locale settings.
 * Uses en_US locale as default.
 *
 * @author mva
 */
public class DataunitLocaleHolder {

    private static Locale locale = Locale.forLanguageTag("en_US"); // default value

    /**
     * Set current locale.
     *
     * @param locale Locale
     */
    public static void setLocale(Locale locale) {
        DataunitLocaleHolder.locale = locale;
    }

    /**
     * Get current locale.
     *
     * @return Locale
     */
    public static Locale getLocale() {
        return DataunitLocaleHolder.locale;
    }
}
