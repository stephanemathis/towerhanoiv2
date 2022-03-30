package fr.mathis.tourhanoipro.core.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import android.util.Base64;

import fr.mathis.tourhanoipro.view.game.model.QuickTouch;

public class PrefHelper {

    public static String KEY_DISK_COUNT = "key_disk_count";
    public static String KEY_DRAWER_LOCKED = "pref_key_drawer_touch_enabled";
    public static String KEY_DARK_THEME = "pref_key_dark_theme";
    public static String KEY_THEME_INDEX = "pref_key_theme_index";
    public static String KEY_MOUVEMENT = "pref_key_mouvement_mode";
    public static String KEY_THEME_DISK_INDEX = "pref_key_theme_disk_index";
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

    public static void SaveString(Context context, String key, String value) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void SaveQtPosition(Context context, int orientation, QuickTouch qt) {
        String s = toString(qt);
        if(s != null)
            SaveString(context, "qt"+orientation, s);
    }

    public static void ClearQtPosition(Context context, int orientation) {
        SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mgr.edit();
        editor.remove("qt"+orientation);
        editor.commit();

    }

    public static QuickTouch GetQtPosition(Context context, int orientation) {

        String savedQtSting = ReadString(context, "qt"+orientation, null);
        QuickTouch qt = null;

        if(savedQtSting != null)
            qt = (QuickTouch) fromString(savedQtSting);

        return qt;
    }


    private static Object fromString(String s) {
        try {
            byte b[] = Base64.decode(s, 0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = null;

            si = new ObjectInputStream(bi);


            return si.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static String toString(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream( baos );
            oos.writeObject( o );
            oos.close();
            return Base64.encodeToString(baos.toByteArray(), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
