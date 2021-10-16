package fr.mathis.tourhanoipro.view.game.listener;

import fr.mathis.tourhanoipro.view.game.model.ClassField;

public interface TurnListener {
	ClassField field = null;

	public void turnPlayed(int nbCoup, int nbTotal);

	public void gameFinished(int nbCoup, int nbTotal, long miliseconds);
}
