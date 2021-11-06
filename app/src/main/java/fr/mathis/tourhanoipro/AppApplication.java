package fr.mathis.tourhanoipro;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.core.tools.DataManager;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.view.game.GameView;

public class AppApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String currentThemeMode = PrefHelper.ReadString(this, PrefHelper.KEY_DARK_THEME, null);
        if(currentThemeMode == null)
            PrefHelper.SaveString(this, PrefHelper.KEY_DARK_THEME, "auto");

        Tools.updateThemeMode(this);

        // Initialise le nombre de disque
        int diskCount = PrefHelper.ReadInt(this, PrefHelper.KEY_DISK_COUNT, -1);
        if(diskCount == -1)
            PrefHelper.SaveInt(this, PrefHelper.KEY_DISK_COUNT, 5);

        // Initialise la partie
        ArrayList<String> savedGames = DataManager.GetAllSavedGames(this);
        if(savedGames.size() == 0) {
            savedGames.add(GameView.getNewSaveData(this, PrefHelper.ReadInt(this, PrefHelper.KEY_DISK_COUNT, -1)));
            DataManager.SaveAllGames(savedGames, this);
        }
    }
}
