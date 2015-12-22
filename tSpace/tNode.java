package tTree;
/*
 * Node class, contains an arraylist of taxa included and a double height;
 * 
 * To be added/changed:
 * -- case1 need to handle a second call to case1 properly (by sorting speeds of 2+ cherry, using speedTree)
 * -- perhaps make speedTree a method since same thing done in case0 and case1;
 * --
*/
import java.util.*;
import java.io.*;
//Consider making the time of tree another part of tNode;
public class tNode {
	ArrayList<String> taxa;
	double height;
	
	public tNode(ArrayList<String> taxa, double height){
		this.taxa = taxa;
		this.height = height;		
	}
	
	public void setHeight(double height){
		this.height = height;
	}
	public double getHeight(){
		return height;
	}
	public void setTaxa(ArrayList<String> taxa){
		this.taxa = taxa;
	}
	public ArrayList<String> getTaxa(){
		return taxa;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(taxa);
		sb.append(height);
		return sb.toString();
	}
	
//The following is for cat0 trees, but hopefully soon cat2 aswell.
	//each taxon has it's own node so they can move more independently than before
	//Also they move at constant speed so almost completely independent of end point (besides direction of movement)
	//direction only changes in cat2+'s at root intersection.
	//should take a double as speed
	public static void getTreesAndPath(String[] t1Taxa, double[] t1Height, String[] t2Taxa, double[] t2Height){
		ArrayList<tNode> tree1 = generateCat1(t1Taxa, t1Height); //trees of independent nodes --> 
		ArrayList<tNode> tree2 = generateCat1(t2Taxa, t2Height);
		//findDirections -->	
		ArrayList<Double> timeDistance = new ArrayList<Double>(); //distance  = 1st element, rest are times
		double totalDistance = 0.0;
		ArrayList<String> trees = new ArrayList<String>();
		ArrayList<tNode> treeB = tree1;
		trees= getPath(tree1, tree2,treeB, timeDistance, totalDistance, trees);	
		System.out.println("Times: " + timeDistance);
		System.out.println(trees);
		String[] treesArray = new String[trees.size()];
		for(int i = 0; i<trees.size(); i++){
			treesArray[i] = trees.get(i);
		}
		printToFile(treesArray, "trees.txt");
	}
	
