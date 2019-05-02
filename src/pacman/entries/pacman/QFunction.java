package pacman.entries.pacman;

import java.io.Serializable;

/**
 * A linear function of the feature values.
 */
public class QFunction implements Serializable {

	private double[] weights; // Weight vector
	private double bias;
	private int length;
	
	private double[] elig; // eligibility traces
	private double e_bias;
	
	/** Start with everything at zero. */
	public QFunction(FeatureSet prototype) {
		weights = new double[prototype.size()];
		elig = new double[prototype.size()];
		length = weights.length;
	}

	/** Estimate the Q-value given the features for an action. */
	public double getQ(FeatureSet features) {
		double sum = bias;
		for (int i = 0; i < length; i++) {
            sum = sum + features.get(i) * weights[i];
        }
		return sum;
	}

	/** Gradient-descent weight update - with eligibility traces. */
	public double updateWeights(double update) {
		double sum = 0;

	    for (int i = 0; i < length; i++) {
            weights[i] = weights[i] + update * elig[i];
            sum+= Math.abs(update * elig[i]);
        }
		bias = bias + update * e_bias;
		return sum;
	}
	
	/** Zero out the eligibility traces. */
	public void clearTraces() {
		for (int i = 0; i < length; i++) {
            elig[i] = 0;
        }
		e_bias = 0;
	}
	
	/** Decrease the eligibility traces. */
	public void decayTraces(double update) {
		for (int i = 0; i < length; i++) {
            elig[i] = elig[i] * update;
        }
		e_bias = e_bias * update;
	}
	
	/** Increase the eligibility traces. */
	public void addTraces(FeatureSet features) {
		for (int i = 0; i < length; i++) {
            elig[i] = elig[i] + features.get(i);
        }
		e_bias++;
	}

}
