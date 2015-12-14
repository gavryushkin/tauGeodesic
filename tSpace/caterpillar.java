package tTree;
import java.util.*;
public class caterpillar {

	public static void main(String[] args) {
		//Send arrays to make trees, compare trees, output tree along the path at time, time;
		
		//Things that can be modified
		String[] t1string = {"1","2","3","4","5"};
		double[] t1height = {2,2,6,8,10};    
		String[] t2string = {"3","4","1","2","5"};
		double[] t2height = {2,2,6,8,10};
		double time = 0.1;
		boolean iterate = true;
		boolean distances = false;
		boolean absSpeed = false;
		
		String filename = "NewickFormatTrees.txt";
		
		
		boolean treeOk = true;
		while (treeOk) {
			t1height = generateRandomTreeHeights(4);
			t2height = generateRandomTreeHeights(4);
			treeOk = areTreesOk(t1height, t2height);
		}
		
		for(int i = 0; i<4; i++){
			System.out.println("Tree1 : " + t1height[i] + "   Tree2 : " + t2height[i]);
			
		}
		int t1Move = 2; //indicates speed for t1 from stage 1-3 (inc) - possible speeds are 0-3
		int t2Move = 0; //indicates speed of t2 at stage1 (although currently 0 is the only option here);
		int t3Move = 3; //indicates speed of t3 at stage 2-3 (inc) - possible speeds are 0-3
		
		
		double minDistance = 100.0;
		int bestT1 = 0;
		int bestT3 = 0;
		int[] otherT1 = new int[16];
		int[] otherT3 = new int[16];
		double[] otherDistance = new double[16];
		int counter = 0;
		//t1Move = i, t2Move = j
		for(int i = 0; i < 4; i++){			
			for(int j = 0; j<4; j++){
				double dist = tNode.generate(t1string, t1height, t2string, t2height, time,
						iterate, distances, absSpeed, filename, i, t2Move, j);
				if(dist < minDistance){
					minDistance = dist;
					bestT1 = i;
					bestT3 = j;
				} else if (dist == minDistance){
					otherT1[counter] = i;
					otherT3[counter] = j;
					otherDistance[counter] = dist;
					counter++;
				}
			}
		}
		System.out.println("The choice of speeds: " + bestT1 + " and " + bestT3 + " found the shortest path.");
		System.out.println("The distance of this path is: " + minDistance);
		for(int i = 0; i< otherDistance.length; i++){
			if(otherDistance[i] == minDistance){
				System.out.println("Other same speed Choices: " + otherT1[i] + " , " + otherT3[i]);
			}
		}		
	}
	public static double[] generateRandomTreeHeights(int length){
		Random r = new Random();
		double[] treeHeights = new double[length];
		double count = 0;
		for(int i = 1; i < 4 ; i++){
			count += r.nextDouble();
			treeHeights[i] = count;
		}
		treeHeights[0] = treeHeights[1];
		return treeHeights;
	}	
	//checks to see if node of 4 reaches node3 before(1,2) (based on fastest t1 can go)
	public static boolean areTreesOk(double[] tree1, double[] tree2){
		double d1 = tree1[2] - tree1[1];
		double d2 = Math.abs(tree2[3] - tree1[1]);
		double d3 = tree1[3] - tree1[2];
		double d4 = Math.abs(tree1[3] - tree2[1]);
		
		
		//if it returns false the trees are ok -- bad readability
		return d1/d2 < d3/d4;
	}		
	}
}
