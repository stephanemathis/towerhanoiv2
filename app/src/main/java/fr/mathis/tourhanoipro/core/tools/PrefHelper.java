package fr.mathis.tourhanoipro.core.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefHelper {

    public static String KEY_DISK_COUNT = "key_disk_count";
    public static String KEY_DRAWER_LOCKED = "pref_key_drawer_touch_enabled";
    public static String KEY_DARK_THEME = "pref_key_dark_theme";
    public static String KEY_THEME_INDEX = "pref_key_theme_index";
    public static int DEFAULT_THEME_INDEX = 3;

    public static int ReadInt(Context context, String key, int defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(mgr.getString(key, defaultValue + ""));
    }

    public static void SaveInt(Context context, String key, int value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putString(key, value + "");
        editor.commit();
    }

    public static boolean ReadBool(Context context, String key, boolean defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.getBoolean(key, defaultValue);
    }

    public static void SaveBool(Context context, String key, boolean value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public static String ReadString(Context context, String key, String defaultValue) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.getString(key, defaultValue);
    }
}
