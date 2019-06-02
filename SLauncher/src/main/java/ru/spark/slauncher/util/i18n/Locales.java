package ru.spark.slauncher.util.i18n;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.spark.slauncher.util.Lang;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Locales {

    public static final SupportedLocale DEFAULT = new SupportedLocale(Locale.getDefault(), "lang.default");

    /**
     * English
     */
    public static final SupportedLocale RU = new SupportedLocale(Locale.forLanguageTag("ru"));

    /**
     * Russian
     */
    public static final SupportedLocale EN = new SupportedLocale(Locale.ENGLISH);

    public static final List<SupportedLocale> LOCALES = Lang.immutableListOf(DEFAULT, RU, EN);


    public static SupportedLocale getLocaleByName(String name) {
        if (name == null) return DEFAULT;
        switch (name.toLowerCase()) {
            case "en":
                return EN;
            case "ru":
                return RU;
            default:
                return DEFAULT;
        }
    }

    public static String getNameByLocale(SupportedLocale locale) {
        if (locale == EN) return "en";
        else if (locale == RU) return "ru";
        else if (locale == DEFAULT) return "def";
        else throw new IllegalArgumentException("Unknown locale: " + locale);
    }

    @JsonAdapter(SupportedLocale.TypeAdapter.class)
    public static class SupportedLocale {
        private final Locale locale;
        private final String name;
        private final ResourceBundle resourceBundle;

        SupportedLocale(Locale locale) {
            this(locale, null);
        }

        SupportedLocale(Locale locale, String name) {
            this.locale = locale;
            this.name = name;
            resourceBundle = ResourceBundle.getBundle("assets.lang.I18N", locale, UTF8Control.INSTANCE);
        }

        public Locale getLocale() {
            return locale;
        }

        public ResourceBundle getResourceBundle() {
            return resourceBundle;
        }

        public String getName(ResourceBundle newResourceBundle) {
            if (name == null) return resourceBundle.getString("lang");
            else return newResourceBundle.getString(name);
        }

        public static class TypeAdapter extends com.google.gson.TypeAdapter<SupportedLocale> {
            @Override
            public void write(JsonWriter out, SupportedLocale value) throws IOException {
                out.value(getNameByLocale(value));
            }

            @Override
            public SupportedLocale read(JsonReader in) throws IOException {
                return getLocaleByName(in.nextString());
            }
        }
    }
}