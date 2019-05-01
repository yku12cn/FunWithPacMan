package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class CustomFeatureSet extends FeatureSet {

	private int DEPTH; // Depth controls how many junctions we will search
	private int FEATURES; // DEPTH + 3
	private double MAX_DISTANCE;
	private double MAX_SCORE;
	private ArrayList<HashMap<Integer,Double>> junctions;
	// There will be DEPTH hashmaps in this arraylist. For each hashmaps, it would populate that junction node with its (max) safety score.
	private double powerDepth;
	private double pillDepth;
	public double[] values;
	// the value for each features, the first DEPTH is the safty score for every junction, the fifth is used to measure the distance
	// with the closest pill, the sixth is used to measure the score of current move, the seventh is used to measure the best score in
	// other possible move
	private Game GAME;

	public CustomFeatureSet() {
		DEPTH = 4;
		FEATURES = 7;
		init();
	}
	
	public CustomFeatureSet(int depth){		
		DEPTH = depth;
		FEATURES = DEPTH + 3;
		init();
	}
	
	private void init(){
		MAX_DISTANCE = 200;
		MAX_SCORE = 250; //(MAX_DISTANCE/4)^2, according to the score method
		values = new double[FEATURES];
		powerDepth = pillDepth = DEPTH;
		junctions = new ArrayList<>();
		for (int i=0; i<DEPTH; i++)
			junctions.add(new HashMap<>());
	}

	public int size() {
		return FEATURES;
	}

	public double get(int i) {
		return values[i];
	}

	// calculate the values array and use it to build a new FeatureSet
	public FeatureSet extract(Game game, MOVE move) {
		CustomFeatureSet features = new CustomFeatureSet();
		features.setValues(game, move);
		return features;
	}

	// calculate each features and store them in values array
	private void setValues(Game game, MOVE move) {

		GAME = game;
		int node = GAME.getPacmanCurrentNodeIndex();
		exploreJunctions(node, move, 0, 0);

		double[] safety = new double[DEPTH];
		double min = Double.POSITIVE_INFINITY;
		for (int i=0; i<DEPTH; i++) {
			for (Integer n : junctions.get(i).keySet()) {
				safety[i] = Math.max(safety[i], junctions.get(i).get(n));
				safety[i] = Math.min(min, safety[i]);
			}
			min = Math.min(safety[i], min);
		}

		double[] startPath = initPath();
		double[] feastPath = exploreFeasts(node, move, 0, 0, startPath);
        double feastScore = score(feastPath);

		// Upcoming feast opportunity?
		double futureFeastScore = 0;
		MOVE[] possibleMoves = GAME.getPossibleMoves(node);
		for (MOVE possibleMove : possibleMoves) {
			double[] initialPath = initPath();
			double[] path = exploreEnemies(node, possibleMove, 0, 0, initialPath);
            futureFeastScore = Math.max(futureFeastScore, score(path));
		}

		// Features
		int v = 0;
		for (int i=0; i<DEPTH; i++)
			values[v++] = safety[i] / MAX_DISTANCE; // How safe junctions are
		values[v++] = (DEPTH - pillDepth) / DEPTH; // How close a pill is
		values[v++] = feastScore / MAX_SCORE; // How good feasts look
		values[v++] = futureFeastScore / MAX_SCORE / (powerDepth+1); // How good future feasts look
		
		if (v != values.length) {
			System.out.println("Feature vector length error: said "+values.length+", got "+v);
			System.exit(0);
		}
	}

	private double[] initPath() {

		double[] path = new double[DEPTH];
		for (int i = 0; i < DEPTH; i++) {
			path[0] = MAX_DISTANCE;
		}
		return path;

	}

	/** Search up to a few junctions ahead in this direction. */
//	Searches up to DEPTH junctions, calculates the safety of that node, and populates it into junctions
	private void exploreJunctions(int node, MOVE move, int depth, double distance) {

		if (depth >= DEPTH) return;
		boolean pillInSegment = false;
		boolean powerInSegment = false;

		while (true) {
			node = GAME.getNeighbour(node, move);
			distance++;

// Basically nothing gets populated if there is a ghost on the next node			
			// Stop for an approaching enemy
			for (GHOST ghost : GHOST.values()) {
				if (GAME.getGhostCurrentNodeIndex(ghost) == node && move != GAME.getGhostLastMoveMade(ghost) && !GAME.isGhostEdible(ghost))
					return;
			}

			// Notice a power pill
// This part updates powerDepth			
			int powerIndex = GAME.getPowerPillIndex(node);
			if (powerIndex > -1 && GAME.isPowerPillStillAvailable(powerIndex)) {

				double safety = safety(node, distance);
				if (safety <= 0) return;

				powerInSegment = true;
				if (depth < powerDepth) powerDepth = depth;
			}

			// Notice a regular pill (maybe)
			int pillIndex = GAME.getPillIndex(node);
			if (pillIndex > -1 && GAME.isPillStillAvailable(pillIndex)) pillInSegment = true;

			// Notice a junction
			if (GAME.isJunction(node)) {
				
				double safety = safety(node, distance);
				if (safety <= 0) return;

				if (!junctions.get(depth).containsKey(node) || junctions.get(depth).get(node) < safety)
					junctions.get(depth).put(node, safety);

				// Really notice a regular pill (only in segments without power and with safe exits)
				if (!powerInSegment && pillInSegment && depth < pillDepth)
					pillDepth = depth;
			}

			// Split at a junction
			if (GAME.isJunction(node)) {

				MOVE[] possibleMoves = GAME.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves)
					exploreJunctions(node, possibleMove, depth+1, distance);
				return;
			} else if (GAME.getNeighbour(node, move) == -1)
				move = GAME.getPossibleMoves(node, move)[0]; // // Turn when we meet a corner
		}
	}

	/** Find the highest-scoring path (ghost : depth) towards enemies in this direction. */
	private double[] exploreEnemies(int node, MOVE move, int depth, double distance, double[] path) {

		if (depth >= DEPTH-1) return path;

		// Step
		while (true) {
			node = GAME.getNeighbour(node, move);
			distance++;

			// Notice an approaching enemy
			for (GHOST ghost : GHOST.values()) {
				if (GAME.getGhostCurrentNodeIndex(ghost) == node)
					if (move != GAME.getGhostLastMoveMade(ghost))
						if (!GAME.isGhostEdible(ghost))
							if (distance < path[ghost.ordinal()])
								path[ghost.ordinal()] = distance;
			}

			// Split at a junction
			if (GAME.isJunction(node)) {

				double[] bestPath = path;
                double bestScore = score(path);

				MOVE[] possibleMoves = GAME.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves) {

					double[] pathCopy = Arrays.copyOf(path, path.length);
					double[] newPath = exploreEnemies(node, possibleMove, depth+1, distance, pathCopy);
                    double newScore = score(newPath);

					if (newScore > bestScore) {
						bestPath = newPath;
						bestScore = newScore;
					}
				}

				return bestPath;
			}

			// Turn at a corner
			else if (GAME.getNeighbour(node, move) == -1)
				move = GAME.getPossibleMoves(node, move)[0];
		}
	}

	/** Find the path with shortest distances towards feasts in this direction. */
