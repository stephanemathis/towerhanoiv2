package fr.mathis.tourhanoipro.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.core.tools.DataManager;

public class HomeViewModel extends ViewModel {

    private int currentGameIndex = 0;
    private ArrayList<String> allGames;

    private final MutableLiveData<GameAction> selectedItem = new MutableLiveData<GameAction>();

    public void sendEvent(GameAction item) {
        selectedItem.setValue(item);
    }
    public LiveData<GameAction> getEvent() {
        return selectedItem;
    }

    //#region Getter & Setter

    public int getCurrentGameIndex() {
        return currentGameIndex;
    }

    public void setCurrentGameIndex(int currentGameIndex) {
        this.currentGameIndex = currentGameIndex;
    }

    public ArrayList<String> getAllGames() {
        return allGames;
    }

    public void setAllGames(ArrayList<String> allGames) {
        this.allGames = allGames;
    }

    //#endregion

    //#region Méthodes



    //#endegion
}

