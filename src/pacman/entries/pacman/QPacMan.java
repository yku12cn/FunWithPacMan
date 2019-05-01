package pacman.entries.pacman;

import java.util.Random;
import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.MOVE;


public class QPacMan extends Controller<MOVE> {

    /** Initialize the policy. */
    public QPacMan(FeatureSet proto) {
        prototype = proto;
        Qfunction = new QFunction(prototype);
    }

    /** Prepare for the first move. */
    public void initialize(Game game, boolean test) {
        testMode = test;
        lastScore = 0;
        Qfunction.clearTraces();
        evaluateMoves(game);
    }

    /** Choose a move. */
    public MOVE getMove(Game game, long timeDue) {
        return actions[last];
    }

    /** Override the move choice. */
    public void setMove(MOVE move) {
        last = -1;
        for (int i=0; i<actions.length; i++) {
            if (actions[i] == move)
                last = i;
        }
    }

    /** Prepare for the next move, and learn if appropriate. */
    public void prepare(Game game) {

        // Eligibility traces
        if (last != best)
            Qfunction.clearTraces();
        else
            Qfunction.decayTraces(r*lambda);
        Qfunction.addTraces(features[last]);

        // Q-value correction
        double reward = game.getScore() - lastScore;
        lastScore = game.getScore();
        double delta = reward - Qs[last];

        if (!game.gameOver()) {
            evaluateMoves(game);
            delta = delta + r * Qs[best];
        }
        if (!testMode)
            Qfunction.updateWeights(alpha*delta);
    }

    /** Compute predictions for moves in this state. */
    private void evaluateMoves(Game game) {

        actions = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        int length = actions.length;
        features = new FeatureSet[length];
        Qs = new double[length];
        best = 0;
        for (int i=0; i<length; i++) {
            features[i] = prototype.extract(game, actions[i]);
            Qs[i] = Qfunction.evaluate(features[i]);
            if (Qs[i] > Qs[best])
                best = i;
        }
        // Explore or exploit
        if (!testMode && random.nextDouble() < e) {
            last = random.nextInt(length);
        }
        else{
            last = best;
        }
    }

    private Random random = new Random();
    private FeatureSet prototype; // Class to use
    private QFunction Qfunction; // Learned policy

    private MOVE[] actions; // Actions possible in the current state
    private double[] Qs; // Q-values for actions in the current state
    private FeatureSet[] features; // Features for actions in the current state

    private int lastScore; // Last known game score
    private int best; // Index of current best action
    private int last; // Index of action actually being taken
    private boolean testMode;

    private double e = 0.5; // Exploration rate
    private double alpha = 0.001; // Learning rate
    private double r = 0.999; // Discount rate
    private double lambda = 0.7; // Backup weighting
}

