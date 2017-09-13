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

public class Sij {

    static String wd = new File(System.getProperty("user.dir")) + File.separator;  //Working Directory

    public static void main(String[] args) throws FileNotFoundException {

        //Load data: Inputs mi and dij

        //Number of regions n
        int n = 0;
        Scanner scan = new Scanner(new File(wd + "Inputs.csv"));
        scan.nextLine();
        while (scan.hasNextLine()) {
            String[] cols = scan.nextLine().split(";");
            n++;
        }

        //Inputs
        int[] mj = new int[n];  //Number of inhabitants at destination (mj)
        scan = new Scanner(new File(wd + "Inputs.csv"));
        scan.nextLine();
        int k = 0;
        while (scan.hasNextLine()) {
            String[] cols = scan.nextLine().split(";");
            mj[k] = Integer.parseInt(cols[1]);
            k++;
        }

        //Distance matrix dij (size n x n)
        double[][] dij = new double[n][n];
        scan = new Scanner(new File(wd + "Distance.csv"));
        scan.nextLine();
        k = 0;
        while (scan.hasNextLine()) {
            String[] cols = scan.nextLine().split(";");
            for (int i = 0; i < cols.length; i++) {
                cols[i] = cols[i].replace(',', '.');
                dij[k][i] = Double.parseDouble(cols[i]);
            }
            k++;
        }

        //Compute the matrix of opportunities sij: Number of opportunities located in a circle of radius dij centered in i
        //(excuding the source and the destination)
        int[][] S = new int[n][n];   //sij
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    S[i][j] = 0;
                } else {
                    int r = 0;
                    for (int l = 0; l < n; l++) {
                        if ((dij[i][l] <= dij[i][j]) && (i != l) && (l != j)) {
                            r = r + mj[l];
                        }
                    }
                    S[i][j] = r;
                }
            }
        }

        //Write the resulting matrix in a file
        try (PrintWriter writer = new PrintWriter(new File(wd + "sij.csv"))) {
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
        }
    }
}