	public static ArrayList<tNode> generateCat1(String[] taxa, double[] height){
		ArrayList<tNode> tree = new ArrayList<tNode>();		
		for(int i = 0; i<taxa.length; i++){
			ArrayList<String> nodeTaxa = new ArrayList<String>();
			nodeTaxa.add(taxa[i]);
			tree.add(new tNode(nodeTaxa, height[i]));
		}	
		return tree;
	}
	//set everything to max speed --> either everything reaches it's destination or it intersects somewhere.
	public static ArrayList<tNode> findDirections(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		ArrayList<tNode> directions = new ArrayList<tNode>();
		double max = 0.0;
		for(tNode n1 : tree1){
			String t1taxa = n1.getTaxa().get(0);
			ArrayList<String> taxa = new ArrayList<String>();
			taxa.add(t1taxa);
			for(tNode n2 : tree2){
				String t2taxa = n2.getTaxa().get(0);
				if(t1taxa.compareTo(t2taxa) == 0){
					double height = (n2.getHeight() - n1.getHeight());					
					if(Math.abs(height) > max){
						max = Math.abs(height);
					}
					if(height != 0){
						height = height/Math.abs(height); //results in either 1 or -1.
					}
					directions.add(new tNode(taxa, height));					
				}
			}		
		}
		//multiply each direction by maxspeed;	
		for(tNode n : directions){
			n.setHeight(n.getHeight()*max);
		}
		
		return directions;
	}
	//timeDistance is a map of the time and distance up to that time. As every time should be different it maps to the distance
	//add directions to tree1 to get a new tree;
	//check to see if there have been any topology changes --> document/restart from tree at that change;
	//check to see if any nodes have stopped/should have stopped --> 
	public static ArrayList<String> getPath(ArrayList<tNode> tree1, ArrayList<tNode> tree2, ArrayList<tNode> treeB,
			ArrayList<Double> timeDistance, double totalDistance, ArrayList<String> trees){
		//will any nodes stop?  --> adjust directions. every time --don't need it as a parameter
		//comparing tree1 + directions to tree2
		//if 2.0 --> no stop, else --> stop
		mergeSort(tree1);
		System.out.println("Tree1: " + tree1);	
		System.out.println("TreeB: " + treeB);
		System.out.println("Tree2: " + tree2);
		trees.add(convertToNewick(tree1));
		//baseCase:
		if(treesAreEqual(tree1, tree2)){
			System.out.println("Total Distance:  " + totalDistance);
			System.out.println(timeDistance);
			return trees;
		}			
		ArrayList<tNode> treeNext = new ArrayList<tNode>();	
		ArrayList<tNode> directions = findDirections(treeB, tree2);  //get arrayList (sorted by tree1); 	
		//create treeNext		
		for(int i = 0; i < treeB.size(); i++){
			double height = treeB.get(i).getHeight() + directions.get(i).getHeight();
			treeNext.add(new tNode(treeB.get(i).getTaxa(),height));
		}
		mergeSort(treeNext);
		//compare to tree2 to see if any nodes have stopped
		double minTimeNodeStop = 10.0;//adjust
		if(findNodeStop(treeB, tree2, directions) != 10.0){
			minTimeNodeStop = findNodeStop(treeB, tree2, directions);
		}
		double minTimeTopologyAlteration = 10.0;
		//check for topology changes
		if(findTopologyAlterations(treeB, treeNext) != 10.0){
			//find it/them...; --> just want lowest;
			minTimeTopologyAlteration = findTopologyAlterations(treeB, treeNext);
			// adjust treeNext to account for this.						
		}
		double minTime = Math.min(minTimeNodeStop, minTimeTopologyAlteration);
		System.out.println("minTime: "+ minTime);
		//one of minTimeNodeStop and minTimeTopologyAlteration must be less than 1.0.
		//unless there are no topology alterations and each node moves the same distance.
		//or it's the last step and things either move at zero or max;
		if(minTime != 1.0){
			if(minTime == minTimeTopologyAlteration){
				treeNext = findTreeAtMinTime(treeB, directions, minTime);		
				totalDistance += findL1Distance(tree1, treeNext);
				timeDistance.add(minTime);
				getPath(treeNext, tree2, treeNext, timeDistance, totalDistance, trees);
			} else {
				treeNext = findTreeAtMinTime(treeB, directions, minTime);
				getPath(tree1, tree2, treeNext, timeDistance, totalDistance, trees);
			}		
		} else {
			treeNext = findTreeAtMinTime(treeB, directions, 1.0);
			totalDistance += findL1Distance(tree1, treeNext);
			getPath(treeNext, tree2, treeNext, timeDistance, totalDistance, trees);
		}		
		return trees;
	}
	
	public static boolean treesAreEqual(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		boolean equal = true; //assume equal until 1 topology change or height change then return false;
		for(tNode n1 : tree1){
			String taxa1 = n1.getTaxa().get(0);
			for(tNode n2 : tree2){
				String taxa2 = n2.getTaxa().get(0);
				if(taxa1.compareTo(taxa2) == 0){
					double height1 = n1.getHeight();
					double height2 = n2.getHeight();
					if(height1 != height2){
						return false;
					}
				}			
			}
		}		
		return equal;
	}
	public static double findNodeStop(ArrayList<tNode> tree1, ArrayList<tNode> tree2, ArrayList<tNode> directions){
		//directions is aligned with tree1, tree2 isn't.
		//make a tree2 which is ordered by tree1;
		ArrayList<tNode> tree2Ordered = getTree2Ordered(tree1, tree2);
		//using directions, tree2Ordered, and tree1 --> find out if the node will stop
		double minTime = 10.0;//adjust
		for(int i = 0; i < tree1.size(); i++){
			double nodeOri = tree1.get(i).getHeight();
			double actualDest = tree2Ordered.get(i).getHeight();
			double direction = directions.get(i).getHeight();
			if(direction != 0){
				double intersection = (actualDest - nodeOri)/direction;
				if(intersection <= 1.0 && intersection < minTime){
					minTime = intersection;
				}
			}						
		}
		return minTime;
	}
	
