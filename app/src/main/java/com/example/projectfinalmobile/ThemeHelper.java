package com.example.projectfinalmobile;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_IS_DARK = "is_dark";

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_DARK, false);
    }

    public static void setDarkMode(Context context, boolean isDark) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply();
    }
}