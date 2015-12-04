package tTree;
/*
 * Node class, contains an arraylist of taxa included;
 * and a double height, the time since divergence;
 * Go through the arraylist of strings for taxa names, compare each to find 1st node where each taxa occurs and that is it's destination height
*/
import java.util.*;
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
	//Generates a Caterpillar Tree from an array of taxa names and their corresponding heights;
	public static ArrayList<tNode> generateTree(String[] taxa, double[] height){
		ArrayList<tNode> tree = new ArrayList<tNode>();			
		ArrayList<String> s = new ArrayList<String>();
		tNode node = null;
		for(int i = 0; i<taxa.length; i++){	
			s = new ArrayList<String>();		
			for(int j=0; j<i+1; j++){
				s.add(taxa[j]);
			}
			node = new tNode(s, height[i]);
			tree.add(node);
		}
		return tree;
	}
	
	//Works out new tree at time between the two trees, New tree isn't sorted and is done in the next method;
	public static ArrayList<tNode> difference(ArrayList<tNode> t1, ArrayList<tNode> t2, double time){
		ArrayList<tNode> treeAtTime = new ArrayList<tNode>();
		ArrayList<tNode> diffTree = new ArrayList<tNode>();			
		String contained = "";
		double diffHeight = 0;	
		double heightTime = 0;
		int j = 0;
		for(tNode n1: t1){
			for(String s1: n1.getTaxa()){
				j=0;
				if(!contained.contains(s1)){
					contained = contained.concat(s1);
					for(tNode n2: t2){
						for(String s2 : n2.getTaxa()){
							if(s1.compareTo(s2)==0){
								diffHeight = n2.getHeight() - n1.getHeight();
								ArrayList<String> diffStr = new ArrayList<String>();
								diffStr.add(s1);
								diffTree.add(new tNode(diffStr, diffHeight));
								j++;								
							}
						}
						if(j!=0){
							j=0;
							break;							
						}
					}
				}
			}
		}	
		//find new tree between t1 t2 using difftree t1 and time.
		
		contained = "";
		String s2;
		for(tNode n1: t1){
			for(String s1: n1.getTaxa()){
				if(!contained.contains(s1)){
					contained = contained.concat(s1);
					for(tNode n2 : diffTree){
						s2 = n2.getTaxa().get(0);
						if(s1.compareTo(s2)==0){
							//find newheight at time, put into new tree 						
							heightTime = n1.getHeight() + n2.getHeight()*time;
							ArrayList<String> str = new ArrayList<String>();
							str.add(s1);
							treeAtTime.add(new tNode(str, heightTime));
						}
					}
				}
			}
		}
		return treeAtTime;
	}
	//Takes tree at new time, sorts it's heights using merge sort, generates final tree;
	public static ArrayList<tNode> sortAndGenerateTree(ArrayList<tNode> tree){
		tree = mergeSort(tree);
		
		String[] taxa = new String[tree.size()];
		double[] height = new double[tree.size()];
		
		for(int i =0; i<tree.size(); i++){
			taxa[i] = tree.get(i).getTaxa().get(0);
			height[i] = tree.get(i).getHeight();
		}		
		ArrayList<tNode> newtree = generateTree(taxa, height);
		
		return newtree;
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
