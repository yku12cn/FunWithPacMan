package pacman;

import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.game.Constants;
import java.util.*;
import pacman.entries.ghosts.*;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.neural_net.NNPacMan;

public class RunTest {
	
// Note on notation:
// custom refers to the CustomFeatureSet (using something else defaults to DepthFeatureSet)
// S refers to SarsaPacMan (using something else defaults to QPacMan)
	
	
/* 	4 options for RLPacMan learner type
 * 		- teacher: teacher RLPacMan loads policy
 * 		- independent: student starts from scratch
 * 		- transfer: student loads policy
 * 		- (baseline, advise, correct, predict): uses teacher+independent and a strategy for teacher 
 * 
 * 	Notes on folder names in mydata
 * 		- mydata/TEACHER/STUDENT/LEARNER-TYPE_MAZE-NUM/
 */
	public static Random rng = new Random();
	
	// Ghost "policies"
	public static StandardGhosts ghostsS = new StandardGhosts();
    public static RandomGhosts ghostsR = new RandomGhosts();
    public static ChaserGhosts ghostsC = new ChaserGhosts();
    public static LineGhosts ghostsL = new LineGhosts();
    
    public static int ghostType = 1;		// 0 = RandomGhosts, 1 = StandardGhosts, 2 = ChaserGhosts
    
    // Task parameters
    public static int mazeNum = 4;
    // Controls how fast ghosts move when pacman has eaten power pill. Lower numbers = slower (1 = frozen). 
    public static int ghostSlowdown = 2;
    public static int nGhosts = 1;
    
    public static Constants defaultConstants;
    
	/**
	 * Run experiments.
	 */
	public static void main(String[] args) {
        //human();
        NNdemo();
	}

    /** NN demo **/
    public static void NNdemo(){
        defaultConstants = new Constants();
        defaultConstants.MAZE_NUM = mazeNum; //which maze;
        defaultConstants.GHOST_TYPE = ghostType; //which of three types of ghosts 0 = RandomGhosts, 1 = StandardGhosts, 2 = ChaserGhosts
        // Controls how fast ghosts move when pacman has eaten power pill. Lower numbers = slower (1 = frozen).
        defaultConstants.GHOST_SPEED_REDUCTION = ghostSlowdown; //ghost speed
        defaultConstants.NUM_GHOSTS = nGhosts; //how many ghosts

        //increase delay for watching
        defaultConstants.DELAY = defaultConstants.DELAY*10;

        Game game=new Game(rng.nextLong(), defaultConstants);
        NNPacMan brain = new NNPacMan(game);

        brain.train(game,10000);

        HumanController human = new HumanController(new KeyBoardInput());
//        for(int i = 0; i< game.getNumberOfNodes() - 1; i++){
//            System.out.println(game.currentMaze.graph[i].pillIndex);
//        }

        GameView gv=new GameView(game).showGame();
        gv.getFrame().addKeyListener(human.getKeyboardInput());

        while(!game.gameOver()) {
            //game.advanceGame(brain.TgetMove(game,5), getGhostMove(game, game.constants.GHOST_TYPE));
            game.advanceGame(brain.getMove(game,false), getGhostMove(game, game.constants.GHOST_TYPE));
            //System.out.println(game.getTotalTime());
            try {
                Thread.sleep(defaultConstants.DELAY);
            } catch (Exception e) {
            }
            gv.repaint();
        }

    }

	/** Human demo **/
    public static void human(){
	    HumanController human = new HumanController(new KeyBoardInput());
        defaultConstants = new Constants();
        defaultConstants.MAZE_NUM = mazeNum; //which maze;
        defaultConstants.GHOST_TYPE = ghostType; //which of three types of ghosts 0 = RandomGhosts, 1 = StandardGhosts, 2 = ChaserGhosts
        // Controls how fast ghosts move when pacman has eaten power pill. Lower numbers = slower (1 = frozen).
        defaultConstants.GHOST_SPEED_REDUCTION = ghostSlowdown; //ghost speed
        defaultConstants.NUM_GHOSTS = nGhosts; //how many ghosts

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
				return ghostsR.getMove(game, -1);
			case 1:
				return ghostsS.getMove(game, -1);
			case 2:
				return ghostsC.getMove(game, -1);
			case 3:
				return ghostsL.getMove(game, -1);
			default:
				System.err.println("INVALID GHOST TYPE");
				System.exit(1);
				return null;				// Compilier is complaining... 
		}
	}
}