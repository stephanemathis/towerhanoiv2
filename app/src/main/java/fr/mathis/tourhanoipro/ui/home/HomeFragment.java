package fr.mathis.tourhanoipro.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.MainActivity;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.DataManager;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.view.game.GameView;
import fr.mathis.tourhanoipro.view.game.listener.QuickTouchListener;
import fr.mathis.tourhanoipro.view.game.listener.TurnListener;

public class HomeFragment extends Fragment implements TurnListener, QuickTouchListener {

    private GameView gvMain;

    static final int MENU_QUICK_TOUCH_ENABLE = 1;
    static final int MENU_QUICK_TOUCH_REMOVE = 5;

    private MenuItem menuItemSmallTouchEnable;
    private MenuItem menuItemSmallTouchModify;
    private MenuItem menuItemSmallTouchRemove;

    private int currentGameIndex = 0;
    ArrayList<String> allGames;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        gvMain = root.findViewById(R.id.gv_main);

        gvMain.setTurnListener(this);
        gvMain.setQuickTouchListener(this);

        return root;
    }

    @Override
    public void onResume() {

        allGames = DataManager.GetAllSavedGames(getActivity().getApplicationContext());

        if (allGames.size() == 0) {
            gvMain.createNewGame(PrefHelper.ReadInt(getContext(), PrefHelper.KEY_DISK_COUNT, 5));
            allGames.add(0, gvMain.saveGameAsString());
        } else {
            gvMain.launchGame(allGames.get(0));
        }


        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        allGames.set(currentGameIndex, gvMain.saveGameAsString());
        DataManager.SaveAllGames(allGames, getActivity().getApplicationContext());
    }

    //#region Menus

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();

        menuItemSmallTouchEnable = menu.add(0, MENU_QUICK_TOUCH_ENABLE, 0, R.string.quick_touch_enable);
        menuItemSmallTouchEnable.setIcon(R.drawable.ic_action_smalltouch);
        menuItemSmallTouchEnable.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menuItemSmallTouchRemove = menu.add(0, MENU_QUICK_TOUCH_REMOVE, 0, R.string.quick_touch_delete);
        menuItemSmallTouchRemove.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        menu.findItem(MENU_QUICK_TOUCH_REMOVE).setVisible((gvMain != null && gvMain.getQt() != null));
        menu.findItem(MENU_QUICK_TOUCH_ENABLE).setVisible((gvMain == null || gvMain.getQt() == null));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case MENU_QUICK_TOUCH_ENABLE:
                gvMain.activateQuickTouchMode();

                menuItemSmallTouchEnable.setVisible(false);

                //initHelpPopup(true);

                return true;
            case MENU_QUICK_TOUCH_REMOVE:
                gvMain.activateQuickTouchMode();

                menuItemSmallTouchRemove.setVisible(false);
                menuItemSmallTouchModify.setVisible(false);
                menuItemSmallTouchEnable.setVisible(true);
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    //#endregion

    //#region Listeners

    @Override
    public void turnPlayed(int nbCoup, int nbTotal) {
        ((MainActivity)getActivity()).updateMainTitle(nbCoup + " / " + nbTotal);
    }

    @Override
    public void gameFinished(int nbCoup, int nbTotal, long miliseconds) {
        Toast.makeText(getActivity(), "Terminé ! Bravo !", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void quickTouchConstructed() {

        menuItemSmallTouchRemove.setVisible(true);
        menuItemSmallTouchModify.setVisible(true);

        // le back du thème qui ne fonctionne pas et drawer lock
        // besoin de le setter ? pas en trop ?


        // Ajouter dans les settings, de quoi se déconnecter de google play ?
        // mode sombre dans les settings
        // choisir la couleur du thème dans les settings
        // ajouter le menu pour le quick touch
        // pour avertir du succès, ajouter un truc en fond (sans popup) avec la tour en transparent derrière et des feux d'artifices !


    }

    //#endregion
}