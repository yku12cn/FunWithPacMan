package pacman.neural_net;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class NNPacMan {
    private JyNN brain;
    private float [] rndS = {0.7f, 0.2f, 0.05f, 0.05f}; //Define probability distribution of choices
    private float [][] rndT; //Storing random table

    public NNPacMan(Game game) {
        brain = new JyNN(0.1f, 0.00001f, game.getNumberOfNodes() - 1, 100, 4);

        //Setting random table
        rndT = new float[4][];
        rndT[0] = new float [] {1f,1f,1f,1f};
        rndT[1] = new float [] {rndS[0]/(rndS[0]+rndS[1]),1f,1f,1f};
        rndT[2] = new float [] {rndS[0]/(1f - rndS[3]),(rndS[0]+rndS[1])/(1f - rndS[3]),1f,1f};
        rndT[3] = new float [] {rndS[0],rndS[0]+rndS[1],1f - rndS[3],1f};
    }

    public static Jymax readNode(Game game) {
        Jymax out = new Jymax(1, game.getNumberOfNodes() - 1);
        float[] x = out.getMat()[0];

        return out;
    }

    /**
     * Sorting possible moves base on their score
     * Use Brain before using this!
     **/
    private MOVE[] sortPMoves(Game game) {
        float[] out = brain.X[brain.layer - 1].getMat()[0]; //get score
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
     * rnd for setting whether using random strategy
     **/
    public MOVE getMove(Game game, boolean rnd) {
        brain.forward(readNode(game)); //Analyze game
        MOVE[] rMoves = sortPMoves(game); //Find Sorted possible moves
        //Select random move
        if(rnd){
            float [] rndP = rndT[rMoves.length];  //Fetch CPD that matches the # of possible moves.
            float R = (float)Math.random();  //Roll dice
            return rMoves[(R>rndP[0] ? 1 : 0) + (R>rndP[1] ? 1 : 0) + (R>rndP[2] ? 1 : 0)];
        }else{
            return rMoves[0]; //return best choice
        }
    }
}
