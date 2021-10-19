package fr.mathis.tourhanoipro.core.tools;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.mathis.tourhanoipro.R;

public class Tools {

    public static int convertDpToPixel(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static final String generateMd5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            s = s + s;
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getViewDiagonal(View v) {
        int diagonal = 0;

        diagonal = (int) Math.sqrt(v.getHeight() * v.getHeight() + v.getWidth() * v.getWidth());

        return diagonal;
    }

    public static void updateThemeMode(Context context) {

        String themeMode = PrefHelper.ReadString(context, PrefHelper.KEY_DARK_THEME, "auto");

        if (themeMode.compareTo("auto") == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else if (themeMode.compareTo("always_on") == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (themeMode.compareTo("always_off") == 0)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void applyColoredTheme(Context context)
    {
        int index = PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX);

        int[] themes = new int[] {
                R.style.AppThemeColored0,
                R.style.AppThemeColored1,
                R.style.AppThemeColored2,
                R.style.AppThemeColored3,
                R.style.AppThemeColored4,
                R.style.AppThemeColored5,
                R.style.AppThemeColored6,
                R.style.AppThemeColored7,
                R.style.AppThemeColored8,
                R.style.AppThemeColored9
        };

        context.getTheme().applyStyle(themes[index], true);
    }

    public static void applyTranslucentColoredTheme(Context context)
    {
        int index = PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX);

        int[] themes = new int[] {
                R.style.AppThemeTranslucentColored0,
                R.style.AppThemeTranslucentColored1,
                R.style.AppThemeTranslucentColored2,
                R.style.AppThemeTranslucentColored3,
                R.style.AppThemeTranslucentColored4,
                R.style.AppThemeTranslucentColored5,
                R.style.AppThemeTranslucentColored6,
                R.style.AppThemeTranslucentColored7,
                R.style.AppThemeTranslucentColored8,
                R.style.AppThemeTranslucentColored9
        };

        context.getTheme().applyStyle(themes[index], true);
    }

    public static int findIntIndex(int[] _array, int _value) {
        int index = 0;
        for(; index < _array.length; index++) {
            if(_array[index] == _value)
                break;
        }
        return index;
    }
}
