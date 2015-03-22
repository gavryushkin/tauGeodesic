/*
 * Copyright (C) 2014 Alex Gavryushkin <alex@gavruskin.com>
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

import beast.evolution.tree.Tree;
import beast.util.NexusParser;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Parsing the file, calling mean, returning the mean.
 *
 * Created on 14/11/14.
 * @author Alex Gavryushkin <alex@gavruskin.com>
 */
public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            NexusParser parser = new NexusParser();
            File treeFile = new File(args[0]);
            parser.parseFile(treeFile);
            List<Tree> trees = parser.trees;

            // Putting trees into an array to save 2 seconds per tree when converting them to tau-trees:
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

            System.out.println("The number of trees passed is " + numberOfTreesPassed + ".\n");
            Date date = new Date();
            System.out.println("The tau-trees have been created on " + date + ". Start computing the mean...\n");

            TauTree mean = Mean.mean(tauTrees, 10000000, 100, 0.0001);

            mean.labelMap = tauTrees[0].labelMap;

            Tree meanBeast = TauTree.constructFromTauTree(mean);

            System.out.print("\n");
            System.out.println("Mean tree is\n");
            System.out.println(meanBeast.getRoot().toNewick());
            System.out.print("\n");

            // Return distances to the mean:
            //double[] dist2mean = new double[tauTrees.length];
            //for (int i = 0; i < tauTrees.length; i++) {
            //    dist2mean[i] = Geodesic.geodesic(mean,tauTrees[i],0.5).geoLength;
            //    System.out.println(dist2mean[i]);
            //}

            //Return tree closest to mean:
            double distance2mean = Geodesic.geodesic(mean,tauTrees[0],0.5).geoLength;
            int closestTreeIndex = 0;
            double stdDeviation = distance2mean*distance2mean;
            for (int i = 1; i < tauTrees.length; i++) {
                double distCurrent = Geodesic.geodesic(mean,tauTrees[i],0.5).geoLength;
                stdDeviation = stdDeviation + distCurrent*distCurrent;
                if (distCurrent < distance2mean) {
                    distance2mean = Geodesic.geodesic(mean,tauTrees[i],0.5).geoLength;
                    closestTreeIndex = i;
                }
            }
            stdDeviation = Math.sqrt(stdDeviation/(tauTrees.length - 1));
            TauTree closestTree = tauTrees[closestTreeIndex];
            Tree closestTreeBeast = TauTree.constructFromTauTree(closestTree);
            System.out.println("The closes tree in the sample to the mean-tree is\n");
            System.out.println(closestTreeBeast.getRoot().toNewick());
            System.out.print("\n");
            System.out.println("Distance to mean is " + distance2mean + ".\n");
            System.out.println("Standard deviation is " + stdDeviation + ".\n");
        }
    }
}