// Path contains for each ghost, the minimum distance of a safe node	
	private double[] exploreFeasts(int node, MOVE move, int depth, double distance, double[] path) {

		if (depth >= DEPTH+1)
			return path;

		// Step
		while (true) {
			node = GAME.getNeighbour(node, move);
			distance++;

			// Stop for a power pill
			int powerIndex = GAME.getPowerPillIndex(node);
			if (powerIndex > -1 && GAME.isPowerPillStillAvailable(powerIndex))
				return path;

			for (GHOST ghost : GHOST.values()) {
				if (GAME.getGhostCurrentNodeIndex(ghost) == node) {

					// Stop for an approaching enemy
					if (!GAME.isGhostEdible(ghost)) {
						if (move != GAME.getGhostLastMoveMade(ghost))
							return path;
					}

					else {
						
						// Stop for an unsafe feast
						double safety = safety(node, distance);
						if (safety <= 0)
							return path;

						// Otherwise notice it
						if (distance < path[ghost.ordinal()])
							path[ghost.ordinal()] = distance;
					}
				}
			}

			// Split at a junction
			if (GAME.isJunction(node)) {

				double[] bestPath = path;
				//double bestScore = score(path);
                double bestScore = score(path);

				MOVE[] possibleMoves = GAME.getPossibleMoves(node, move);
				for (MOVE possibleMove : possibleMoves) {

					double[] pathCopy = Arrays.copyOf(path, path.length);
					double[] newPath = exploreFeasts(node, possibleMove, depth+1, distance, pathCopy);
					//double newScore = score(newPath);
                    double newScore = score(newPath);

					if (newScore > bestScore) {
						bestPath = newPath;
						bestScore = newScore;
					}
				}

				return bestPath;
			}

			// Turn at a corner
			else if (GAME.getNeighbour(node, move) == -1)
				move = GAME.getPossibleMoves(node, move)[0];
		}
	}


	private double score(double[] path) {
		double score = 0;

		// we can be more aggressive when there's still powerpills, in this case, score would be higher when we get close to ghost
		if (hasPowerPillAvailable()) {
			for (int i = 0; i < path.length; i++)
				score += Math.pow((MAX_DISTANCE - path[i]) / 4, 2);
		} else {
			for (int i = 0; i < path.length; i++)
				score += Math.pow((path[i]) / 4, 2);
		}

		return score / path.length;
	}

	private double safety(Integer node, double myDistance) {

		double enemyDistances = enemyNodeDistances(node);
		if (enemyDistances == MAX_DISTANCE)
			return enemyDistances;
		else
			return enemyDistances - myDistance - GAME.constants.EAT_DISTANCE;

	}

	private double enemyNodeDistances(int node) {

		double tmp;
		double distances;
		distances = MAX_DISTANCE;

		for (GHOST ghost : GHOST.values()) {
			if (!GAME.isGhostEdible(ghost)) {

				int ghostNode = GAME.getGhostCurrentNodeIndex(ghost);
				if (ghostNode != GAME.getCurrentMaze().lairNodeIndex) {
					int myNode = GAME.getPacmanCurrentNodeIndex();
					if (GAME.getDistance(myNode, node, DM.PATH) < GAME.getDistance(myNode, ghostNode, DM.PATH)) {

						MOVE ghostMove = GAME.getGhostLastMoveMade(ghost);
						tmp = GAME.getDistance(ghostNode, node, ghostMove, DM.PATH);
						if (tmp < distances) distances = tmp;
					}
				}
			}
		}

		return distances;

	}

	public boolean hasPowerPillAvailable() {

        int [] indexs = GAME.getPowerPillIndices();
        if (indexs.length==0) return false;
        return true;

    }
}
