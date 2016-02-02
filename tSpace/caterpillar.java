package tTree;
import java.util.*;
public class caterpillar {

	public static void main(String[] args) {
		//Send arrays to make trees, compare trees, output tree along the path at time, time;
		//nf = newickFormat
		String nfTree1 = "(((A:1.0,B:1.0):3,C:4):1,((D:1,E:1):2,(F:2,G:2):1):2)";		
		String nfTree2 = "((((A:1,B:1):2,C:3):1,(D:2,E:2):2):1,(F:1,G:1):4)";
		
		TreeFromNewick tree1Newick = new TreeFromNewick(nfTree1);
		ArrayList<tNode> tree1 = tree1Newick.getTree();
		TreeFromNewick tree2Newick = new TreeFromNewick(nfTree2);
		ArrayList<tNode> tree2 = tree2Newick.getTree();
		
		Map<String, tNode> tree1Map = new HashMap<String, tNode>();
		Map<String, tNode> tree2Map = new HashMap<String, tNode>();
		
		CherryTree cherryTree1 = new CherryTree(tree1, tree1Map);
		CherryTree cherryTree2 = new CherryTree(tree2, tree2Map);
		
		cherryTree1.getSimplifiedTrees(cherryTree2);
		
		System.out.println("Final CherryTree1: " + cherryTree1);
		System.out.println("Final CherryTree2: " + cherryTree2);
		System.out.println();
		System.out.println("___________________________________________________________");
		System.out.println();
		//The following is for cat2
		String[] t1LeftStr = {"1","2","3"};
		double[] t1LeftHeight = {2,2,4};
		String[] t1RightStr = {"4","5","6"};
		double[] t1RightHeight = {5,3,3};
		double t1Root = 7;
		
		String[] t2LeftStr = {"2","5","6"};
		double[] t2LeftHeight = {1,6,1};
		String[] t2RightStr = {"1","3","4"};
		double[] t2RightHeight = {3,4,3};
		double t2Root = 7;
		
		//currentTime is at what time one wants the tree, set to >1.0 if the entire path is desired;
		double currentTime = 0.1;
		//findThePath determines if a path is found or just the tree at currentTime, note: if currentTime > 1.0, the path is always returned.
		boolean findThePath = false;
		
		//getStarted finds the path between two trees or the tree at currentTime;		
		System.out.println("This shows the paths between Cat2 trees");
		System.out.println("---------------------------------------");
		System.out.println();
		tNode.getStarted(t1LeftStr, t1LeftHeight, t1RightStr, t1RightHeight, t1Root, t2LeftStr, t2LeftHeight, t2RightStr, t2RightHeight, t2Root, currentTime, findThePath);
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		//for cat1:
		String[] t1Str = {"1","2","3","4"};
		double[] t1Height = {4,4,7,12};
		String[] t2Str = {"1","4","3","2"};
		double[] t2Height = {1,1,3,9};
		
		tNode.getTreesAndPath(t1Str, t1Height, t2Str, t2Height);
	}
}
