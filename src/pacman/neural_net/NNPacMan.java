package pacman.neural_net;

import pacman.entries.ghosts.ChaserGhosts;
import pacman.entries.ghosts.LineGhosts;
import pacman.entries.ghosts.RandomGhosts;
import pacman.entries.ghosts.StandardGhosts;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.io.*;
import java.util.EnumMap;

public class NNPacMan {
    private JyNN brain;
    private float [] rndS = {0.7f, 0.2f, 0.05f, 0.05f}; //Define probability distribution of choices
    private float [] rndtry = {0.8f, 0.1f, 0.1f, 0f}; //Define probability distribution of blind tries
    private float [][] rndT; //Storing random table
    private float [][] rndTryT; //Storing another table for blind tries
    private float predictF = 0.99f; //Trust on prediction.

    private static StandardGhosts StdGhosts = new StandardGhosts();
    private static RandomGhosts RndGhosts = new RandomGhosts();
    private static ChaserGhosts CasGhosts = new ChaserGhosts();
    private static LineGhosts LinGhosts = new LineGhosts();

    public NNPacMan(Game game) {
        brain = new JyNN(0.1f, 0.00001f, game.getNumberOfNodes() - 1, 100, 4);

        //Setting random table
        rndT = new float[4][];
        rndT[0] = new float [] {1f,1f,1f,1f};
        rndT[1] = new float [] {rndS[0]/(rndS[0]+rndS[1]),1f,1f,1f};
        rndT[2] = new float [] {rndS[0]/(1f - rndS[3]),(rndS[0]+rndS[1])/(1f - rndS[3]),1f,1f};
        rndT[3] = new float [] {rndS[0],rndS[0]+rndS[1],1f - rndS[3],1f};

        //Setting random try table
        rndTryT = new float[4][];
        rndTryT[0] = new float [] {1f,1f,1f,1f};
        rndTryT[1] = new float [] {rndtry[0]/(rndtry[0]+rndtry[1]),1f,1f,1f};
        rndTryT[2] = new float [] {rndtry[0]/(1f - rndtry[3]),(rndtry[0]+rndtry[1])/(1f - rndtry[3]),1f,1f};
        rndTryT[3] = new float [] {rndtry[0],rndtry[0]+rndtry[1],1f - rndtry[3],1f};
    }

    /** Encoding the graph **/
    public static Jymax readNode(Game game) {
        Jymax out = new Jymax(1, game.getNumberOfNodes() - 1);
        float[] x = out.getMat()[0];
        //Mark pill
        for(int i = 0; i <game.getPillIndices().length; i++)
            x[game.getPillIndices()[i]] = game.isPillStillAvailable(i) ? 0.002f : 0f;
        //Mark Power pill
        for(int i = 0; i <game.getPowerPillIndices().length; i++)
            x[game.getPowerPillIndices()[i]] = game.isPowerPillStillAvailable(i) ? 0.01f : 0f;
        //Mark Ghosts
        int i;
        for(GHOST type : GHOST.values()){  //Mark GHOST
            i = game.getGhostCurrentNodeIndex(type);
            if(i >= x.length)
                continue;
            x[i] = (game.isGhostEdible(type)&&(x[i]!=-1)) ? 0.04f : -1f;
        }
        //Mark PacMan
        x[game.getPacmanCurrentNodeIndex()] = 1f;
        return out;
    }

    /**
     * Sorting possible moves base on their score
     * Use Brain before using this!
     **/
    private static MOVE[] sortPMoves(Game game, float [] sList) {
        MOVE[] pMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex()); //Get possible moves
        // Sort possible moves base on score
        MOVE swap;
        int temp;
        for (int i = 0; i < pMoves.length; i++) {
            temp = i;
            for (int j = i + 1; j < pMoves.length; j++)
                temp = (sList[pMoves[temp].ordinal()] < sList[pMoves[j].ordinal()]) ? j : temp;
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
        MOVE[] rMoves = sortPMoves(game, brain.X[brain.layer - 1].getMat()[0]); //Find Sorted possible moves
        //Select random move
        if(rnd){
            float [] rndP = rndT[rMoves.length-1];  //Fetch CPD that matches the # of possible moves.
            float R = (float)Math.random();  //Roll dice
            return rMoves[(R>rndP[0] ? 1 : 0) + (R>rndP[1] ? 1 : 0) + (R>rndP[2] ? 1 : 0)];
        }else{
            return rMoves[0]; //return best choice
        }
    }

    /**
     * return Pacman move base on current game, using given score vector
     * rnd for setting whether using random strategy
     **/
    public MOVE getMove(Game game, Jymax out, boolean rnd) {
        MOVE[] rMoves = sortPMoves(game, out.getMat()[0]); //Find Sorted possible moves
        //Select random move
        if(rnd){
            float [] rndP = rndT[rMoves.length-1];  //Fetch CPD that matches the # of possible moves.
            float R = (float)Math.random();  //Roll dice
            return rMoves[(R>rndP[0] ? 1 : 0) + (R>rndP[1] ? 1 : 0) + (R>rndP[2] ? 1 : 0)];
        }else{
            return rMoves[0]; //return best choice
        }
    }

