package fr.mathis.tourhanoipro;

import android.app.Application;

import fr.mathis.tourhanoipro.core.tools.Tools;

public class AppApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Tools.updateThemeMode(this);
    }
}
