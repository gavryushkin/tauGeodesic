/*
 * Copyright (C) 2014 Alex Gavryushkin <alex@gavruskin.com> and Alexei Drummond
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class can be used to compute the geodesic.
 *
 * Created on 17/12/14.
 * @author Alex Gavryushkin <alex@gavruskin.com>
 * @author Alexei Drummond
 */
public class GeoPath {
    public static void main(String[] args) throws Exception {
        NexusParser parser = new NexusParser();
        File treeFile = new File(args[0]);
        parser.parseFile(treeFile);
        List<Tree> trees = parser.trees;

        Tree[] treesArray = new Tree[trees.size()];
        for (int i = 0; i < trees.size(); i++) {
            treesArray[i] = trees.get(i);
        }

        // Converting the trees to tau-trees:
        TauTree[] tauTrees = new TauTree[trees.size()];
        int numberOfTreesPassed = 0;
        for (int i = 0; i < treesArray.length; i++) {
            tauTrees[i] = new TauTree(treesArray[i]);
            numberOfTreesPassed=i+1;
        }
        if (numberOfTreesPassed != 2) {
            System.out.println("The file has to contain exactly two trees. Please choose another file.");
            System.exit(0);
        }
        ArrayList<String> taxonOrder = new ArrayList<>();
        for (Node node:treesArray[1].getExternalNodes()) {
            taxonOrder.add(node.getID());
        }
        TauTree tree1 = tauTrees[0];
        TauTree tree2 = tauTrees[1];
        Scanner keyboard = new Scanner(System.in);
        System.out.println("How many trees to output along the geodesic?");
        int NumSteps = keyboard.nextInt();
        boolean printOriginBranch=false;
        System.out.println("Would you like to add origin branch to make all trees of the same height (maximum height)? Y/N");
        String originBranch = keyboard.next();
        if (originBranch.equals("Y")) {
            printOriginBranch = true;
        }
        Tree[] geodesic = new Tree[NumSteps];
        TauTree[] tauGeodesic = new TauTree[NumSteps];
        for (int i = 0; i < NumSteps; i++) {
            double t = (double) i/(NumSteps-1);
            tauGeodesic[i] = Geodesic.geodesic(tree1, tree2, t).tree;
            tauGeodesic[i].firstTau = (1-t)*tree1.getFirstTau() + t*tree2.getFirstTau();
            tauGeodesic[i].numTaxa = tree1.numTaxa;
            tauGeodesic[i].labelMap = tree1.labelMap;
        }

        String outputFile = "";
        if (args.length > 1) {
            outputFile = args[1];
        }

        if (outputFile.isEmpty()) {
            String inputFileName = treeFile.getPath();
            if (inputFileName.contains(".trees")) {
                inputFileName = inputFileName.substring(0, inputFileName.indexOf(".trees"));
            }
            outputFile = inputFileName +"_geodesics.trees";
        }

        PrintStream writer = null;
        try {
            writer = new PrintStream(new File(outputFile));
//            String newLine = System.getProperty("line.separator");
//            writer.println("#NEXUS" + newLine + newLine + "Begin taxa;");
//            writer.println("    Dimensions ntax=" + tree1.numTaxa + ";");
//            writer.println("        Taxlabels");
//            for (int i = 0; i < tree1.numTaxa; i++) {
//                writer.println("            a"+i);
//            }
//            writer.println(";" + newLine + "End;" + newLine + "Begin trees;" + newLine + "  Translate");
//            for (int i = 0; i < tree1.numTaxa - 1; i++) {
//                writer.println("        " + i + " a" + i + ",");
//            }
//            int k = tree1.numTaxa - 1;
//            writer.println("        " + k + " a" + k + newLine + ";");
//            writer.println(newLine + "Begin trees;" + newLine);
            Tree Origin = TauTree.constructFromTauTree(tree1);
            Origin.changeOrderOfTaxa(Origin.getRoot(), taxonOrder);
            Origin.init(writer);
            Tree Destination = TauTree.constructFromTauTree(tree2);
            Destination.changeOrderOfTaxa(Destination.getRoot(), taxonOrder);
            writer.println("Tree Origin = " + Origin.getRoot().toNewick() + ";");
            writer.println("Tree Destination = " + Destination.getRoot().toNewick() + ";");
            double maxHeight = 0.0;
            for (int i = 0; i < tauGeodesic.length; i++) {
                geodesic[i] = TauTree.constructFromTauTree(tauGeodesic[i]);
                if (geodesic[i].getRoot().getHeight() > maxHeight) {
                    maxHeight = geodesic[i].getRoot().getHeight();
                }

            }
            for (int i = 0; i < tauGeodesic.length; i++) {
                geodesic[i].changeOrderOfTaxa(geodesic[i].getRoot(), taxonOrder);
                if (printOriginBranch) {
                    writer.println("Tree " + i + " = " + geodesic[i].getRoot().toNewickWithRootBranch(maxHeight)+";");
                } else {
                    writer.println("Tree " + i + " = " + geodesic[i].getRoot().toNewick()+";");
                }
            }
            writer.println("End;");
            System.out.println("See " + outputFile);
        }
        catch (IOException e) {
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
