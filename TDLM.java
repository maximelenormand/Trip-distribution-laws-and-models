/*
 *  Author: Maxime Lenormand (2015)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class TDLM {

    static String wd = new File(System.getProperty("user.dir")) + File.separator;  //Working Directory

    public static void main(String[] args) throws FileNotFoundException {

        //Parameters: law, model, beta, repli and writepij
        Scanner scan = new Scanner(new File(wd + "Parameters.csv"));
        scan.nextLine();
        String[] cols = scan.nextLine().split(";");
        String law = cols[0];
        String model = cols[1];
        cols[2] = cols[2].replace(',', '.');
        double beta = Double.parseDouble(cols[2]);
        double repli = Integer.parseInt(cols[3]);
        boolean writepij = Boolean.parseBoolean(cols[4]);

        //Check if the law and the model are defined
        if (!(law.equals("GravExp") || law.equals("NGravExp") || law.equals("GravPow") || law.equals("NGravPow") || law.equals("Schneider") || law.equals("Rad") || law.equals("RadExt") || law.equals("Rand"))) {
            System.out.print("The law ");
            System.out.print(law);
            System.out.println(" is not defined");
            return;
        }

        if (!(model.equals("UM") || model.equals("PCM") || model.equals("ACM") || model.equals("DCM"))) {
            System.out.print("The model ");
            System.out.print(model);
            System.out.println(" is not defined");
            return;
        }

        //Load data: Inputs (mi, mj, Oi and Dj), dij and sij       
        //Number of regions n
        int n = 0;
        scan = new Scanner(new File(wd + "Inputs.csv"));
        scan.nextLine();
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            n++;
        }

        //Inputs
        int[] mi = new int[n];  //Number of inhabitants at origin (mi)
        int[] mj = new int[n];  //Number of inhabitants at destination (mj)
        int[] Oi = new int[n];  //Number of out-commuters (Oi) 
        int[] Dj = new int[n];  //Number of in-commuters (Dj)
        scan = new Scanner(new File(wd + "Inputs.csv"));
        scan.nextLine();
        int k = 0;
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            mi[k] = Integer.parseInt(cols[0]);
            mj[k] = Integer.parseInt(cols[1]);
            Oi[k] = Integer.parseInt(cols[2]);
            Dj[k] = Integer.parseInt(cols[3]);
            k++;
        }

        //Distance matrix dij (size n x n)
        double[][] dij = new double[n][n];
        scan = new Scanner(new File(wd + "Distance.csv"));
        scan.nextLine();
        k = 0;
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            for (int i = 0; i < cols.length; i++) {
                cols[i] = cols[i].replace(',', '.');
                dij[k][i] = Double.parseDouble(cols[i]);
            }
            k++;
        }

        //Matrix of opportunities sij matrix (size n x n) [only for the intervening opportunities laws]
        double[][] sij = new double[n][n];
        if (law.equals("Rad") || law.equals("RadExt") || law.equals("Schneider")) {
            scan = new Scanner(new File(wd + "Sij.csv"));
            scan.nextLine();
            k = 0;
            while (scan.hasNextLine()) {
                cols = scan.nextLine().split(";");
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].replace(',', '.');
                    sij[k][i] = Double.parseDouble(cols[i]);
                }
                k++;
            }
        }

        System.out.println("Data loaded");

        //Build the matrix pij according to the law
        double[][] pij = proba(law, dij, sij, mi, mj, beta);

        //Write pij if needed
        if (writepij) {
            //Sum pij for normalization
            double sumpij = 0.0;
            for (int i = 0; i < pij.length; i++) {
                for (int j = 0; j < pij.length; j++) {
                    sumpij += pij[i][j];
                }
            }
            //Write
            PrintWriter writerpij = new PrintWriter(new File("pij.csv"));
            for (int j = 0; j < pij.length; j++) {
                writerpij.print("V" + (j + 1));
                writerpij.print(";");
            }
            writerpij.println();
            for (int i = 0; i < pij.length; i++) {
                for (int j = 0; j < pij.length; j++) {
                    writerpij.print(pij[i][j] / sumpij);
                    writerpij.print(";");
                }
                writerpij.println();
            }
        }

        //Loop replications
        for (int r = 0; r < repli; r++) {

            System.out.println("Replication " + (r + 1));

            //Simulated OD
            double[][] S = new double[n][n];

            //Network generation according to the constrained model 
            if (model.equals("UM")) {   //Unconstrained model
                S = UM(pij, Oi);
            }
            if (model.equals("PCM")) {  //Production cconstrained model
                S = PCM(pij, Oi);
            }
            if (model.equals("ACM")) {  //Attraction constrained model
                S = ACM(pij, Dj);
            }
            if (model.equals("DCM")) {  //Doubly constrained model
                S = DCM(pij, Oi, Dj, 50, 0.01);
            }

            //Write the resulting simulated OD matrix in a file 
            PrintWriter writer = new PrintWriter(new File("S_" + (r + 1) + ".csv"));
            for (int j = 0; j < S.length; j++) {
                writer.print("V" + (j + 1));
                writer.print(";");
            }
            writer.println();
            for (int i = 0; i < S.length; i++) {
                for (int j = 0; j < S.length; j++) {
                    writer.print(S[i][j]);
                    writer.print(";");
                }
                writer.println();
            }
            writer.close();
        }
    }

    //proba: generate the matrix pij according to the law (GravExp, GravPow, NGravExp, NGravPow, Schneider, Rad, RadExt and Rand)
    //inputs: law, mi, mj and beta
    static double[][] proba(String law, double[][] dij, double[][] sij, int[] mi, int[] mj, double beta) {

        int n = mi.length;                 //Number of regions
        double[][] W = new double[n][n];   //Output

        //Gravity law with an exponential decay function
        if (law.equals("GravExp")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((double) mi[i]) * ((double) mj[j]) * Math.exp((dij[i][j]) * (-beta));
                    } else {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Normalized gravity law with an exponential decay function
        if (law.equals("NGravExp")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((double) mj[j]) * Math.exp((dij[i][j]) * (-beta));
                    } else {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Gravity law with a power decay function
        if (law.equals("GravPow")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((double) mi[i]) * ((double) mj[j]) * Math.pow(dij[i][j], (-beta));
                    } else {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Normalized gravity law with a power decay function
        if (law.equals("NGravPow")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((double) mj[j]) * Math.pow(dij[i][j], (-beta));
                    } else {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Schneider's intervening opportunities law
        if (law.equals("Schneider")) {
            for (int i = 0; i < W.length; i++) {
                for (int j = 0; j < W.length; j++) {
                    if (i != j) {
                        W[i][j] = (Math.exp(-beta * sij[i][j]) - Math.exp(-beta * (sij[i][j] + ((double) mj[j]))));
                    } else {
                        W[i][j] = 0;
                    }
                    if (Double.isNaN(W[i][j])) {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Radiation law
        if (law.equals("Rad")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((double) mj[i]) * ((double) mj[j]) / ((((double) mj[i]) + sij[i][j]) * (((double) mj[i]) + ((double) mj[j]) + sij[i][j]));
                    } else {
                        W[i][j] = 0;
                    }
                    if (Double.isNaN(W[i][j])) {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Extended radiation law
        if (law.equals("RadExt")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        W[i][j] = ((Math.pow(sij[i][j] + ((double) mj[i]) + ((double) mj[j]), beta) - Math.pow(sij[i][j] + ((double) mj[i]), beta)) * (Math.pow(((double) mj[i]), beta) + 1)) / ((Math.pow(sij[i][j] + ((double) mj[i]) + ((double) mj[j]), beta) + 1) * (Math.pow(sij[i][j] + ((double) mj[i]), beta) + 1));
                    } else {
                        W[i][j] = 0;
                    }
                    if (Double.isNaN(W[i][j])) {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Uniform law
        if (law.equals("Rand")) {
            for (int i = 0; i < W.length; i++) {
                for (int j = 0; j < W.length; j++) {
                    if (i != j) {
                        W[i][j] = 1.0 / (((double) W.length) * ((double) W.length) - ((double) W.length));
                    } else {
                        W[i][j] = 0;
                    }
                }
            }
        }

        //Row normalization if needed
        if (!(law.equals("GravExp") || law.equals("GravPow") || law.equals("Rand"))) {
            double[] Wi = new double[n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Wi[i] += W[i][j];
                }
            }
            for (int i = 0; i < n; i++) {
                if (Wi[i] != 0) {
                    for (int j = 0; j < n; j++) {
                        if (i != j) {
                            W[i][j] = ((double) mi[i]) * W[i][j] / Wi[i];
                        } else {
                            W[i][j] = 0;
                        }
                    }
                }
            }
        }

        return W;
    }

    //UM: generate the network using the Unconstrained Model
    //inputs: pij, Oi
    static double[][] UM(double[][] pij, int[] Oi) {

        int n = Oi.length;  //Number of Units

        //Number of commuters
        int nbCommuters = 0;
        for (int i = 0; i < n; i++) {
            nbCommuters += Oi[i];
        }

        //Sum pij for the normalization
        double sumt = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sumt += pij[i][j];
            }
        }

        //Sum pij by row for the Row normalization (see Multinomial_ij)
        double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += pij[i][j];
            }
        }

        //NbCommuters are sampled from pij
        int nb = 0;
        double[][] S = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                S[i][j] = Math.floor(nbCommuters * pij[i][j] / sumt);
                nb += S[i][j];
            }
        }
        int[][] index = Multinomial_ij(nbCommuters - nb, pij, sum);
        for (int k = 0; k < index.length; k++) {
            S[index[k][0]][index[k][1]]++;
        }

        return S;
    }

    //PCM: generate the network using the Production Constrained Model
    //inputs: pij, Oi
    static double[][] PCM(double[][] pij, int[] Oi) {

        int n = Oi.length; //Number of regions

        double[][] S = new double[n][n];

        //Sum pij for the normalization
        double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += pij[i][j];
            }
        }

        //NbCommuters are sampled from pij preserving Oi
        int[] nb = new int[n];
        for (int i = 0; i < n; i++) {
            if (sum[i] > 0) {
                for (int j = 0; j < n; j++) {
                    S[i][j] = Math.floor(Oi[i] * pij[i][j] / sum[i]);
                    nb[i] += S[i][j];
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (Oi[i] != 0) {
                int[] index = Multinomial_i(Oi[i] - nb[i], pij[i], sum[i]);
                for (int k = 0; k < index.length; k++) {
                    S[i][index[k]]++;
                }
            }
        }
        return S;
    }

    //ACM: generate the network using the Attraction Constrained Model
    //inputs: pij, Dj
    static double[][] ACM(double[][] pij, int[] Dj) {

        int n = Dj.length; //Number of regions

        double[][] S = new double[n][n];

        //Transpose of pij
        double[][] tweights = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tweights[i][j] = pij[j][i];
            }
        }

        //Sum pij for the normalization
        double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += tweights[i][j];
            }
        }

        //NbCommuters are sampled from pij preserving Dj
        int[] nb = new int[n];
        for (int i = 0; i < n; i++) {
            if (sum[i] > 0) {
                for (int j = 0; j < n; j++) {
                    S[j][i] = (int) Math.floor(Dj[i] * tweights[i][j] / sum[i]);
                    nb[i] += S[j][i];
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (Dj[i] != 0) {
                int[] index = Multinomial_i(Dj[i] - nb[i], tweights[i], sum[i]);
                for (int k = 0; k < index.length; k++) {
                    S[index[k]][i]++;
                }
            }
        }

        return S;
    }

    //DCM: generate the network using the Doubly Constrained Model
    //inputs: pij, Oi, Dj, maxIter (maximal number of iterations for the IPF procedure), closure (stopping criterion)
    static double[][] DCM(double[][] pij, int[] Oi, int[] Dj, int maxIter, double closure) {

        int n = Oi.length; //Number of Units

        //Iterative Proportional Fitting procedure (IPF)
        //IPF is a procedure for adjusting a table of data cells such that they add up to selected totals for both 
        //the columns and rows (in the two-dimensional case) of the table. 
        double[][] marg = new double[n][2];   //Observed marginals
        for (int i = 0; i < n; i++) {
            marg[i][0] = Oi[i];         //Observed marginal row       
            marg[i][1] = Dj[i];         //Observed marginal column 
            if (marg[i][0] == 0) {      //Only non-zero values
                marg[i][0] = 0.01;
            }
            if (marg[i][1] == 0) {      //Only non-zero values are admited
                marg[i][1] = 0.01;
            }
        }

        double[][] weights = new double[n][n];   //Seed of the IPF based on pij
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weights[i][j] = pij[i][j];
                if (weights[i][j] == 0) {    //Only non-zero values are admited
                    weights[i][j] = 0.01;
                }
            }
        }

        int iter = 0;                   //Number of iterations
        double critOut = 1.0;           //Distance between observed and simulated marginal rows 
        double critIn = 1.0;            //Distance between observed and simulated marginal columns 
        double[] sout = new double[n];  //Simulated marginal rows 
        double[] sin = new double[n];   //Simulated marginal columns 

        //Repeat the process while iter lower than maxIter and the distances between observed and simulated marginal 
        //are above a threshold 
        while ((critOut > closure || critIn > closure) && (iter <= maxIter)) {

            //Compute sout
            for (int i = 0; i < n; i++) {
                sout[i] = 0;
                for (int k = 0; k < n; k++) {
                    sout[i] += weights[i][k];
                }
            }
            //Each row of weights is proportionally adjusted to equal the observed marginal row (specifically, each cell is divided 
            //by the simulated marginal row, then multiplied by the observed marginal row). 
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    weights[i][j] = marg[i][0] * weights[i][j] / sout[i];
                }
            }

            //Compute sin
            for (int i = 0; i < n; i++) {
                sin[i] = 0;
                for (int k = 0; k < n; k++) {
                    sin[i] += weights[k][i];
                }
            }
            //Each column of weights is proportionally adjusted to equal the observed marginal column (specifically, each cell is divided 
            //by the simulated marginal column, then multiplied by the observed marginal column).
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    weights[i][j] = marg[j][1] * weights[i][j] / sin[j];
                }
            }

            //Compute the distances between observed and simulated marginals
            critOut = 0.0;
            critIn = 0.0;
            for (int i = 0; i < n; i++) {
                sout[i] = 0;
                sin[i] = 0;
                for (int k = 0; k < n; k++) {
                    sout[i] += weights[i][k];
                    sin[i] += weights[k][i];
                }
                critOut = Math.max(critOut, Math.abs(1 - (sout[i] / marg[i][0])));
                critIn = Math.max(critIn, Math.abs(1 - (sin[i] / marg[i][1])));
            }
            iter++;
        }

        //NbCommuters are sampled from weights
        double[][] S = UM(weights, Oi);

        return S;
    }

    //Multinomial_i: Given a vector of weights , it returns N indices randomly sampled according to these weights.
    //weights is a vector of weights
    //sum is the sum of weights (for the normalization)
    static int[] Multinomial_i(int n, double[] weights, double sum) {
        int[] randomIndex = new int[n];
        double[] random = new double[n];
        for (int k = 0; k < n; k++) {
            random[k] = Math.random() * sum;
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < weights.length; i++) {
                random[k] -= weights[i];
                if (random[k] <= 0.0) {
                    randomIndex[k] = i;
                    break;
                }
            }
        }
        return randomIndex;
    }

    //Multinomial_ij: Given a matrix of weights, it returns N 2D indices randomly sampled according to these weights.
    //weights is a matrix of weights
    //sum is vector of the sum by row (ie row marginal of the matrix)
    static int[][] Multinomial_ij(int n, double[][] weights, double[] sum) {

        int[][] randomIndex = new int[n][2];

        double sumt = 0.0;
        for (int k = 0; k < sum.length; k++) {
            sumt += sum[k];
        }

        double[] random = new double[n];
        double[] randomi = new double[n];
        for (int k = 0; k < n; k++) {
            random[k] = Math.random() * sumt;
            randomi[k] = random[k];
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < sum.length; i++) {
                randomi[k] -= sum[i];
                random[k] -= sum[i];
                if (randomi[k] <= 0.0) {
                    random[k] += sum[i];
                    randomIndex[k][0] = i;
                    break;
                }
            }
            for (int j = 0; j < weights.length; j++) {
                random[k] -= weights[randomIndex[k][0]][j];
                if (random[k] <= 0.0) {
                    randomIndex[k][1] = j;
                    break;
                }
            }
        }
        return randomIndex;
    }
}
