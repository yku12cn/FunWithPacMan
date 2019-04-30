package pacman.neural_net;// File:         Jymax.java
// Created:      2019/04/28
// Last Changed: Date: 2019/04/28 14:15:25
// Author:       Yang Kaiyu
//
// History:
//  Revision 1.0 init
//  initial impound
//

import java.util.Random;

public class Jymax {
    private float[][] matrix;
    private int height;
    private int width;

    /**
     * Init h*w size matrix to be all zeros
     **/
    public Jymax(int h, int w) {
        this.matrix = new float[h][w];
        this.height = h;
        this.width = w;
    }

    /**
     * Init h*w size matrix with random numbers from -rng/2 ~ rng/2
     **/
    public Jymax(int h, int w, float rng) {
        this.matrix = new float[h][w];
        this.height = h;
        this.width = w;
        Random r = new Random();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                this.matrix[i][j] = (r.nextFloat()-0.5f)*rng;
            }
        }
    }

    /**
     * Init matrix from 2d array
     **/
    public Jymax(float[][] a2d) {
        this.height = a2d.length;
        this.width = a2d[0].length;
        this.matrix = new float[this.height][];
        for (int i = 0; i < this.height; i++) {
            this.matrix[i] = a2d[i].clone();
        }
    }

    /**
     * Init matrix from 1d array
     **/
    public Jymax(float[] a1d) {
        this.height = 1;
        this.width = a1d.length;
        this.matrix = new float[1][this.width];
        for (int i = 0; i < this.width; i++) {
            this.matrix[0][i] = a1d[i];
        }
    }

    /**
     * Init a matrix with another JY matrix
     **/
    public Jymax(Jymax inMax) {
        this.matrix = new float[inMax.height][];
        this.height = inMax.height;
        this.width = inMax.width;
        for (int i = 0; i < this.height; i++) {
            this.matrix[i] = inMax.getRow(i);
        }
    }

    /**
     * General method of adding two Matrices
     **/
    public static Jymax add(Jymax a, Jymax b) {
        if ((a.getH() == b.getH()) && (a.getW() == b.getW())) {
            Jymax out = new Jymax(a.getH(), a.getW());
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = out.getMat();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = m1[i][j] + m2[i][j];
                }
            }
            return out;
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of subtracting two Matrices
     **/
    public static Jymax sub(Jymax a, Jymax b) {
        if ((a.getH() == b.getH()) && (a.getW() == b.getW())) {
            Jymax out = new Jymax(a.getH(), a.getW());
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = out.getMat();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = m1[i][j] - m2[i][j];
                }
            }
            return out;
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

