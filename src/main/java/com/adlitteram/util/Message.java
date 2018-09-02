
package com.adlitteram.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Message {
    //
    private static ResourceBundle resource;
    private static Locale locale;
    //
    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("com.adlitteram.resource.text", getLocale());
    }

    public static void setLocale(Locale loc) {
        locale = loc;
        resource = null;
    }

    public static Locale getLocale() {
        return (locale == null) ? Locale.getDefault() : locale;
    }

    public static String get(String key) {
        try {
            if (resource == null) resource = getResourceBundle();
            return resource.getString(key);
        }
        catch (MissingResourceException e) {
            return key;
        }
    }

    public static String get(String key, Object arg1) {
        return get(key, new Object[]{arg1});
    }

    public static String get(String key, Object arg1, Object arg2) {
        return get(key, new Object[]{arg1, arg2});
    }

    public static String get(String key, Object[] args) {
        if (args == null) return get(key);

        try {
            return MessageFormat.format(resource.getString(key), args);
        }
        catch (MissingResourceException e) {
            return key;
        }
    }
}
