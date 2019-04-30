package pacman.neural_net;

/**
 * Abstract class for user defined activation function
 **/
public abstract class JyAct {

    public JyAct() {
    }

    /**
     * Activation function
     **/
    public abstract float k(float x);

    /**
     * Derivative of the activation function
     **/
    public abstract float dk(float x);
}
