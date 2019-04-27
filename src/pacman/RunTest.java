package pacman;

import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.game.Constants;
import java.util.EnumMap;
import java.util.Random;
import pacman.entries.ghosts.*;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class RunTest {
	//public static String STUDENT = "depthS"; // Student feature set and algorithm

	public static Random rng = new Random();
	
	// Ghost "policies"
	public static StandardGhosts ghostsS = new StandardGhosts();
    public static RandomGhosts ghostsR = new RandomGhosts();
    public static ChaserGhosts ghostsC = new ChaserGhosts();
    public static LineGhosts ghostsL = new LineGhosts();

    
    // Task parameters
    
    public static Constants defaultConstants;
    public static HumanController human = new HumanController(new KeyBoardInput());
	/**
	 * Run experiments.
	 */
	public static void main(String[] args) {
        human();
	}

	/** Human demo **/
    public static void human(){
	    HumanController human = new HumanController(new KeyBoardInput());
        defaultConstants = new Constants();
        defaultConstants.MAZE_NUM = 2; //which maze;
        defaultConstants.GHOST_TYPE = 1; //which of three types of ghosts 0 = RandomGhosts, 1 = StandardGhosts, 2 = ChaserGhosts
        // Controls how fast ghosts move when pacman has eaten power pill. Lower numbers = slower (1 = frozen).
        defaultConstants.GHOST_SPEED_REDUCTION = 2; //ghost speed
        defaultConstants.NUM_GHOSTS = 0; //how many ghosts

        //increase delay for watching
        defaultConstants.DELAY = defaultConstants.DELAY*10;


        Game game=new Game(rng.nextLong(), defaultConstants);
        GameView gv=new GameView(game).showGame();

        gv.getFrame().addKeyListener(human.getKeyboardInput());

        while(!game.gameOver()) {
            game.advanceGame(human.getMove(game, -1), getGhostMove(game, game.constants.GHOST_TYPE));

            try {
                Thread.sleep(defaultConstants.DELAY);
            } catch (Exception e) {
            }
            gv.repaint();
        }
    }


	private static EnumMap<GHOST, MOVE> getGhostMove(Game game, int ghostType){

		switch(ghostType){
			case 0:
				return ghostsR.getMove(game.copy(), -1);
			case 1:
				return ghostsS.getMove(game.copy(), -1);
			case 2:
				return ghostsC.getMove(game.copy(), -1);
			case 3:
				return ghostsL.getMove(game.copy(), -1);
			default:
				System.err.println("INVALID GHOST TYPE");
				System.exit(1);
				return null;				// Compilier is complaining...
		}
	}
}