	public static ArrayList<tNode> getTree2Ordered(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		ArrayList<tNode> tree2Ordered = new ArrayList<tNode>();
		for(int i = 0; i < tree1.size(); i++){
			ArrayList<String> taxa = new ArrayList<String>();
			taxa.add(tree1.get(i).getTaxa().get(0));
			for(int j = 0; j < tree2.size(); j++){				
				if(taxa.get(0).compareTo(tree2.get(j).getTaxa().get(0))==0){
					double height = tree2.get(j).getHeight();
					tree2Ordered.add(new tNode(taxa, height));
				}
			}
		}
		return tree2Ordered;		
	}
	
	
	public static double findTopologyAlterations(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		double minTime = 10.0;
		ArrayList<tNode> tree2Ordered = getTree2Ordered(tree1, tree2);
		ArrayList<tNode> timeSwap = timeSwapTree(tree1, tree2Ordered);
		for(tNode n : timeSwap){
			if(n.getHeight() < minTime && n.getHeight() > 0.0){
				minTime = n.getHeight();
			}
		}
		return minTime;
	}
	public static ArrayList<tNode> findTreeAtMinTime(ArrayList<tNode> tree1, ArrayList<tNode> directions, double minTime){
		ArrayList<tNode> treeNext = new ArrayList<tNode>();
		for(int i = 0; i<tree1.size(); i++){
			double height = tree1.get(i).getHeight() + directions.get(i).getHeight()*minTime;
			ArrayList<String> taxa =  new ArrayList<String>();
			taxa.add(tree1.get(i).getTaxa().get(0));
			treeNext.add(new tNode(taxa, height));
		}		
		return treeNext;
	}
	public static double findL1Distance(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		double totalDistance = 0.0;
		//assume ordered (as they meet at 1 or 0 topology changes;		
		for(int i = 0; i<tree1.size(); i++){
			double distance = Math.abs(tree1.get(i).getHeight() - tree2.get(i).getHeight());
			totalDistance += distance;
		}	
		return totalDistance;
	}
	
	
	
	//Attempt2 from here;	
	
	/*
	 * Case0 = If at least 1 taxon in the cherry of original tree is in the cherry of the destination tree - Similar to Attempt1
	 * Case1 = If NONE of the taxa in the cherry of the original tree are in the cherry of the destination tree
	 */	
	
