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

public class GOF {

    static String wd = new File(System.getProperty("user.dir")) + File.separator;  //Working Directory

    public static void main(String[] args) throws FileNotFoundException {

        //Parameters: law, model, beta and repli
        Scanner scan = new Scanner(new File(wd + "Parameters.csv"));
        scan.nextLine();
        String[] cols = scan.nextLine().split(";");
        double repli = Integer.parseInt(cols[3]);

        //Load data: Tij and dij

        //Number of regions n
        int n = 0;
        scan = new Scanner(new File(wd + "Inputs.csv"));
        scan.nextLine();
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            n++;
        }

        //Observed OD matrix Tij (size n x n)
        double[][] Tij = new double[n][n];
        scan = new Scanner(new File(wd + "OD.csv"));
        scan.nextLine();
        int k = 0;
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            for (int i = 0; i < cols.length; i++) {
                cols[i] = cols[i].replace(',', '.');
                Tij[k][i] = Double.parseDouble(cols[i]);
            }
            k++;
        }

        //Distance matrix dij (size n x n)
        double maxd = 0.0;
        double[][] dij = new double[n][n];
        scan = new Scanner(new File(wd + "Distance.csv"));
        scan.nextLine();
        k = 0;
        while (scan.hasNextLine()) {
            cols = scan.nextLine().split(";");
            for (int i = 0; i < cols.length; i++) {
                cols[i] = cols[i].replace(',', '.');
                dij[k][i] = Double.parseDouble(cols[i]);
                maxd = Math.max(maxd, dij[k][i]);
            }
            k++;
        }

        //Writer
        PrintWriter writer = new PrintWriter(new File("GOF.csv"));

        //Loop replications
        for (int r = 0; r < repli; r++) {

            //Load simulated OD
            double[][] S = new double[n][n];
            scan = new Scanner(new File("S_" + (r + 1) + ".csv"));
            scan.nextLine();
            k = 0;
            while (scan.hasNextLine()) {
                cols = scan.nextLine().split(";");
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].replace(',', '.');
                    S[k][i] = Double.parseDouble(cols[i]);
                }
                k++;
            }

            //Indicators used to compute CPC, CPL and CPCd
            double nb = 0.0;     //Number of commuters
            double cpc = 0.0;    //Number of commuters in common
            double nbNL = 0.0;   //Number of new links
            double nbCL = 0.0;   //Number of common links
            double nbML = 0.0;   //Number of missing links

            int lenCDD = ((int) Math.floor(maxd / 2)) + 1;   //Length of the histogram for the Commuting Distance Distribution (CDD)
            double[] CDD_R = new double[lenCDD];             //Observed CDD           
            double[] CDD_S = new double[lenCDD];             //Simulated CDD

            //Loop over rows and columns to fill the indicators
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    nb += Tij[i][j];
                    if (Tij[i][j] == 0 && S[i][j] != 0) {
                        nbNL++;
                    }
                    if (Tij[i][j] != 0 && S[i][j] == 0) {
                        nbML++;
                    }
                    if (Tij[i][j] != 0 && S[i][j] != 0) {
                        nbCL++;
                        cpc += Math.min(S[i][j], Tij[i][j]);
                    }

                    int indice = (int) Math.floor(dij[i][j] / 2);
                    CDD_R[indice] += Tij[i][j];
                    CDD_S[indice] += S[i][j];
                }
            }

            //CPC
            cpc = cpc / nb;

            //CPL
            double cpl = 2 * nbCL / (nbNL + 2 * nbCL + nbML);

            //CPCd
            double cpcd = 0.0;
            for (int i = 0; i < CDD_S.length; i++) {
                cpcd += Math.abs(CDD_S[i] - CDD_R[i]) / nb;
            }
            cpcd = 1 - 0.5 * cpcd;

            //Write the results
            writer.print(r);
            writer.print(";");
            writer.print(cpc);
            writer.print(";");
            writer.print(cpl);
            writer.print(";");
            writer.print(cpcd);
            writer.println();

            System.out.print(r);
            System.out.print(" ");
            System.out.print(cpc);
            System.out.print(" ");
            System.out.print(cpl);
            System.out.print(" ");
            System.out.print(cpcd);
            System.out.println();

        }
        writer.close();
    }
}
