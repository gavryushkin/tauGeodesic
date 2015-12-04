package tTree;
import java.util.*;
public class caterpillar {

	public static void main(String[] args) {
		//Send arrays to make trees, compare trees, output tree along the path at time, time;
		
		//Doesn't work when cherry moves - in progress;
		String[] t1string = {"1","2","3","4","5"};
		double[] t1height = {1,2,3,4,5};    
		String[] t2string = {"1","4","5","3","2"};
		double[] t2height = {1,2,3,4,5};
		double time = 0.5;
		ArrayList<tNode> t1 = tNode.generateTree(t1string, t1height);
		ArrayList<tNode> t2 = tNode.generateTree(t2string, t2height);
		System.out.println(t1);
		System.out.println(t2);
		ArrayList<tNode> tdiff = tNode.difference(t1,t2,time);		
		ArrayList<tNode> treeAtTime = tNode.sortAndGenerateTree(tdiff);		
		
		System.out.println();
		//Shows change from t1 to t2 over increments of time;
		for(double i=0; i <= 1; i+=0.2){
			ArrayList<tNode> k = tNode.difference(t1, t2, i);
			ArrayList<tNode> z = tNode.sortAndGenerateTree(k);
			System.out.println(z);
		}
	}
}