	//Generate takes taxa, heights of taxa, time you want the tree at, determines if you want incremental answer or fixed point; 
	public static void generate(String[] taxa1, double[] height1,String[] taxa2, double[] height2, double time, boolean iterate, String filename){
		ArrayList<tNode> tree1 = new ArrayList<tNode>();
		ArrayList<tNode> tree2 = new ArrayList<tNode>();		
		//perhaps start with check if taxa and height are of equal length and also that taxa1 and taxa2 are equal length;
		//also check how many taxa. -  need at least 2;
		for(int i = 0; i<taxa1.length; i++){
			ArrayList<String> s1 = new ArrayList<String>();
			ArrayList<String> s2 = new ArrayList<String>();
			s1.add(taxa1[i]);
			s2.add(taxa2[i]);			
			tNode node1 = new tNode(s1, height1[i]);
			tNode node2 = new tNode(s2, height2[i]);
			tree1.add(node1);
			tree2.add(node2);			
		}
		if(iterate){
			runIterations(tree1, tree2, time, filename);
		} else {
		chooseCase(tree1, tree2, time, iterate);
		}
	}
	//Runs if increments desired;  Takes both trees and the increment for which to get trees at (must be less than 1)
	//After getting tree for each increment turn it into newick format, add to string[]tree to be sent to file after final increment;
	//NOTE: increments always include time 0 and time 1;
	public static void runIterations(ArrayList<tNode> tree1, ArrayList<tNode> tree2, double increment, String filename){		
		String[] trees = new String[(int)(1/increment)+2];
		ArrayList<tNode> treeToConvert = null;
		int arrayCounter = 0;
		for(double i = 0; i<1.0; i += increment){
			treeToConvert = chooseCase(tree1, tree2, i, true);
			trees[arrayCounter] = convertToNewick(treeToConvert);
			arrayCounter++;
		}		
		trees[arrayCounter] = convertToNewick(tree2);
		printToFile(trees, filename);
	}
	//Decides if path is case0 or case1;
	public static ArrayList<tNode> chooseCase(ArrayList<tNode> tree1, ArrayList<tNode> tree2, double time, boolean iterate){
		//Sorts taxa by heights to find cherry		
		tree1 = mergeSort(tree1);
		tree2 = mergeSort(tree2);
		ArrayList<tNode> returnTree = null;
		
		//Finds how many taxa are in the cherry (since this can be 2+)
		double heightTree1 = tree1.get(0).getHeight();		
		int counterTree1 = 0;		
		for(int i=0; tree1.get(i).getHeight() == heightTree1; i++){
			counterTree1++;
		}		
		//for loop checking the counterTree1 taxa in tree1 vs the 2 taxa in tree2.  If any are equal it's case0; else case1.
		boolean caseHasBeenAssigned = false;
		for(int i=0; i<counterTree1; i++){
			String taxa1 = tree1.get(i).getTaxa().get(0);
			if(taxa1.compareTo(tree2.get(0).getTaxa().get(0))==0 || taxa1.compareTo(tree2.get(1).getTaxa().get(0))==0){	
				caseHasBeenAssigned = true;
				returnTree = case0(tree1, tree2, time, iterate);
				break;
			}			
		}
		if(!caseHasBeenAssigned){
			returnTree = case1(tree1, tree2, time, iterate, counterTree1);
		}		
		returnTree = mergeSort(returnTree);
		return returnTree;
	}		
	//Case0 finds the new tree at time;
	public static ArrayList<tNode> case0(ArrayList<tNode> tree1, ArrayList<tNode> tree2, double time, boolean iterate){		
		ArrayList<tNode> treeAtTime = new ArrayList<tNode>();				
		ArrayList<tNode> speedTree = new ArrayList<tNode>();	
		
		for(int i =0; i<tree1.size(); i++){
			String taxaTree1 = tree1.get(i).getTaxa().get(0);
			double heightTree1 = tree1.get(i).getHeight();
			for(int j=0; j<tree2.size(); j++){
				String taxaTree2 = tree2.get(j).getTaxa().get(0);
				double heightTree2 = tree2.get(j).getHeight();
				if(taxaTree1.compareTo(taxaTree2)==0){					
					double speed = heightTree2 - heightTree1;
					speedTree.add(new tNode(tree1.get(i).getTaxa(), speed));					
				}
			}
		} 
		//have speedTree, have time, have tree1.  Simply find the new tree at time;		
		for(int i = 0; i<tree1.size(); i++){
			double h = tree1.get(i).getHeight() + speedTree.get(i).getHeight()*time;
			treeAtTime.add(new tNode(tree1.get(i).getTaxa(),h));
		}		
		if(!iterate){
		System.out.println("Tree at time: " + time);
		outputTree(treeAtTime);
		}		
		return treeAtTime;		
	}
	/*
	 * Case1: finds 1st intersection between cherry node and another node.  Then:
	 *  if that occurs before or at desired time --> make tree at time --> chooseCase --> either Case0 or Case1 to find the tree at timel
	 *  else occurs after desired time --> case0, since there is no intersection before time
	 */
	public static ArrayList<tNode> case1(ArrayList<tNode> tree1, ArrayList<tNode> tree2, double time, boolean iterate, int cherryCounter){
		//Add a check to find slowest 2 (uphill) of the 2+ taxa in the cherry (Could then remove if, else statement as get(1) is always bigger)
		ArrayList<tNode> speedTree = new ArrayList<tNode>();		
		
		for(int i =0; i<tree1.size(); i++){
			String taxaTree1 = tree1.get(i).getTaxa().get(0);
			double heightTree1 = tree1.get(i).getHeight();
			for(int j=0; j<tree2.size(); j++){
				String taxaTree2 = tree2.get(j).getTaxa().get(0);
				double heightTree2 = tree2.get(j).getHeight();
				if(taxaTree1.compareTo(taxaTree2)==0){					
					double speed = heightTree2 - heightTree1;
					speedTree.add(new tNode(tree1.get(i).getTaxa(), speed));					
				}
			}
		}				
		//Find max speed of cherry of tree1 (no need to regard cherry of tree2 as we are in case1 because they are different);
		//Set other taxa of cherry as this speed;
		if(speedTree.get(0).getHeight() > speedTree.get(1).getHeight()){
			speedTree.get(1).setHeight(speedTree.get(0).getHeight());
		} else {
			speedTree.get(0).setHeight(speedTree.get(1).getHeight());
		}
		
		//Create tree2Ordered which is tree2 ordered by tree1's taxa;
		ArrayList<tNode> tree2Ordered = new ArrayList<tNode>();
		for(int i=0; i<tree1.size(); i++){
			double h = tree1.get(i).getHeight() + speedTree.get(i).getHeight();
			tree2Ordered.add(new tNode(tree1.get(i).getTaxa(), h));
		}
		/*
		 * create timeSwap tree, the heights of the tree is the time that the node will intersect with the cherry node;
		 * timeSwap is calc'd by t=(y-x)/((x'-x)-(y'-y))
		 * tree2Ordered = y' tree, tree1 = y tree.
		 * We know x and x'.  So now make timeSwap with tree1 and tree2Ordered;
		 */
		ArrayList<tNode> timeSwapTree = new ArrayList<tNode>();
		double xOri = tree1.get(1).getHeight();
		double xDest = tree2Ordered.get(1).getHeight();
		for(int i = 2; i<tree1.size(); i++){
			String taxaTree1 = tree1.get(i).getTaxa().get(0);
			double yOri = tree1.get(i).getHeight();			
			double yDest = tree2Ordered.get(i).getHeight();
			double timeSwap = (yOri-xOri) / ((xDest - xOri) - (yDest - yOri));
			timeSwapTree.add(new tNode(tree1.get(i).getTaxa(), timeSwap));		
		}		
		//Find smallest of timeSwapTree;	
		double minTime = 1;
		for(int i = 0 ; i<timeSwapTree.size(); i++){
			double nextTime = timeSwapTree.get(i).getHeight();
			if(nextTime < minTime && nextTime > 0.0){
				minTime = timeSwapTree.get(i).getHeight();
			}
		}		
		ArrayList<tNode> treeNext = null;
		ArrayList<tNode> returnTree = null;
		//Decide next step (case0 or chooseCase, depending on minTime and time)
		if(time < minTime){
			//Then the cherry doesn't change in time and therefore case 0  applies.
			returnTree = case0(tree1, tree2, time, iterate);
		} else {			
			//Then the cherry does change in time;			
			//Create new tree1 - call it treeNext
			//Send treeNext to chooseCase ---->
			treeNext = new ArrayList<tNode>();
			double nextHeight = 0;
			for(int i = 0; i<tree1.size(); i++){
				nextHeight = tree1.get(i).getHeight() + speedTree.get(i).getHeight()*minTime; 
				treeNext.add(new tNode(tree1.get(i).getTaxa(), nextHeight));
			}		
			if(!iterate){
			System.out.println("Tree at time: " + minTime);
			outputTree(treeNext);
			}
			returnTree = chooseCase(treeNext, tree2, (time-minTime)/((1.0)-minTime), iterate);					
		}				
		return returnTree;
	}
	//prints out tree, for non-incremental part
	public static void outputTree(ArrayList<tNode> tree){
		tree = mergeSort(tree);		
		ArrayList<tNode> treeForPrint = new ArrayList<tNode>();		
		for(int i = 1; i < tree.size(); i++){	
			ArrayList<String> s = new ArrayList<String>();
			for(int j=0;  j<=i; j++){
				s.add(tree.get(j).getTaxa().get(0));				
			}
			treeForPrint.add(new tNode(s, tree.get(i).getHeight()));			
		}			
		System.out.println(treeForPrint);
	}
	//ConvertToNewick takes a tree of nodes and converts it to newick format;
	public static String convertToNewick(ArrayList<tNode> tree){
		String newickFormat = tree.get(0).getTaxa().get(0);		
		for(int i = 1; i<tree.size(); i++){
			if(i==1){
				newickFormat = newickFormat + ":" + tree.get(1).getHeight();
			} else {
				newickFormat = newickFormat + ":" + (tree.get(i).getHeight() - tree.get(i-1).getHeight());
			}
			newickFormat = newickFormat + "," + tree.get(i).getTaxa().get(0) + ":" + tree.get(i).getHeight();
			newickFormat = "(" + newickFormat + ")";			
		}				
		return newickFormat;
	}
	//Output into file;
	public static void printToFile(String[] trees, String filename){
		try{
			File file = new File(filename);			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for(int i = 0; i< trees.length; i++){
				if(trees[i] !=null){
					bw.write(trees[i]);
				}
				bw.newLine();				
			}			
			System.out.println("A file of trees in newick format has been generated at: ");
			System.out.println(file.getAbsolutePath());
			bw.flush();
			bw.close();			
		}catch(IOException e) {System.out.println(e);}
	}	
	
	
	
	
	
	
	//MergeSort followed by Merge, sorts ArrayList<tNode>'s by heights of tNodes
	public static ArrayList<tNode> mergeSort(ArrayList<tNode> tree){
		
		ArrayList<tNode> left = new ArrayList<tNode>();
		ArrayList<tNode> right = new ArrayList<tNode>();
		int center;
		if(tree.size() ==1){
			return tree;
		} else {		
			center = tree.size()/2;
			for(int i = 0; i<center; i++){
				left.add(tree.get(i));
			}
			for(int i = center; i<tree.size(); i++){
				right.add(tree.get(i));
			}			
			left = mergeSort(left);
			right = mergeSort(right);
			
			merge(left, right, tree);
		}		
		return tree;
	}
	public static void merge (ArrayList<tNode> left, ArrayList<tNode> right, ArrayList<tNode> tree) {
		int leftIndex = 0, rightIndex = 0, treeIndex = 0;
		
		while(leftIndex <left.size() && rightIndex <right.size()){
			if(left.get(leftIndex).getHeight() < right.get(rightIndex).getHeight()){
				tree.set(treeIndex,  left.get(leftIndex));
				leftIndex++;
			}else {
				tree.set(treeIndex,  right.get(rightIndex));
				rightIndex++;
			}
			treeIndex++;
		}		
		ArrayList<tNode> rest;
		int restIndex;
		if(leftIndex >= left.size()){
			rest = right;
			restIndex = rightIndex;
		}else{
			rest=left;
			restIndex = leftIndex;
		}		
		for(int i=restIndex; i<rest.size(); i++){
			tree.set(treeIndex,  rest.get(i));
			treeIndex++;
		}
	}
	
	
}
