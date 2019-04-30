package pacman.neural_net;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class NNPacMan {
    JyNN brain;

    public NNPacMan(Game game) {
        brain = new JyNN(0.1f, 0.00001f, game.getNumberOfNodes() - 1, 100, 4);

    }

    public static Jymax readNode(Game game) {
        Jymax out = new Jymax(1, game.getNumberOfNodes() - 1);
        float[] x = out.getMat()[0];

        return out;
    }

    /**
     * Sorting possible moves base on their score
     **/
    private MOVE[] sortPMoves(Game game, Jymax score) {
        float[] out = score.getMat()[0]; //get score
        MOVE[] pMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex()); //Get possible moves
        // Sort possible moves base on score
        MOVE swap;
        int temp;
        for (int i = 0; i < pMoves.length; i++) {
            temp = i;
            for (int j = i + 1; j < pMoves.length; j++)
                temp = (out[pMoves[temp].ordinal()] < out[pMoves[j].ordinal()]) ? j : temp;
            if (temp != i) {
                swap = pMoves[i];
                pMoves[i] = pMoves[temp];
                pMoves[temp] = swap;
            }
        }
        return pMoves;
    }

    /**
     * return Pacman move base on current game
     **/
//    public MOVE getMove(Game game) {
//        brain.forward(readNode(game)); //Analyze game
//        MOVE[] rMoves = sortPMoves(game, brain.X[brain.layer - 1]); //Find Sorted possible moves
//        //
//
//    }
}
