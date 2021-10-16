package fr.mathis.tourhanoipro.ui.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;

import dev.sasikanth.colorsheet.ColorSheet;
import fr.mathis.tourhanoipro.MainActivity;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Bind du clic sur la préférence de couleur
        Preference colorPref = findPreference(getString(R.string.pref_key_color));
        colorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final ColorSheet colorSheet = new ColorSheet();
                final int[] colors = getResources().getIntArray(R.array.themeColors);
                colorSheet.cornerRadius(8);

                final Function1 listener = (new Function1() {
                    public Object invoke(Object var1) {
                        this.invoke(((Number)var1).intValue());
                        return Unit.INSTANCE;
                    }

                    public final void invoke(int color) {

                        int index = Tools.findIntIndex(colors, color);
                        PrefHelper.SaveInt(getContext(), PrefHelper.KEY_THEME_INDEX, index);
                        getActivity().recreate();
                    }
                });

                int currentColor = colors[PrefHelper.ReadInt(getContext(), PrefHelper.KEY_THEME_INDEX, PrefHelper.DEFAULT_THEME_INDEX)];

                colorSheet.
                        colorPicker(colors, currentColor, false, listener)
                        .show(getParentFragmentManager());

                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.compareTo(PrefHelper.KEY_DARK_THEME) == 0) {
            Tools.updateThemeMode(this.getContext());
        }
        else if (key.compareTo(PrefHelper.KEY_DRAWER_LOCKED) == 0) {
            MainActivity activity = (MainActivity) getActivity();
            activity.updateDrawerLockMode();
        }
    }
}
