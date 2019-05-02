package pacman;

import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.entries.ghosts.StandardGhosts;
import pacman.entries.pacman.CustomFeatureSet;
import pacman.entries.pacman.FeatureSet;
import pacman.entries.pacman.QPacMan;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

import java.util.EnumMap;
import java.util.Random;

public class PacmanDemo {

    public static void init(){
        defaultC = new Constants();
        defaultC.MAZE_NUM = maze_num;
        defaultC.GHOST_SPEED_REDUCTION = ghostSlowdown;
        defaultC.NUM_GHOSTS = ghost_num;
    }

    private static EnumMap<GHOST, MOVE> getGhostMove(Game game){
        return ghostsS.getMove(game.copy(), -1);
    }

    /** Human controller demo **/
    public static void humanDemo(){
        HumanController human = new HumanController(new KeyBoardInput());
        init();
        defaultC.DELAY = defaultC.DELAY*10;

        Game game=new Game(random.nextLong(), defaultC);
        GameView gv=new GameView(game).showGame();
        gv.getFrame().addKeyListener(human.getKeyboardInput());

        while(!game.gameOver()) {
            game.advanceGame(human.getMove(game, -1), getGhostMove(game));
            try {
                Thread.sleep(defaultC.DELAY);
            } catch (Exception e) {
            }
            gv.repaint();
        }
    }

    /** RL demo **/
    public static void RLdemo (){

        init();
        defaultC.DELAY = defaultC.DELAY*5;
        FeatureSet feature = new CustomFeatureSet();


        QPacMan pacman = new QPacMan(feature);
//        double[] initialData = new double[0];
//
//        // evaluate the random policy
//        double [] result = perform(pacman, test_time);
//
//        System.out.println("score at start = " + result[0]);
//
//        StandardChartTheme mChartTheme = new StandardChartTheme("CN");
//        mChartTheme.setLargeFont(new Font("Sans-serif", Font.BOLD, 20));
//        mChartTheme.setExtraLargeFont(new Font("Sans-serif", Font.PLAIN, 15));
//        mChartTheme.setRegularFont(new Font("Sans-serif", Font.PLAIN, 15));
//        ChartFactory.setChartTheme(mChartTheme);
//        DefaultCategoryDataset mDataset = new DefaultCategoryDataset();
//
//
//        int num = 0;
//        int show = 1;
//        double show_score = 0;
//        ArrayList<String> strs = new ArrayList<>();
//        for (int x = 1; x <= train_time; x++) {
//
//            double error = train(pacman);
//            num++;
//
//            double [] result1 = perform(pacman, test_time) ;
//            double score = result1[0];		// average score over test number of games
//
//            show++;
//            show_score = show_score*0.9 + score*0.1;
//            if (show%10==0) {
//                mDataset.addValue(show_score, "Scores", Integer.toString(show));
//            }
//
//            strs.add(error + ","+score+"\n");
//            System.out.println("episode: " + num + "   score: " + score);
//        }
//
//        String fileName = "error2.csv";
//        try {
//            FileWriter writer = new FileWriter(fileName);
//            for (int i = 0; i < strs.size(); i++) {
//                writer.write(strs.get(i));
//            }
//            writer.close();
//        }
//        catch (Exception e) {
//
//        }
//        try {
//            pacman.save();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//        JFreeChart mChart = ChartFactory.createLineChart(
//                "Score Trend",
//                "eposide",
//                "Scores",
//                mDataset,
//                PlotOrientation.VERTICAL,
//                true,
//                true,
//                false);
//
//        CategoryPlot mPlot = (CategoryPlot)mChart.getPlot();
//        mPlot.setBackgroundPaint(Color.LIGHT_GRAY);
//        mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
//        mPlot.setOutlinePaint(Color.BLACK);//边界线
//
//        ChartFrame mChartFrame = new ChartFrame("Score Trend", mChart);
//        mChartFrame.pack();
//        mChartFrame.setVisible(true);
//
//
        // vision of the game
        Game game=new Game(random.nextLong(), defaultC);
        System.out.println(game.getPillIndices().length);
        pacman.initialize(game, true);
        GameView gv=new GameView(game).showGame();


        try {
            pacman.load();
        }catch (Exception e) {
            e.printStackTrace();
        }


        while(!game.gameOver()) {
            game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
            pacman.prepare(game);
            try{
                Thread.sleep(defaultC.DELAY);
            }catch(Exception e){
                e.printStackTrace();
            }
            gv.repaint();
        }
        System.out.println("Training Done");
    }

    /** Train a learner */

    public static double train(QPacMan pacman) {

        Game game = new Game(random.nextLong(), defaultC);
        pacman.initialize(game, false);
        double sum = 0;
        while(!game.gameOver()) {
            game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
            sum += Math.abs(pacman.prepare(game));
        }

        return sum;
    }

    /** Estimate the current performance of a learner. */
    public static double[] perform(QPacMan pacman, int num) {

        double[] performance = new double[2];
        double scores = 0;
        double steps = 0;

        for(int i = 0; i < num; i++) {
            Game game = new Game(random.nextLong(), defaultC);
            pacman.initialize(game, true);
            while(!game.gameOver()) {
                game.advanceGame(pacman.getMove(game.copy(), -1), getGhostMove(game));
                pacman.prepare(game);
            }
            scores += game.getScore();
            steps += game.getTotalTime();
        }
        performance[0] = scores/num;
        performance[1] = steps/num;
        return performance;
    }


    public static String Choice = "customS";
    public static int train_time = 2000;
    public static int test_time = 1; // test episodes
    public static Random random = new Random();
    public static StandardGhosts ghostsS = new StandardGhosts();

    public static int maze_num = 2;
    // ghost speed: 1 = frozen
    public static int ghostSlowdown = 2;
    public static int ghost_num = 4;
    public static Constants defaultC;
}
