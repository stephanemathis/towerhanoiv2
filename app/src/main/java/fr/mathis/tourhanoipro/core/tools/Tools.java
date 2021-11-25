package fr.mathis.tourhanoipro.core.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    public static void applyColoredTheme(Context context) {
        int index = PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX);

        int[] themes = new int[]{
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

    public static int[][] getColorPalette(Context context) {
        int[][] colors = new int[][]{
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent0, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary0, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark0, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent1, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary1, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark1, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent2, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary2, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark2, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent3, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary3, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark3, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent4, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary4, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark4, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent5, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary5, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark5, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent6, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary6, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark6, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent7, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary7, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark7, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent8, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary8, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark8, null)},
                new int[]{ResourcesCompat.getColor(context.getResources(), R.color.colorAccent9, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary9, null), ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark9, null)},
        };

        return colors;
    }

    public static int[] getRandomColorPalette(Context context) {
        return getColorPalette(context)[new Random().nextInt(10)];
    }

    public static void applyTranslucentColoredTheme(Context context) {
        int index = PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX);

        int[] themes = new int[]{
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
        for (; index < _array.length; index++) {
            if (_array[index] == _value)
                break;
        }
        return index;
    }

    public static int DISK_COLOR_COUNT = 3;

    public static int[] getDiskColors(Context context, int index) {

        if (index == -1) {
            index = PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_DISK_INDEX, 0);
        }

        int[] colors;

        if (index == 0) {
            colors = context.getResources().getIntArray(R.array.diskColor_classic);
        } else if (index == 1) {
            colors = context.getResources().getIntArray(R.array.diskColor_new);
        } else {
            int[] t = getColorPalette(context)[PrefHelper.ReadInt(context, PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX)];
            int[] newColors = new int[]{t[0], t[2]};
            return newColors;
        }

        return colors;
    }
}