    /** Evaluate current situation using NN **/
    private float NNevalScore(Game game){
        if(game.getPacmanNumberOfLivesRemaining()==0)
            return -10000;  //If Pacman get killed, just return a large negative number.
        brain.forward(readNode(game)); //Analyze game
        float [] sList = brain.X[brain.layer - 1].getMat()[0]; //get score list
        MOVE[] rMoves = sortPMoves(game, sList); //Find Sorted possible moves
        float [] rndP = rndT[rMoves.length-1]; //Get probability distribution for current moves
        float score = rndP[0]*sList[rMoves[0].ordinal()]; //Calculate overall score base on probability distribution
        for(int i = 1; i < rMoves.length; i++)
            score += (rndP[i]-rndP[i-1])*sList[rMoves[i].ordinal()];
        return score;
    }

    /** Try a move on current game and evaluate gains
     * Must use game.copy() to pass game.
     * Otherwise, it will mess up your current game **/
    private float pseudoPlay(Game game, MOVE nextM){
        int rs = game.getScore();
        game.advanceGame(nextM, GhostMove(game, game.constants.GHOST_TYPE)); //Play a move
        rs = game.getScore() - rs; //Find actual score changes.
        return (float)rs + NNevalScore(game)*this.predictF; //Calculate more accurate version of score
    }

    /** Try a sequence of random moves on current game and evaluate gains
     * Must use game.copy() to pass game.
     * Otherwise, it will mess up your current game **/
    private float pseudoPlay(Game game, float [] pdMoves, int depth){
        int rs = game.getScore();
        //int rt = game.getTotalTime();
        float [] rndP;
        MOVE nextM;
        float R;
        MOVE[] rMoves = sortPMoves(game, pdMoves); //First step towards the highest
        game.advanceGame(rMoves[0], GhostMove(game, game.constants.GHOST_TYPE));
        if(game.getPacmanNumberOfLivesRemaining()==0)
            return -10000;  //If Pacman get killed, just return a large negative number.
        while((!game.gameOver()&&(depth != 0))){ //Keep random trying until die
            rMoves = sortPMoves(game, pdMoves); //Find Sorted possible moves
            //Select random move
            rndP = this.rndTryT[rMoves.length-1];
            R = (float)Math.random();  //Roll dice
            nextM = rMoves[(R>rndP[0] ? 1 : 0) + (R>rndP[1] ? 1 : 0) + (R>rndP[2] ? 1 : 0)];
            game.advanceGame(nextM, GhostMove(game, game.constants.GHOST_TYPE)); //Play a move
            depth -= 1;
        }
        rs = game.getScore() - rs; //Find actual score changes.
        //rt = game.getTotalTime() - rt; //Find how long past.
        if(!game.gameOver()){
            return (float)rs + NNevalScore(game)*this.predictF;
        }else {
            return (float)rs;
        }
    }

    /** A strategist used as teacher for NN. AKA reference strategy **/
    private Jymax strategist(Game game, int depth){
        // Random search four actions and generate evaluated score
//        float [] nextS = {pseudoPlay(game.copy(),MOVE.UP),pseudoPlay(game.copy(),MOVE.RIGHT),
//                pseudoPlay(game.copy(),MOVE.DOWN),pseudoPlay(game.copy(),MOVE.LEFT)};
        float [] nextS = {pseudoPlay(game.copy(),new float[] {10,5,1,5},depth),
                pseudoPlay(game.copy(),new float[] {5,10,5,1},depth),
                pseudoPlay(game.copy(),new float[] {1,5,10,5},depth),
                pseudoPlay(game.copy(),new float[] {5,1,5,10},depth)};
        return new Jymax(nextS);
    }

    /** Reference strategy Player **/
    public MOVE TgetMove(Game game, int depth){
        return sortPMoves(game,strategist(game, depth).getMat()[0])[0];
//        Jymax A = strategist(game, depth);
//        System.out.println(A);
//        return sortPMoves(game,A.getMat()[0])[0];
    }

    /** Train a round **/
    public void episode(Game game){
        Jymax label;
        Jymax prediction = new Jymax(1,4); //Set container for prediction;
        while(!game.gameOver()) {
            label = strategist(game,20); //Get precise evaluation
            brain.forward(readNode(game)); //Let brain think
            prediction.set(brain.X[brain.layer - 1]); //Update prediction result;
            brain.backUpdate(label); //Train brain
//            game.advanceGame(getMove(game,prediction,true), //Let brain play
//                    GhostMove(game, game.constants.GHOST_TYPE));
            game.advanceGame(getMove(game,label,false), //Let teacher play
                    GhostMove(game, game.constants.GHOST_TYPE));
        }
    }

    public void train(Game game, int count){
        float percent;
        for(int i=0; i<count; i++){
            episode(game.copy());
            percent = ((float)i)/((float)count)*100;
            if(percent % 1 == 0){
                System.out.println(percent+" %");
            }
        }
    }

    /** Save current brain to file **/
    public void saveBrain(String name){
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(name));
            os.writeObject(this.brain);
            os.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /** Load saved brain from file **/
    public void loadBrain(String name){
        try{
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(name));
            brain =(JyNN) is.readObject();
            is.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /** Helper function to get ghosts action **/
    private static EnumMap<GHOST, MOVE> GhostMove(Game game, int ghostType){
        switch(ghostType){
            case 0:
                return RndGhosts.getMove(game.copy(), -1);
            case 1:
                return StdGhosts.getMove(game.copy(), -1);
            case 2:
                return CasGhosts.getMove(game.copy(), -1);
            case 3:
                return LinGhosts.getMove(game.copy(), -1);
            default:
                System.err.println("INVALID GHOST TYPE");
                System.exit(1);
                return null;				// Compilier is complaining...
        }
    }
}
