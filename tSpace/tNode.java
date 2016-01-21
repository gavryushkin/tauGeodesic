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
		trees.add(convertToNewick(treeB));
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
		double nodeStop = findNodeStop(treeB, tree2, directions);
		if(nodeStop != 10.0){
			minTimeNodeStop = nodeStop;
		}
		double minTimeTopologyAlteration = 10.0;
		//check for topology changes
		double topologyAlteration = findTopologyAlterations(treeB, treeNext);
		if(topologyAlteration != 10.0){
			minTimeTopologyAlteration = topologyAlteration;				
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

	public static ArrayList<tNode> timeSwapTree(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		ArrayList<tNode> timeSwapTree = new ArrayList<tNode>();
		for(int i = 0; i < tree1.size() ; i++){
			double xOri = tree1.get(i).getHeight();
			double xDest = tree2.get(i).getHeight();
			for(int j = i + 1; j < tree1.size(); j++){
				double yOri = tree1.get(j).getHeight();					
				double yDest = tree2.get(j).getHeight();
				double timeSwap = (yOri-xOri) / ((xDest - xOri) - (yDest - yOri));
				timeSwapTree.add(new tNode(tree1.get(i).getTaxa(), timeSwap));	
			}
		}
		return timeSwapTree;
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





//for cat2 L1 trees:	
	//The following section is for CAT(2) trees. 
	//First, getStarted takes in the info, generates the trees, then depending on inputs finds the path or tree at time;  
	public static void getStarted(String[] t1LeftTaxa, double[] t1LeftHeight, String[] t1RightTaxa, double[] t1RightHeight,
			double t1RootHeight, String[] t2LeftTaxa, double[] t2LeftHeight, String[] t2RightTaxa, double[] t2RightHeight,
			double t2RootHeight, double currentTime, boolean findThePath){
		ArrayList<String> sub1T1 = stringArrayToList(t1LeftTaxa);
		ArrayList<String> sub2T1 = stringArrayToList(t1RightTaxa);
		ArrayList<String> sub1T2 = stringArrayToList(t2LeftTaxa);
		ArrayList<String> sub2T2 = stringArrayToList(t2RightTaxa);
		
		ArrayList<tNode> tree1 = generateCat2(t1LeftTaxa, t1LeftHeight, t1RightTaxa, t1RightHeight, t1RootHeight);
		ArrayList<tNode> tree2 = generateCat2(t2LeftTaxa, t2LeftHeight, t2RightTaxa, t2RightHeight, t2RootHeight);
		
		mergeSort(tree1);
		mergeSort(tree2);
		
		System.out.println("Tree1:" + tree1);
		System.out.println("Tree2: " + tree2);
		
		ArrayList<String> crossedRoot = new ArrayList<String>();
		ArrayList<String> trees = new ArrayList<String>();		
		ArrayList<Double> times = new ArrayList<Double>();
		
		//do I want a path or a tree? -->
		if(findThePath || currentTime > 1.0){		
			ArrayList<String> path1Trees = null;
			ArrayList<String> path2Trees = null;
			if(isTaxaEqual(t1LeftTaxa, t2LeftTaxa)){
				System.out.println();
				System.out.println("This is the path:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub1T2, sub2T2);	
				double totalDistance = 0.0;
				path1Trees = getCat2Path(tree1, tree1, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);			
				
			}else if(isTaxaEqual(t1LeftTaxa, t2RightTaxa)){
				System.out.println();
				System.out.println("This is the path:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub2T2, sub1T2);
				double totalDistance = 0.0;
				path2Trees = getCat2Path(tree1,tree1, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub2T2, sub1T2, currentTime);
				
			}else {
				System.out.println();
				System.out.println("PATH 1:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub1T2, sub2T2);	
				double totalDistancePath1 = 0.0;
				
				path1Trees = getCat2Path(tree1, tree1, tree2, trees, totalDistancePath1, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);
				
				System.out.println();
				System.out.println("PATH 2:");			
				tree1 = generateCat2(t1LeftTaxa, t1LeftHeight, t1RightTaxa, t1RightHeight, t1RootHeight);
				trees = new ArrayList<String>();			
				times = new ArrayList<Double>();
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub2T2, sub1T2);
				double totalDistancePath2 = 0.0;
				
				path2Trees = getCat2Path(tree1,tree1, tree2, trees, totalDistancePath2, times, crossedRoot, sub1T1, sub2T1, sub2T2, sub1T2, currentTime);	
			}
			

			printCat2Trees(path1Trees, "Cat2Path1.txt");
			printCat2Trees(path2Trees, "Cat2Path2.txt");
		
			//we want the tree:	
		} else {
			ArrayList<tNode> endTree1 = null;
			ArrayList<tNode> endTree2 = null;			
					
			if(isTaxaEqual(t1LeftTaxa, t2LeftTaxa)){
				System.out.println();
				System.out.println("This is the Tree:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub1T2, sub2T2);	
				double totalDistance = 0.0;
				endTree1 = getCat2Tree(tree1, tree1, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);			
				
			}else if(isTaxaEqual(t1LeftTaxa, t2RightTaxa)){
				System.out.println();
				System.out.println("This is the path:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub2T2, sub1T2);
				double totalDistance = 0.0;
				endTree2 = getCat2Tree(tree1,tree1, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub2T2, sub1T2, currentTime);
				
			}else {
				System.out.println();
				System.out.println("PATH 1:");
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub1T2, sub2T2);	
				double totalDistancePath1 = 0.0;
				
				endTree1 = getCat2Tree(tree1, tree1, tree2, trees, totalDistancePath1, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);
				System.out.println();
				System.out.println("EndTree1: " + endTree1);
				
				System.out.println();
				System.out.println("PATH 2:");			
				tree1 = generateCat2(t1LeftTaxa, t1LeftHeight, t1RightTaxa, t1RightHeight, t1RootHeight);
				trees = new ArrayList<String>();			
				times = new ArrayList<Double>();
				crossedRoot = findCrossedRoot(sub1T1, sub2T1, sub2T2, sub1T2);
				double totalDistancePath2 = 0.0;
				
				endTree2 = getCat2Tree(tree1,tree1, tree2, trees, totalDistancePath2, times, crossedRoot, sub1T1, sub2T1, sub2T2, sub1T2, currentTime);	
				System.out.println();			
				System.out.println("EndTree2: " + endTree2);				
			}				
			
			if(endTree1 != null){
				ArrayList<String> endTree = new ArrayList<String>();
				endTree.add(convertCat2ToNewick(endTree1, endTree1.get(0).getTaxa().get(0)));
				printCat2Trees(endTree, "Cat2Tree1.txt");
			}
			if(endTree2 != null){
				ArrayList<String> endTree = new ArrayList<String>();
				endTree.add(convertCat2ToNewick(endTree2, endTree2.get(0).getTaxa().get(0)));
				printCat2Trees(endTree, "Cat2Tree2.txt");
			}
		}
		
		
		
		
	}
	
	private static ArrayList<tNode> getCat2Tree(ArrayList<tNode> tree1, ArrayList<tNode> treeB, ArrayList<tNode> tree2,
			ArrayList<String> trees, double totalDistance, ArrayList<Double> times, ArrayList<String> crossedRoot,
			ArrayList<String> sub1T1, ArrayList<String> sub2T1, ArrayList<String> sub1T2, ArrayList<String> sub2T2, 
			double currentTime){
		System.out.println(".............................................................................................");
		System.out.println();	
		System.out.println("Initial Tree: " + tree1);
		System.out.println();
		//instead of recursive make a while loop, as in while time < currentTime
		times.add(0.0);
		ArrayList<tNode> treeA = new ArrayList<tNode>();
		treeA.addAll(tree1);
		while(currentTime > times.get(times.size()-1)){
			
			//each time need to convert "currentTime" to modCurrentTime;
			double modCurrentTime = (currentTime - times.get(times.size()-1))/(1.0-times.get(times.size()-1));			
			
			ArrayList<tNode> directions = findCat2Directions(treeA, tree2, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2);
								
			//get minTime;
			double minTimeNodeStop = 10.0;//adjust
			double nodeStop = findCat2NodeStop(treeA, tree2, directions, crossedRoot);
			if(nodeStop != 10.0 && nodeStop > 0.0){
				minTimeNodeStop = nodeStop;
			}
			
			double minTimeIntersection = 10.0;
			double intersection = findCat2Intersections(treeA, directions, crossedRoot);
			if(intersection != 10.0 && intersection > 0.0){
				minTimeIntersection = intersection;
			}
			double minTime = Math.min(minTimeNodeStop, minTimeIntersection);
			
			//get the tree at minTime; 
			//add it to times; change destinationTree;
			minTime = Math.min(minTime, modCurrentTime);			
			double time = minTime;
			if(!times.isEmpty()){
				double prevTime = times.get(times.size()-1);
				time = prevTime + (1.0-prevTime)*minTime;
			}
			times.add(time);	
			
			treeB = findCat2NextTree(treeA, directions, minTime, crossedRoot);
			totalDistance += findL1Cat2Distance(treeA, treeB);
			treeA = new ArrayList<tNode>();
			treeA.addAll(treeB);
			System.out.println("TreeNext:" + treeA);	
			System.out.println("TOTAL: " + totalDistance);
			
		}
		return treeA;	
	}
	
	private static ArrayList<String> findCrossedRoot(ArrayList<String> sub1T1, ArrayList<String> sub2T1,
			ArrayList<String> sub1T2, ArrayList<String> sub2T2){
		ArrayList<String> crossedRoot = new ArrayList<String>();
		//add each taxa which isn't crossing the root to crossedRoot;
		for(String taxa: sub1T1){
			if(sub1T2.contains(taxa)){
				crossedRoot.add(taxa);
			}
		}
		for(String taxa: sub2T1){
			if(sub2T2.contains(taxa)){
				crossedRoot.add(taxa);
			}
		}
		return crossedRoot;
	}
	
	private static ArrayList<String> stringArrayToList(String[] array){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0 ; i< array.length ; i++){
			list.add(array[i]);
		}	
		return list;
	}
	//returns true if there are not changes over the root node;
	private static boolean isTaxaEqual(String[] t1Taxa, String[] t2Taxa){
		//go through the taxa
		boolean equal = false;
		for(String taxa1 : t1Taxa){
			for(String taxa2 : t2Taxa){
				if(taxa1.equals(taxa2)){
					equal = true;
				}
			}
			if(!equal){
				return false;
			}
			equal = false;
		}
		return true;
	}
	
	private static ArrayList<String> getCat2Path(ArrayList<tNode> tree1, ArrayList<tNode> treeB, ArrayList<tNode> tree2,
			ArrayList<String> trees, double totalDistance, ArrayList<Double> times, ArrayList<String> crossedRoot,
			ArrayList<String> sub1T1, ArrayList<String> sub2T1, ArrayList<String> sub1T2, ArrayList<String> sub2T2, 
			double currentTime){
		//go through each taxa of the tree	
		System.out.println(".............................................................................................");
		System.out.println();
		mergeSort(treeB);
		
		trees.add(convertCat2ToNewick(tree1, crossedRoot.get(0)));  //separate to subtrees, find newick format between the two, and add a comma between them.
		//baseCase:
		if(areCat2TreesEqual(tree1, tree2)){
			System.out.println("*********************************************************************************************");
			System.out.println("Total Distance:  " + totalDistance);
			System.out.println("Times:" + times);
			System.out.println("End Tree: " + tree1);
			System.out.println("*********************************************************************************************");
			return trees;
		}			
		
		ArrayList<tNode> directions = findCat2Directions(treeB, tree2, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2);  //get arrayList (sorted by tree1); 	
		System.out.println("Directions: " + directions);
		//create treeNext		
		ArrayList<tNode> treeNext = getTreeNext(treeB, directions);	
		mergeSort(treeNext);		
		//Now find next intersection/node stop,			
		double minTimeNodeStop = 10.0;//adjust
		double nodeStop = findCat2NodeStop(treeB, tree2, directions, crossedRoot);
		if(nodeStop != 10.0 && nodeStop > 0.0){
			minTimeNodeStop = nodeStop;
		}
		System.out.println("nodestop: " + nodeStop);
		
		double minTimeIntersection = 10.0;
		double intersection = findCat2Intersections(treeB, directions,crossedRoot);
		if(intersection != 10.0 && intersection > 0.0){
			minTimeIntersection = intersection;
		}
		System.out.println("Intersections: " + intersection);	
		double minTime = Math.min(minTimeNodeStop, minTimeIntersection);
		
		ArrayList<tNode> theRealTree1 = new ArrayList<tNode>();
		theRealTree1.addAll(tree1);
		
		double time = minTime;
		if(!times.isEmpty()){
			double prevTime = times.get(times.size()-1);
			time = prevTime + (1.0-prevTime)*minTime;
		}		
		if(currentTime <= time){
			double prevTime = times.get(times.size()-1);
			minTime = (time - prevTime)/(1.0 - prevTime);
			treeNext = findCat2NextTree(treeB, directions, minTime, crossedRoot);
			ArrayList<String> timeTree = new ArrayList<String>();
			timeTree.add(convertCat2ToNewick(treeNext, crossedRoot.get(0)));
			return timeTree;
		}		
		
		if(minTime != 1.0){
			if(minTime == minTimeIntersection){
				treeNext = findCat2NextTree(treeB, directions, minTime, crossedRoot);
				totalDistance += findL1Cat2Distance(theRealTree1, treeNext);
				times.add(time);
				System.out.println("TreeNext:" + treeNext);
				getCat2Path(treeNext, treeNext, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);
			} else {
				treeNext = findCat2NextTree(treeB, directions, minTime, crossedRoot);
				System.out.println("TreeNext:" + treeNext);
				getCat2Path(theRealTree1, treeNext, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);
			}		
		} else {
			treeNext =  findCat2NextTree(treeB, directions, minTime, crossedRoot);
			totalDistance += findL1Cat2Distance(tree1, treeNext);
			System.out.println("TreeNext, also final tree:" + treeNext);	
			times.add(time);
			getCat2Path(treeNext, treeNext, tree2, trees, totalDistance, times, crossedRoot, sub1T1, sub2T1, sub1T2, sub2T2, currentTime);
			
		}			
		return trees;
	}
	//calculates the distance between two trees, this should be called after/at each topology change (intersection).
	private static double findL1Cat2Distance(ArrayList<tNode> tree1, ArrayList<tNode> tree2){
		double distance = 0.0;
		//because the two trees are only a single topology change apart, can simply find distance between tree1[i] and tree2[i].
		//these trees should also be of the same size;
		for(int i = 0; i < tree1.size(); i++){
			distance += Math.abs(tree1.get(i).getHeight() - tree2.get(i).getHeight());			
		}	
		return distance;
	}
	
	private static ArrayList<tNode> findCat2NextTree(ArrayList<tNode> tree, ArrayList<tNode> directions,
			double time, ArrayList<String> crossedRoot){
		//we know an intersection or a nodeStop occurs, so something is going to happen/move/intersect/just stop.
		mergeSort(tree);
		ArrayList<tNode> endTree;
		ArrayList<String> sub1Taxa = new ArrayList<String>();
		ArrayList<String> sub2Taxa = new ArrayList<String>();
		ArrayList<tNode> subTree1 = new ArrayList<tNode>();
		ArrayList<tNode> subTree2 = new ArrayList<tNode>();
		ArrayList<String> rootTaxa = new ArrayList<String>();
		
		//determine both subTrees
		rootTaxa.addAll(tree.get(tree.size()-1).getTaxa());
		sub1Taxa.addAll(tree.get(tree.size()-2).getTaxa());
		rootTaxa.removeAll(sub1Taxa);
		
		for(int i = tree.size()-3; i >= 0; i--){
			if(rootTaxa.containsAll(tree.get(i).getTaxa())){
				rootTaxa.removeAll(tree.get(i).getTaxa());
				sub2Taxa.addAll(tree.get(i).getTaxa());
			}
		}
		//sub1Taxa, sub2Taxa have the subtrees's taxa.  
		//covert them to the <tNode>'s required and add a "R" node as the roots
		subTree1 =  getSubtree(sub1Taxa, tree);
		subTree2 = getSubtree(sub2Taxa, tree);
		//add directions to each; 		
		subTree1 = addDirections(subTree1, directions, time);
		subTree2 = addDirections(subTree2, directions, time);
		
		//have new trees positions : time to recreate new tree  (endTree)
		mergeSort(subTree1);
		mergeSort(subTree2);
		//go through each node in both subtrees --> see if any nodes are at the same height of the root and not in crossedRoot;
		ArrayList<tNode> crossingNodes = findCrossingNodes(subTree1, crossedRoot);
		if(crossingNodes != null){
			subTree1.removeAll(crossingNodes);
			subTree2.addAll(crossingNodes);
		}
		crossingNodes = findCrossingNodes(subTree2, crossedRoot);
		if(crossingNodes != null){
			subTree2.removeAll(crossingNodes);
			subTree1.addAll(crossingNodes);
		}
		//now rebuild the subtrees, combine them, and add the root;		
		endTree = createEndTree(subTree1, subTree2);	
		
		mergeSort(endTree);
		endTree.add(new tNode(tree.get(tree.size()-1).getTaxa(), tree.get(tree.size()-1).getHeight()));
		return endTree;
	}
	

	private static ArrayList<tNode> getSubtree(ArrayList<String> subTaxa, ArrayList<tNode> tree){
		ArrayList<tNode> subTree = new ArrayList<tNode>();
		for(String taxon : subTaxa){
			for(tNode n: tree){
				if(n.getTaxa().contains(taxon)){
					ArrayList<String> taxa = new ArrayList<String>();
					taxa.add(taxon);
					subTree.add(new tNode(taxa, n.getHeight()));
					break;
				}
			}
		}
		ArrayList<String> root = new ArrayList<String>();
		//not sure we need R.
		root.add("R");
		subTree.add(new tNode(root, tree.get(tree.size()-1).getHeight()));	
		return subTree;
	}
	private static ArrayList<tNode> addDirections(ArrayList<tNode> subTree, ArrayList<tNode> directions, double time){		
		for(tNode n :  subTree){
			//find n.getTaxa in directions
			for(tNode d : directions){
				if(d.getTaxa().equals(n.getTaxa())){
					n.setHeight(n.getHeight() + (d.getHeight()*time));
					break;
				}
			}			
		}
		return subTree;		
	}
	
	private static ArrayList<tNode> findCrossingNodes(ArrayList<tNode> subTree , ArrayList<String> crossedRoot){
		ArrayList<tNode> crossingNodes = new ArrayList<tNode>();
		
		for(int i = 0; i < subTree.size() ; i++){
			tNode n = subTree.get(i);
			if(!n.getTaxa().get(0).equals("R")){
				if(n.getHeight() == subTree.get(subTree.size()-1).getHeight() && !crossedRoot.containsAll(n.getTaxa())){
					crossedRoot.addAll(n.getTaxa());
					crossingNodes.add(n);
				}
			}
		}
		return crossingNodes;
		
	}
	
	private static ArrayList<tNode> createEndTree(ArrayList<tNode> subTree1, ArrayList<tNode> subTree2){
		ArrayList<tNode> endTree = new ArrayList<tNode>();
		for(int i = 0; i < subTree1.size() ; i++){
			ArrayList<String> taxa = new ArrayList<String>();
			if(!subTree1.get(i).getTaxa().get(0).equals("R")){
				for(int j = 0; j <= i; j++){
					if(!subTree1.get(j).getTaxa().get(0).equals("R")){
						taxa.addAll(subTree1.get(j).getTaxa());
					}
				}			
				endTree.add(new tNode(taxa, subTree1.get(i).getHeight()));		
			}
		}
		for(int i = 0; i <  subTree2.size(); i++){
			ArrayList<String> taxa = new ArrayList<String>();
			if(!subTree2.get(i).getTaxa().get(0).equals("R")){
			for(int j = 0; j<= i; j++){
				if(!subTree2.get(j).getTaxa().get(0).equals("R")){
					
				
				taxa.addAll(subTree2.get(j).getTaxa());
				}
			}			
			endTree.add(new tNode(taxa, subTree2.get(i).getHeight()));
			}
		}		
		return endTree;
	}
	//great leftTree, rightTree then join them under the root
	private static ArrayList<tNode> generateCat2(String[] leftTaxa, double[] leftHeight, String[] rightTaxa,
			double[] rightHeight, double rootHeight){
		
		ArrayList<tNode> left = new ArrayList<tNode>();
		for(int i = 0; i < leftTaxa.length; i++){
			ArrayList<String> taxaL = new ArrayList<String>();
			taxaL.add(leftTaxa[i]);
			left.add(new tNode(taxaL, leftHeight[i]));
		}
		ArrayList<tNode> right = new ArrayList<tNode>();
		for(int i = 0; i < rightTaxa.length; i++){
			ArrayList<String> taxaR = new ArrayList<String>();
			taxaR.add(rightTaxa[i]);
			right.add(new tNode(taxaR, rightHeight[i]));
		}
		mergeSort(left);
		mergeSort(right);
		
		//now that they are sorted, add them up into a tree		
		ArrayList<tNode> tree = new ArrayList<tNode>();
		ArrayList<tNode> leftTree = new ArrayList<tNode>();
		ArrayList<tNode> rightTree =  new ArrayList<tNode>();
		
		for(int i = 0; i < left.size(); i++){
			ArrayList<String> taxaL = new ArrayList<String>();
			for(int j = 0; j <= i; j++){
				taxaL.addAll(left.get(j).getTaxa());
			}
			leftTree.add(new tNode(taxaL, left.get(i).getHeight()));
		}
		
		for(int i = 0; i <  right.size(); i++){
			ArrayList<String> taxaR = new ArrayList<String>();
			for(int j = 0; j <= i; j++){
				taxaR.addAll(right.get(j).getTaxa());
			}
			rightTree.add(new tNode(taxaR, right.get(i).getHeight()));
		}
				
		tree.addAll(leftTree);
		tree.addAll(rightTree);
		ArrayList<String> taxaAll = new ArrayList<String>();
		taxaAll.addAll(leftTree.get(leftTree.size()-1).getTaxa());
		taxaAll.addAll(rightTree.get(rightTree.size()-1).getTaxa());
		tree.add(new tNode(taxaAll, rootHeight));		
		
		return tree;
	}
	
	private static boolean areCat2TreesEqual(ArrayList<tNode> tree1, ArrayList<tNode> tree2){		
		//if they're sorted which they are, then each node at each height will equal one of the other nodes at that height, else --> not equal.
		boolean equal = true;
		for(tNode n1: tree1){
			if(n1.getTaxa().size() > 1){
				double height1 = n1.getHeight();
				boolean foundMatch = false;
				for(tNode n2: tree2){
					if(n2.getHeight() == height1 && n2.getTaxa().size() == n1.getTaxa().size()){
						if(n1.getTaxa().containsAll(n2.getTaxa())){
							//then they are equal
							foundMatch = true;
						}
					}
				}
				if(!foundMatch){
					equal = false;
				}
			}			
		}			
		return equal;
	}
	
	//get a tNode ArrayList of taxa and their directions.
	private static ArrayList<tNode> findCat2Directions(ArrayList<tNode> tree1, ArrayList<tNode> tree2, ArrayList<String> crossedRoot,
			ArrayList<String> sub1T1, ArrayList<String> sub2T1, ArrayList<String> sub1T2, ArrayList<String> sub2T2){
		ArrayList<tNode> directions = new ArrayList<tNode>();
		//for each taxa find it in the subtrees, assume if it's not in the 1st its in the second.
		//if they are in the same:  find direction/max as usual
		//else and not in crossedRoot, then they need to cross root --> findtotalDistance --> max/direction, dir == +ve
	
		//most of the "heights" should really be refered to as "speeds/directions"
		ArrayList<String> foundTaxa = new ArrayList<String>();
		double max = 0.0;
		for(tNode n1: tree1){
			for(String taxa : n1.getTaxa()){ 
				if(!foundTaxa.contains(taxa)){
					foundTaxa.add(taxa);
					ArrayList<String> nextTaxa = new ArrayList<String>();
					nextTaxa.add(taxa);					
					if(sub1T1.contains(taxa)){
						if(sub2T2.contains(taxa) && !crossedRoot.contains(taxa)){
							//direction will be +ve, work out what it's max speed is 
							double height1 = n1.getHeight();
							double crossedRootSpeed = findCrossedRootSpeed(taxa, height1, tree2);
							if(crossedRootSpeed > max){
								max = crossedRootSpeed;
							}
							directions.add(new tNode(nextTaxa, crossedRootSpeed));//changed from 1 to the actual speed = crossedRootSpeed	
							//will need to make sure this intersects with root properly!
							
						}else{ //it's in sub1T2 (same)
							for(tNode n2: tree2){
								if(n2.getTaxa().contains(taxa)){
									double height = (n2.getHeight() - n1.getHeight());					
									if(Math.abs(height) > max){
										max = Math.abs(height);
									}
									/*
									if(height != 0){
										height = height/Math.abs(height); //results in either 1 or -1.
									}
									*/									
									directions.add(new tNode(nextTaxa, height));
									break;
								}
							}						
						}
					}else { //it's in sub2T1
						if(sub1T2.contains(taxa) && !crossedRoot.contains(taxa)){
							double height1 = n1.getHeight();
							double crossedRootSpeed = findCrossedRootSpeed(taxa, height1, tree2);
							if(crossedRootSpeed > max){
								max = crossedRootSpeed;
							}
							directions.add(new tNode(nextTaxa, crossedRootSpeed)); //speed adjusted from 1 to CRS as above	
							
						}else{ //it's in sub2T2 (same)
							for(tNode n2: tree2){
								if(n2.getTaxa().contains(taxa)){
									double height = (n2.getHeight() - n1.getHeight());					
									if(Math.abs(height) > max){
										max = Math.abs(height);
									}
									/* standardization removed;
									if(height != 0){
										height = height/Math.abs(height); //results in either 1 or -1.
									}
									*/
									directions.add(new tNode(nextTaxa, height));
									break;
								}
								
							}
						}
					}				
				}
			}
		}		
		//add root speed;
		ArrayList<String> root = new ArrayList<String>();
		root.addAll(tree1.get(tree1.size()-1).getTaxa());
		directions.add(new tNode(root, 0));
		for(tNode n : directions){
			n.setHeight(n.getHeight()); //no longer multiply by max, as all roots should have their own speeds not the max speed;
		}		
		return directions;
	}
	
	//find treeNext;
	private static ArrayList<tNode> getTreeNext(ArrayList<tNode> treeB, ArrayList<tNode> directions){
		ArrayList<tNode> treeNext = new ArrayList<tNode>();
		ArrayList<String> coveredTaxa = new ArrayList<String>();
		for(tNode n1: treeB){
			for(String taxa : n1.getTaxa()){
				if(!coveredTaxa.contains(taxa)){
					coveredTaxa.add(taxa);
					for(tNode n2: directions){
						if(n2.getTaxa().get(0).equals(taxa)){
							double height =  n1.getHeight() + n2.getHeight();
							treeNext.add(new tNode(n1.getTaxa(), height));
							break;
						}
					}
				}
			}
		}	
		//add root;
		treeNext.add(new tNode(treeB.get(treeB.size()-1).getTaxa(), treeB.get(treeB.size()-1).getHeight()));
		return treeNext;
	}
	
	
	//initially we'll assume the root height doesn't change;
	private static double findCrossedRootSpeed(String taxa, double height1, ArrayList<tNode> tree2){
		//get height1 --> root, root --> height2;
		double max = 0.0;
		double rootHeight = tree2.get(tree2.size()-1).getHeight();
		double toRoot = rootHeight - height1;
		for(tNode n : tree2){
			if(n.getTaxa().contains(taxa)){
				double fromRoot = rootHeight - n.getHeight();
				max = toRoot + fromRoot;
				break;
			}
		}		
		return max;		
	}
	
	//add check to only calculate it if it IS in crossedRoot.
	private static double findCat2NodeStop(ArrayList<tNode> treeB, ArrayList<tNode> tree2,
			ArrayList<tNode> directions, ArrayList<String> crossedRoot){
		double minTime = 10.0;
		double direction = 0.0, nodeDest = 0.0, nodeOri = 0.0;
		ArrayList<String> coveredTaxa = new ArrayList<String>();
		for(tNode n1: treeB){
			for(String taxa: n1.getTaxa()){
				if(!coveredTaxa.contains(taxa)){
					coveredTaxa.add(taxa);
					if(crossedRoot.contains(taxa)){
						nodeOri = n1.getHeight();
					//find taxa in the other two
					//NOTE: if two are at the same point then you're getting the same value so 1st occurence is all that's required
						for(tNode n2: tree2){
							if(n2.getTaxa().contains(taxa)){
								nodeDest =  n2.getHeight();
								break;
							}
						}
						for(tNode nD: directions){
							if(nD.getTaxa().contains(taxa)){
								direction = nD.getHeight();
								break;
							}
						}
						if(direction != 0){
							double intersection = (nodeDest - nodeOri)/direction;
							if(intersection <= 1.0 && intersection < minTime){
								minTime = intersection;
							}	
						}
					}					
				}
			}
		}	
		return minTime;
	}
	
		private static double findCat2Intersections(ArrayList<tNode> tree1, ArrayList<tNode> directions,
			ArrayList<String> crossedRoot){
		ArrayList<String> rootTaxa = directions.get(directions.size()-1).getTaxa();
		rootTaxa.add("R");
		//find intersections for each subtree --> return least of both.		
		double time1 = findIntersection(tree1, directions, crossedRoot, rootTaxa);
		double time2 = findIntersection(tree1, directions, crossedRoot, rootTaxa);
		
		return Math.min(time1, time2);
	}
	private static double findIntersection(ArrayList<tNode> tree1, ArrayList<tNode> directions,
			ArrayList<String> crossedRoot, ArrayList<String> subtree){
		double minTime = 10.0;
		double time = 10.0;
		double yOri = 0.0, yDir = 0.0;
		for(int i = 0; i < subtree.size()-1  ;i++){
			String taxa = subtree.get(i);
			double xOri = findTaxaHeightInTree(taxa, tree1);
			double xDir = findTaxaHeightInTree(taxa, directions);
			for(int j = i+1; j < subtree.size(); j++){
				String taxa2 = subtree.get(j);
				if(taxa2.equals("R")){
					yOri = tree1.get(tree1.size()-1).getHeight();
					yDir = 0.0;
				} else {
					yOri = findTaxaHeightInTree(taxa2, tree1);
					yDir = findTaxaHeightInTree(taxa2, directions);
				}
				if((xDir - yDir) != 0){
					time = (yOri-xOri)/(xDir - yDir);	
				}				
				if(time > 0 && time < minTime){
					minTime = time;
				}
			}
		}			
		
		return minTime;
	}
	
	private static double findTaxaHeightInTree(String taxa, ArrayList<tNode> tree){
		double height = 0.0;
		for(tNode n: tree){
			if(n.getTaxa().contains(taxa)){
				height = n.getHeight();
				break;
			}
		}		
		return height;
	}
	
	//break into the two subtrees, then generate newick, then combine with a comma
	private static String convertCat2ToNewick(ArrayList<tNode> tree, String crossedRoot){
		String newickFormat = "";
		//crossedRoot should always be in sub1Taxa inorder to maintain tree viewing stability..
		ArrayList<String> sub1Taxa = new ArrayList<String>();
		ArrayList<String> sub2Taxa = new ArrayList<String>();
		ArrayList<String> rootTaxa = new ArrayList<String>();
		
		rootTaxa.addAll(tree.get(tree.size()-1).getTaxa());
		sub1Taxa.addAll(tree.get(tree.size()-2).getTaxa());
		rootTaxa.removeAll(sub1Taxa);
		for(int i = tree.size()-3; i >= 0; i--){
			if(rootTaxa.containsAll(tree.get(i).getTaxa())){
				rootTaxa.removeAll(tree.get(i).getTaxa());
				sub2Taxa.addAll(tree.get(i).getTaxa());
			}
		}		
		ArrayList<String> subTaxaHolder = new ArrayList<String>();
		if(!sub1Taxa.contains(crossedRoot)){
			subTaxaHolder.addAll(sub1Taxa);
			sub1Taxa = new ArrayList<String>();
			sub1Taxa.addAll(sub2Taxa);
			sub2Taxa = new ArrayList<String>();
			sub2Taxa.addAll(subTaxaHolder);
		}		

		ArrayList<tNode> subTree1 =  getSubtree(sub1Taxa, tree);
		ArrayList<tNode> subTree2 = getSubtree(sub2Taxa, tree);
		// with these you can call them to "covertToNewick" to get separate subtrees, and then combine.
		String tree1 = convertToModNewick(subTree1);
		String tree2 = convertToModNewick(subTree2);
		
		newickFormat =  "(" + tree1 + "," + tree2 + ")";
			
		return newickFormat;
	}
	
	private static String convertToModNewick(ArrayList<tNode> tree){		
		String newickFormat = tree.get(0).getTaxa().get(0);		
		for(int i = 1; i<tree.size(); i++){
			if(i==1){
				newickFormat = newickFormat + ":" + tree.get(1).getHeight();					
			} else {			
				newickFormat = newickFormat + ":" + (tree.get(i).getHeight() - tree.get(i-1).getHeight());
			}
			
			if(!tree.get(i).getTaxa().get(0).equals("R")){
				newickFormat = newickFormat + "," + tree.get(i).getTaxa().get(0) + ":" + tree.get(i).getHeight();			
				newickFormat = "(" + newickFormat + ")";
			}
		}				
		return newickFormat;
	}
	
	
	private static void printCat2Trees(ArrayList<String> trees, String filename){
		if(trees == null || trees.isEmpty()){
			return;
		}		
		String[] treePath = new String[trees.size()];
		for(int i = 0; i<trees.size(); i++){
			treePath[i] = trees.get(i);
		}
		printToFile(treePath, filename);
		
		
	}



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
			}else if(right.get(rightIndex).getHeight() < left.get(leftIndex).getHeight()){
				tree.set(treeIndex,  right.get(rightIndex));
				rightIndex++;
			} else { //they are of equal height;
				if(left.get(leftIndex).getTaxa().size() <= right.get(rightIndex).getTaxa().size()){
					tree.set(treeIndex,  left.get(leftIndex));
					leftIndex++;
				} else {
					tree.set(treeIndex, right.get(rightIndex));
					rightIndex++;
				}
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
