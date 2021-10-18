package fr.mathis.tourhanoipro.ui.home;

public class GameAction {

    private boolean restart;
    private Integer diskCount;

    public GameAction(boolean restart, Integer diskCount) {
        this.restart = restart;
        this.diskCount = diskCount;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public Integer getDiskCount() {
        return diskCount;
    }

    public void setDiskCount(Integer diskCount) {
        this.diskCount = diskCount;
    }
}
