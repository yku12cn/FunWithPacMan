package pacman;

import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.game.Constants;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import java.util.Random;
import javax.imageio.ImageIO;
import pacman.entries.ghosts.*;
import pacman.entries.pacman.CustomFeatureSet;
import pacman.entries.pacman.DepthFeatureSet;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QPacMan;

import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

public class PacmanDemo {

    public static String Choice = "depthS";
    public static int Train_Time = 400;
    public static int TEST = 1; //30; // Test episodes per point
    public static Random rng = new Random();
    public static StandardGhosts ghostsS = new StandardGhosts();



    public static int maze_num = 2;
    // ghost speed: 1 = frozen
    public static int ghostSlowdown = 2;
    public static int ghost_num = 4;

    public static Constants defaultConstants;

    /** Human demo **/
    public static void human(){
        HumanController human = new HumanController(new KeyBoardInput());
        defaultConstants = new Constants();
        defaultConstants.MAZE_NUM = maze_num; //which maze;

        defaultConstants.GHOST_SPEED_REDUCTION = ghostSlowdown; //ghost speed
        defaultConstants.NUM_GHOSTS = ghost_num; //how many ghosts

        //increase delay for watching
        defaultConstants.DELAY = defaultConstants.DELAY*10;

        Game game=new Game(rng.nextLong(), defaultConstants);
        GameView gv=new GameView(game).showGame();

        gv.getFrame().addKeyListener(human.getKeyboardInput());

        while(!game.gameOver()) {
            game.advanceGame(human.getMove(game, -1), getGhostMove(game));

            try {
                Thread.sleep(defaultConstants.DELAY);
            } catch (Exception e) {
            }
            gv.repaint();
        }
    }

    /** RL demo **/
    public static void demo(){
        defaultConstants = new Constants();

        defaultConstants.MAZE_NUM = maze_num; //which maze;
        defaultConstants.GHOST_SPEED_REDUCTION = ghostSlowdown; //ghost speed
        defaultConstants.NUM_GHOSTS = ghost_num; //how many ghosts

        //increase delay for watching
        defaultConstants.DELAY = defaultConstants.DELAY*5;

        //train
        train(0,true,false);

    }

    /** Set up a learner. */
    public static QPacMan create() {


        FeatureSet feature = Choice.startsWith("custom") ? new CustomFeatureSet() : new DepthFeatureSet();

        return new  QPacMan(feature);

    }

    /** Generate learning curves. */
    public static void train(int start, boolean watchAtEnd, boolean watchDuring) {


        // This just sets up the student or teacher with the specified feature set and learning algorithm
        QPacMan pacman = create();

        // First point
        double[] initialData = pacman.episodeData();		// Starts off empty

        // evaluate the random policy
        double [] eval_result = evaluate(pacman, TEST);

        System.out.println("score at start = "+eval_result[0]);

        int episode_num = 0;

        // for each train episode...
        for (int x=1; x<=Train_Time; x++) {
            double[] data = new double[initialData.length];

            //make PacMan play an episode
            episode(pacman);
            episode_num++;

            double[] episodeData = pacman.episodeData();
            for (int d=0; d<data.length; d++)
                data[d] += episodeData[d];


            double [] eval_result2 =evaluate(pacman, TEST) ;
            double score = eval_result2[0];		// This returns the average score over TEST number of games

            System.out.println("episode: "+episode_num+"   score: "+score);

            if (watchDuring){
                watch(pacman,true);
            }

        }

        if (watchAtEnd)
            watch(pacman, false);

        System.out.println("Training Done.");
    }

    /** Train a learner for one more episode. */

    public static void episode(QPacMan pacman) {

        Game game = new Game(rng.nextLong(), defaultConstants);
        pacman.startEpisode(game, false);

        while(!game.gameOver()) {
            game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
            pacman.processStep(game);
        }
    }

    /** Estimate the current performance of a learner. */
    public static double[] evaluate(QPacMan pacman, int num) {

        double[] scoreAndTime = new double[2];
        double sumScore = 0;
        double sumSteps = 0;

        for(int i = 0; i < num; i++) {

            Game game = new Game(rng.nextLong(), defaultConstants);
            pacman.startEpisode(game, true);
            while(!game.gameOver()) {
            // getMove in pacman learner returns the lastAction
            // This will then recompute the next move to make.
                game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
                pacman.processStep(game);
            }

            sumScore += game.getScore();
            sumSteps += game.getTotalTime();
        }

        scoreAndTime[0] = sumScore/num;
        scoreAndTime[1] = sumSteps/num;


        return scoreAndTime;
    }


    /** Observe a learner play a game. */
    public static void watch(QPacMan pacman, boolean destroyWindow) {


        Game game=new Game(rng.nextLong(), defaultConstants);

        pacman.startEpisode(game, true);
        GameView gv=new GameView(game).showGame();

        while(!game.gameOver()) {

            game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
            pacman.processStep(game);

            try{Thread.sleep(defaultConstants.DELAY);}catch(Exception e){}
            gv.repaint();
            Image img_i = gv.createImage(100, 100);
            BufferedImage bi = (BufferedImage)img_i;
            File f = new File("./output.png");
            try {
                ImageIO.write(bi, "png", f);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (destroyWindow){
            gv.setVisible(false);
            gv.setEnabled(false);
            gv.getFrame().dispose();
        }
    }

    private static EnumMap<GHOST, MOVE> getGhostMove(Game game){
        return ghostsS.getMove(game.copy(), -1);
    }
}