//    /**
//     * General method of adding a number to a matrix
//     **/
//    public static Jymax add(Jymax a, float b) {
//        Jymax out = new Jymax(a.getH(), a.getW());
//        float[][] m1 = a.getMat();
//        float[][] o = out.getMat();
//        for (int i = 0; i < out.getH(); i++) {
//            for (int j = 0; j < out.getW(); j++) {
//                o[i][j] = m1[i][j] + b;
//            }
//        }
//        return out;
//    }

    /**
     * General method of matrices multiplication
     * a @ b
     **/
    public static Jymax mul(Jymax a, Jymax b) {
        if (a.getW() == b.getH()) {
            Jymax out = new Jymax(a.getH(), b.getW());
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = out.getMat();
            int len = a.getW();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[i][s] * m2[s][j];
                    }
                }
            }
            return out;
        } else {
            throw new RuntimeException("First width should equal to second height");
        }
    }

    /**
     * General method of matrices multiplication
     * a @ b
     **/
    public static Jymax mul(Jymax a, float b) {
            Jymax out = new Jymax(a.getH(), a.getW());
            float[][] m1 = a.getMat();
            float[][] o = out.getMat();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = m1[i][j]*b;
                }
            }
            return out;
    }

    /**
     * General method of matrices multiplication
     * a.T @ b
     **/
    public static Jymax mulT(Jymax a, Jymax b) {
        if (a.getH() == b.getH()) {
            Jymax out = new Jymax(a.getW(), b.getW());
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = out.getMat();
            int len = a.getH();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[s][i] * m2[s][j];
                    }
                }
            }
            return out;
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of matrices multiplication
     * a @ b.T
     **/
    public static Jymax mulTT(Jymax a, Jymax b) {
        if (a.getW() == b.getW()) {
            Jymax out = new Jymax(a.getH(), b.getH());
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = out.getMat();
            int len = a.getW();
            for (int i = 0; i < out.getH(); i++) {
                for (int j = 0; j < out.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[i][s] * m2[j][s];
                    }
                }
            }
            return out;
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * Get a value at (i,j)
     **/
    public float get(int i, int j) {
        return this.matrix[i][j];
    }

    /**
     * Get a clone of row i
     **/
    public float[] getRow(int i) {
        return this.matrix[i].clone();
    }

    /**
     * Get a clone of column j
     **/
    public float[][] getCol(int j) {
        float[][] b = new float[this.height][1];
        for (int i = 0; i < this.height; i++) {
            b[i][0] = this.matrix[i][j];
        }
        return b;
    }

    /**
     * Return a reference of the matrix
     **/
    public float[][] getMat() {
        return this.matrix;
    }

    /**
     * Return height of the Matrix
     **/
    public int getH() {
        return this.height;
    }

    /**
     * Return width of the Matrix
     **/
    public int getW() {
        return this.width;
    }

    /**
     * Set Matrix equal to target
     **/
    public void set(Jymax t) {
        if ((this.height == t.getH()) && (this.width == t.getW())) {
            float[][] m2 = t.getMat();
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    this.matrix[i][j] = m2[i][j];
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of adding a Matrix to self
     **/
    public void add(Jymax b) {
        if ((this.height == b.getH()) && (this.width == b.getW())) {
            float[][] m2 = b.getMat();
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    this.matrix[i][j] += m2[i][j];
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of adding a Matrix to self with coefficient
     **/
    public void add(Jymax b, float coeff) {
        if ((this.height == b.getH()) && (this.width == b.getW())) {
            float[][] m2 = b.getMat();
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    this.matrix[i][j] += m2[i][j]*coeff;
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of subtracting a Matrix from self
     **/
    public void sub(Jymax b) {
        if ((this.height == b.getH()) && (this.width == b.getW())) {
            float[][] m2 = b.getMat();
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    this.matrix[i][j] -= m2[i][j];
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * General method of adding a number to self
     **/
    public void add(float b) {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                this.matrix[i][j] += b;
            }
        }
    }

    /**
     * Multiply current matrix by a factor
     * a * b
     **/
    public void eqmul(float b) {
        float[][] o = this.getMat();
        for (int i = 0; i < this.getH(); i++) {
            for (int j = 0; j < this.getW(); j++) {
                o[i][j] *= b;
            }
        }
    }

    /**
     * Set current matrix equal to the multiplication of two other matrices
     * a @ b
     **/
    public void eqmul(Jymax a, Jymax b) {
        if ((a.getW() == b.getH()) && (this.getH() == a.getH()) && (this.getW() == b.getW())) {
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = this.getMat();
            int len = a.getW();
            for (int i = 0; i < this.getH(); i++) {
                for (int j = 0; j < this.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[i][s] * m2[s][j];
                    }
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * Set current matrix equal to the multiplication of two other matrices
     * a.T @ b
     **/
    public void eqmulT(Jymax a, Jymax b) {
        if ((a.getH() == b.getH()) && (this.getH() == a.getW()) && (this.getW() == b.getW())) {
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = this.getMat();
            int len = a.getH();
            for (int i = 0; i < this.getH(); i++) {
                for (int j = 0; j < this.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[s][i] * m2[s][j];
                    }
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * Set current matrix equal to the multiplication of two other matrices
     * a @ b.T
     **/
    public void eqmulTT(Jymax a, Jymax b) {
        if ((a.getW() == b.getW()) && (this.getH() == a.getH()) && (this.getW() == b.getH())) {
            float[][] m1 = a.getMat();
            float[][] m2 = b.getMat();
            float[][] o = this.getMat();
            int len = a.getH();
            for (int i = 0; i < this.getH(); i++) {
                for (int j = 0; j < this.getW(); j++) {
                    o[i][j] = 0;
                    for (int s = 0; s < len; s++) {
                        o[i][j] += m1[i][s] * m2[j][s];
                    }
                }
            }
        } else {
            throw new RuntimeException("Sizes don't match");
        }
    }

    /**
     * multiplication with activation function. For NN hidden layer only
     * dkernel(a @ b.T).  Note dkernel need to take the kernel's output as its input
     **/
    public void eqmulTT(Jymax a, Jymax b, JyAct act) {
        float[][] m1 = a.getMat();
        float[][] m2 = b.getMat();
        float[][] o = this.getMat();
        int len = a.getH();
        float temp;
        for (int i = 0; i < this.getH(); i++) {
            for (int j = 0; j < this.getW(); j++) {
                temp = 0;
                for (int s = 0; s < len; s++) {
                    temp += m1[i][s] * m2[j][s];
                }
                o[i][j] = act.dk(o[i][j])*temp;
            }
        }
    }

    /**
     * multiplication with activation function. For NN hidden layer only
     * kernel(a @ b + c)
     **/
    public void eqmul(Jymax a, Jymax b, Jymax c, JyAct act) {
        float[][] m1 = a.getMat();
        float[][] m2 = b.getMat();
        float[][] m3 = c.getMat();
        float[][] o = this.getMat();
        int len = a.getW();
        for (int i = 0; i < a.getH(); i++) {
            for (int j = 0; j < b.getW(); j++) {
                o[i][j] = 0;
                for (int s = 0; s < len; s++) {
                    o[i][j] += m1[i][s] * m2[s][j];
                }
                o[i][j] += m3[0][j];
                o[i][j] = act.k(o[i][j]);
            }
        }
    }



    /**
     * Overload print func
     **/
    public String toString() {
        StringBuilder out = new StringBuilder(this.height * (this.width * 12 + 1) + 1);
        out.append("[");
        for (int i = 0; i < this.height; i++) {
            out.append("[");
            for (int j = 0; j < this.width; j++) {
                out.append(String.format("%.3e", this.matrix[i][j]));
                if (j < this.width - 1) out.append(", ");
            }
            out.append("]");
            if (i < this.height - 1) out.append("\n");
        }
        out.append("]");
        return out.toString();
    }

}
