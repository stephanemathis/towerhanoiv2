package fr.mathis.tourhanoipro.ui.home;

public class GameAction {

    private boolean restart;

    public GameAction(boolean restart) {
        this.restart = restart;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }
}
