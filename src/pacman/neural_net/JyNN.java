package pacman.neural_net;

public class JyNN {
    private Jymax[] W; //Weights
    private Jymax[] B; //Bias
    Jymax[] X; //Current value for each layer
    private JyAct act; //Activation function
    int layer; //Layer's count

    private float lambda; //l2 regularizer;
    private float step; //Step size;

    /** Init net by size of each layer **/
    public JyNN(float s,float l,int... dd){
        this.lambda = l;
        this.step = s;
        this.layer = dd.length;
        X = new Jymax[this.layer];
        W = new Jymax[this.layer-1];
        B = new Jymax[this.layer-1];

        //Init activation function
        act = new JySigmoid();

        //Init random Weights and Bias
        for(int i =0; i<W.length; i++){
            //Note for bias
            W[i] = new Jymax(dd[i],dd[i+1],3f);
            B[i] = new Jymax(1,dd[i+1],3f);
        }

        //Set layer storage
        for(int i =0; i<X.length; i++){
            X[i] = new Jymax(1,dd[i]);
        }
    }

    /** Set Input layer **/
    public void setIn(float... in){
        if(in.length == X[0].getW()){
            float[] x0 = X[0].getMat()[0];
            for(int i=0; i<in.length; i++){
                x0[i] = in[i];
            }
        }else{
            throw new RuntimeException("Input size don't match");
        }
    }

    /** Forward calculation **/
    public void forward(){
        //Calculate hidden layer
        for(int i=0; i<W.length-1; i++){
            X[i+1].eqmul(X[i],W[i],B[i],this.act);
        }
        //Calculate output layer
        X[this.layer-1].eqmul(X[this.layer-2],W[this.layer-2]);
        X[this.layer-1].add(B[this.layer-2]);
    }

    /** Forward calculation **/
    public void forward(Jymax x){
        //Calculate hidden layer
        X[0].set(x);
        for(int i=0; i<W.length-1; i++){
            X[i+1].eqmul(X[i],W[i],B[i],this.act); //Semi Custom function in Jymax...
        }
        //Calculate output layer
        X[this.layer-1].eqmul(X[this.layer-2],W[this.layer-2]);
        X[this.layer-1].add(B[this.layer-2]);
    }

    /** Backward propagation **/
    public void backUpdate(Jymax Y){
        Jymax dW; //temp gradient for W
        X[X.length-1].sub(Y);
        X[X.length-1].eqmul(2f); // Cal gradient for output
        for(int i = this.layer-1; i>0; i--){
            dW = Jymax.mulT(X[i-1],X[i]); //Cal gradient for W
            dW.add(W[i-1],this.lambda);  //Add regularizer for dW
            X[i-1].eqmulTT(X[i],W[i-1],this.act); //Cal gradient for former layer
            W[i-1].add(dW,-this.step); //Update current layer weights

            //Make use of X[i] for calculating dB
            X[i].add(B[i-1],this.lambda); //Cal dB
            B[i-1].add(X[i],-this.step);
        }
    }

}
