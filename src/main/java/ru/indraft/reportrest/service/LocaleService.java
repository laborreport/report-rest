package ru.indraft.reportrest.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleService {

    private static final String BUNDLE_NAME = "bundle.locale";

    private static final LocaleService instance = new LocaleService();

    public static LocaleService getInstance() {
        return instance;
    }

    private ResourceBundle resourceBundle;

    private LocaleService() {
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ru", "RU"));
    }

    public String get(String key) {
        return resourceBundle.getString(key);
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
