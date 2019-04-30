package pacman.neural_net;

/**
 * Fast sigmoid
 * note irrational derivative for sigmoid!!!
 **/
public class JySigmoid extends JyAct {
    private float[] tb; //lookup table for value
    private float[] tbd; //lookup table interpolation
//    private float[] td; //lookup table for derivative
//    private float[] tdd; //lookup table interpolation
    private int len;
    private float alpha;

    /**
     * Generative lookup table for fast calculation
     **/
    public JySigmoid(int res, float inRng) {
        this.len = res - 1;
        this.tb = new float[res];
        this.tbd = new float[res];
//        this.td = new float[res];
//        this.tdd = new float[res];
        this.alpha = (float) res / inRng;
        double x;
        for (int i = 0; i < res; i++) { //Table
            x = ((double) i) / this.alpha;
            x = 1d / (1d + Math.exp(-x));
            this.tb[i] = (float) x;
//            this.td[i] = (float) (x * (1 - x));
        }
        for (int i = 0; i < res - 1; i++) { //diff table
            this.tbd[i] = (this.tb[i + 1] - this.tb[i]);
//            this.tdd[i] = (this.td[i + 1] - this.td[i]);
        }
        this.tbd[res - 1] = 1 - this.tb[res - 1];
//        this.tdd[res - 1] = 0 - this.td[res - 1];
    }

    public JySigmoid() {
        this(100, 8f);
    }

    /**
     * Calculate sigmoid with linear interpolation
     **/
    public float k(float x) {
        if (x >= 0) {
            x *= this.alpha;
            int p1 = (int) x;
            if (p1 < this.len) {
                x -= p1;
                return tb[p1] + tbd[p1] * x;
            } else {
                return 1f;
            }
        } else {
            x *= -this.alpha;
            int p1 = (int) x;
            if (p1 < this.len) {
                x -= p1;
                return 1f - (tb[p1] + tbd[p1] * x);
            } else {
                return 0;
            }
        }
    }

    /**
     * Calculate derivative of sigmoid with linear interpolation
     * Base on current sigmoid value...
     **/
    public float dk(float x) {
        if((x == 0f) || (x == 1f)) {  //avoid gradient degeneration
            x = tb[tb.length - 1];
        }
        return x*(1-x);

//        x = Math.abs(x);
//        x *= this.alpha;
//        int p1 = (int) x;
//        if (p1 < this.len) {
//            x -= p1;
//            return td[p1] + tdd[p1] * x;
//        } else {
//            return td[this.len - 1];
//        }
    }
}
