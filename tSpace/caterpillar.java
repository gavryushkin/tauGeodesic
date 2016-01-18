package tTree;
import java.util.*;
public class caterpillar {

	public static void main(String[] args) {
		//Send arrays to make trees, compare trees, output tree along the path at time, time;
		
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
