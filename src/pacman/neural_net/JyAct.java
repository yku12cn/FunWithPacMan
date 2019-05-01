package pacman.neural_net;

import java.io.Serializable;

/**
 * Abstract class for user defined activation function
 **/
public abstract class JyAct implements Serializable {

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
