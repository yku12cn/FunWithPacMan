package pacman.entries.pacman;

import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * SARSA(lambda) with function approximation.
 */
public class SarsaPacMan extends BasicRLPacMan {

	private Random rng = new Random();
	private FeatureSet prototype; // Class to use
	private QFunction Qfunction; // Learned policy

	private MOVE[] actions; // Actions possible in the current state
	private double[] qvalues; // Q-values for actions in the current state
	private FeatureSet[] features; // Features for actions in the current state

	private int lastScore; // Last known game score
	private int bestActionIndex; // Index of current best action
	private int lastActionIndex; // Index of action actually being taken
	private boolean testMode; // Don't explore or learn or take advice?
	private boolean doUpdate; // Perform a delayed gradient-descent update?
	private double delta1; // First part of delayed update: r-Q(s,a)
	private double delta2; // Second part of delayed update: yQ(s',a')

	private double EPSILON = 0.05; // Exploration rate
	private double ALPHA = 0.001; // Learning rate
	private double GAMMA = 0.999; // Discount rate
	private double LAMBDA = 0.9; // Backup weighting
	
	public boolean debug = false;

	/** Initialize the policy. */
	public SarsaPacMan(FeatureSet proto) {
		prototype = proto;
		Qfunction = new QFunction(prototype);
		//System.out.println("Featureset size:" + prototype.size());
	}

	/** Prepare for the first move. */
	public void startEpisode(Game game, boolean testMode) {
		this.testMode = testMode;
		lastScore = 0;
		Qfunction.clearTraces();
		doUpdate = false;
		delta1 = 0;
		delta2 = 0;
		evaluateMoves(game);
	}

	/** Choose a move. */
	public MOVE getMove(Game game, long timeDue) {
		return actions[lastActionIndex];
	}
	
	/** Override the move choice. */
	public void setMove(MOVE move) {
		lastActionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				lastActionIndex = i;
	}

	/** Learn if appropriate, and prepare for the next move. */
	public void processStep(Game game) {
		
		
		
		
		// Do a delayed gradient-descent update
		if (doUpdate) {
			delta2 = (GAMMA * qvalues[lastActionIndex]);
			//System.out.println("\t"+ qvalues[lastActionIndex]+"\t"+delta1+"\t"+delta2);
			
			Qfunction.updateWeights(ALPHA*(delta1+delta2));
			
			
			
			
			
			
		}
		
		// Eligibility traces
		Qfunction.decayTraces(GAMMA*LAMBDA);
		Qfunction.addTraces(features[lastActionIndex]);
		
		
		// Q-value correction
		double reward = game.getScore() - lastScore;	
		lastScore = game.getScore();
		delta1 = reward - qvalues[lastActionIndex];
		
		if (!game.gameOver())
			evaluateMoves(game);
		
		// Gradient descent update
		if (!testMode) {
			
			// Right away if game is over
			if (game.gameOver()){
				Qfunction.updateWeights(ALPHA*delta1);
				
			}
			
			// Otherwise delayed (for potential advice)
			else
				doUpdate = true;
		}
	}

	/** Compute predictions for moves in this state. */
	private void evaluateMoves(Game game) {

		actions = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		
		features = new FeatureSet[actions.length];
		for (int i=0; i<actions.length; i++){
			features[i] = prototype.extract(game, actions[i]);
			
			if (debug){
				System.out.print("Features for action "+actions[i]+"\t");
				for (int t = 0; t < features[i].size();t++)
					System.out.print("\t"+features[i].get(t));
				System.out.println();
			}
		}

		qvalues = new double[actions.length];
		for (int i=0; i<actions.length; i++){
			qvalues[i] = Qfunction.evaluate(features[i]);
//			System.out.print(actions[i] + " ");
//			System.out.print(qvalues[i] + " ");
			if (debug){
				System.out.println("Q value for action "+actions[i]+":\t"+qvalues[i]);
			}
		}
		//System.out.println();
		
		
		bestActionIndex = 0;
		for (int i=0; i<actions.length; i++)
			if (qvalues[i] > qvalues[bestActionIndex])
				bestActionIndex = i;

		// Explore or exploit
		if (!testMode && rng.nextDouble() < EPSILON)
			lastActionIndex = rng.nextInt(actions.length);
		else
			lastActionIndex = bestActionIndex;
		
		if (debug){
			Scanner scanner = new Scanner(new InputStreamReader(System.in));
			String input = scanner.nextLine();
		}
	}
	
	/** Get the current possible moves. */
	public MOVE[] getMoves() {
		return actions;
	}
	
	/** Get the current Q-value array. */
	public double[] getQValues() {
		return qvalues;
	}
	
	/** Get the current features for an action. */
	public FeatureSet getFeatures(MOVE move) {
		int actionIndex = -1;
		for (int i=0; i<actions.length; i++)
			if (actions[i] == move)
				actionIndex = i;
		return features[actionIndex];
	}
	
	/** Save the current policy to a file. */
	public void savePolicy(String filename) {
		Qfunction.save(filename);
	}

	/** Return to a policy from a file. */
	public void loadPolicy(String filename) {
		Qfunction = new QFunction(prototype, filename);
	}
	
	public QFunction getQFunction(){
		return Qfunction;
	}
	
}
