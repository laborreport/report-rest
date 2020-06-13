package ru.indraft.reportrest.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleService {

    private static final String BUNDLE_NAME = "bundle.locale";

    private static final LocaleService instance = new LocaleService();
    private ResourceBundle resourceBundle;

    private LocaleService() {
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, getDefaultLocale());
    }

    public static LocaleService getInstance() {
        return instance;
    }

    public static Locale getDefaultLocale() {
        return Locale.forLanguageTag("ru");
    }

    public String get(String key) {
        return resourceBundle.getString(key);
    }
}
