package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Superclass for all learners (basic and student).
 */
public abstract class RLPacMan extends Controller<MOVE> {	

// I'm guessing test mode true would turn off exploration, etc... 	
	public abstract void startEpisode(Game game, boolean testMode);
	public abstract void processStep(Game game);
	public abstract void savePolicy(String filename);

	public double[] episodeData() { // Override to add data to learning curves
		double[] data = new double[0];
		return data;
	}
	
	public abstract QFunction getQFunction();
}
