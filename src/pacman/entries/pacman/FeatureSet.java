package pacman.entries.pacman;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Superclass for feature sets.
 */
public abstract class FeatureSet {

	public abstract int size();
	public abstract double get(int i);
	public abstract FeatureSet extract(Game game, MOVE move);

}
