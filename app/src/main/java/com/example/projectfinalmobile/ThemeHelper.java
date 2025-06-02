package com.example.projectfinalmobile;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_IS_DARK_PREFIX = "is_dark_";

    private static String getUserThemeKey(String userId) {
        return KEY_IS_DARK_PREFIX + userId;
    }

    public static boolean isDarkMode(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(getUserThemeKey(userId), false);
    }
    public static void setDarkMode(Context context, String userId, boolean isDark) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(getUserThemeKey(userId), isDark).apply();
    }
}
