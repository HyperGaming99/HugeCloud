package dev.arolg.cloud.utils;

import dev.arolg.cloud.HugeCloud;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static Locale currentLocale;
    private static ResourceBundle messages;

    public static void loadLanguage() {
        String languageCode = HugeCloud.getConfigManager().getConfig().getLanguage();
        setLanguage(languageCode);
    }

    public static void setLanguage(String languageCode) {
        switch (languageCode.toLowerCase()) {
            case "de" -> currentLocale = Locale.GERMAN;
            case "en" -> currentLocale = Locale.ENGLISH;
            default -> throw new IllegalArgumentException("Unsupported language: " + languageCode);
        }
        messages = ResourceBundle.getBundle("messages", currentLocale);

        // Save the selected language to the config
        HugeCloud.getConfigManager().getConfig().setLanguage(languageCode);
        HugeCloud.getConfigManager().saveConfig();
    }

    public static String getMessage(String key, Object... params) {
        String message = messages.getString(key);
        return MessageFormat.format(message, params);
    }
}