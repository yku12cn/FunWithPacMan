package pacman.entries.pacman;

import pacman.controllers.Controller;
import java.util.Random;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

public class SarsaPacMan extends Controller<MOVE> {

    /** Initialize the policy. */
    public SarsaPacMan(FeatureSet proto) {
        prototype = proto;
        Qfunction = new QFunction(prototype);
    }

    /** Prepare for the first move. */
    public void initialize(Game game, boolean test) {
        testMode = test;
        lastScore = 0;
        Qfunction.clearTraces();
        update = false;
        delta1 = 0;
        delta2 = 0;
        evaluateMoves(game);
    }

    /** Choose a move. */
    public MOVE getMove(Game game, long timeDue) {
        return actions[last];
    }

    /** Override the move choice. */
    public void setMove(MOVE move) {
        last = -1;
        for (int i=0; i<actions.length; i++)
            if (actions[i] == move)
                last = i;
    }

    /** Learn if appropriate, and prepare for the next move. */
    public void prepare(Game game) {

        // Do a delayed gradient-descent update
        if (update) {
            delta2 = r * Qs[last];
            Qfunction.updateWeights(alpha * (delta1+delta2));
        }
        // Eligibility traces
        Qfunction.decayTraces(r*lambda);
        Qfunction.addTraces(features[last]);

        // Q-value correction
        double reward = game.getScore() - lastScore;
        lastScore = game.getScore();
        delta1 = reward - Qs[last];

        if (!game.gameOver())
            evaluateMoves(game);

        if (!testMode) {
            if (game.gameOver()){
                Qfunction.updateWeights(alpha*delta1);
            }
            else
                update = true;
        }
    }

    /** Compute predictions for moves in this state. */
    private void evaluateMoves(Game game) {

        actions = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        int length = actions.length;
        features = new FeatureSet[length];
        best = 0;
        Qs = new double[length];
        for (int i=0; i<length; i++){
            features[i] = prototype.extract(game, actions[i]);
            Qs[i] = Qfunction.evaluate(features[i]);
            if (Qs[i] > Qs[best])
                best = i;

        }
        // explore or exploit
        if (!testMode && random.nextDouble() < e)
            last = random.nextInt(length);
        else
            last = best;

    }

    private Random random = new Random();
    private FeatureSet prototype;
    private QFunction Qfunction; // Learned policy

    private MOVE[] actions; // Actions possible in the current state
    private double[] Qs; // Q-values for actions in the current state
    private FeatureSet[] features; // Features for actions in the current state

    private int lastScore; // Last known game score
    private int best; // Index of current best action
    private int last; // Index of action actually being taken
    private boolean testMode;
    private boolean update;
    private double delta1; // First part of delayed update: r-Q(s,a)
    private double delta2; // Second part of delayed update: yQ(s',a')

    private double e = 0.01; // Exploration rate
    private double alpha = 0.01; // Learning rate
    private double r = 0.999; // Discount rate
    private double lambda = 0.9; // Backup weighting
}
