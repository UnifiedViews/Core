package cz.cuni.mff.xrg.odcs.commons.app.i18n;

import java.util.Locale;

/**
 * Holder for locale settings.
 * Uses en_US locale as default.
 *
 * @author mva
 */
public class LocaleHolder {

    private static Locale locale = Locale.forLanguageTag("en_US"); // default value

    /**
     * Set current locale.
     *
     * @param locale
     *            Locale
     */
    public static void setLocale(Locale locale) {
        LocaleHolder.locale = locale;
    }

    /**
     * Get current locale.
     *
     * @return Locale
     */
    public static Locale getLocale() {
        return LocaleHolder.locale;
    }
}
