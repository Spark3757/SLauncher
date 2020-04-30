package ru.spark.slauncher.util.i18n;

import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.Locales.SupportedLocale;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

public final class I18n {

    private I18n() {
    }

    private static SupportedLocale getCurrentLocale() {
        try {
            return ConfigHolder.config().getLocalization();
        } catch (IllegalStateException e) {
            // e is thrown by ConfigHolder.config(), indicating the config hasn't been loaded
            // fallback to use default locale
            return Locales.DEFAULT;
        }
    }

    public static ResourceBundle getResourceBundle() {
        return getCurrentLocale().getResourceBundle();
    }

    public static String i18n(String key, Object... formatArgs) {
        return String.format(i18n(key), formatArgs);
    }

    public static String i18n(String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            Logging.LOG.log(Level.SEVERE, "Cannot find key " + key + " in resource bundle", e);
            return key;
        }
    }

    public static boolean hasKey(String key) {
        try {
            getResourceBundle().getString(key);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
    }
}
