package tTree;

import java.util.*;
//CherryTree is used to simplify 2 trees

public class CherryTree{
	ArrayList<tNode> tree;
	Map<String, tNode> map;
	
	public CherryTree(ArrayList<tNode> tree, Map<String, tNode> map){
		this.tree = tree;
		this.map = map;		
	}
	public void setMap(Map<String, tNode> map){
		this.map = map;
	}
	public Map<String, tNode> getMap(){
		return map;
	}
	public void setTree(ArrayList<tNode> tree){
		this.tree = tree;
	}
	public ArrayList<tNode> getTree(){
		return tree;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(tree.toString());
		sb.append(map);
		return sb.toString();
	}
	//Simplifies both trees
	public ArrayList<CherryTree> getSimplifiedTrees(CherryTree cherryTree2){
		ArrayList<CherryTree> listOfCherryTrees = new ArrayList<CherryTree>();
		CherryTree cherryTree1 = this;
		System.out.println("Initial CherryTree1: " + cherryTree1);
		System.out.println("Initial CherryTree2: " + cherryTree2);
		while(canBeSimplified(cherryTree1, cherryTree2)){			
			ArrayList<tNode> commonCherries = cherryTree1.getCommonCherries(cherryTree1, cherryTree2);		
			cherryTree1 = getSimplifiedTree(cherryTree1, commonCherries);
			cherryTree2 = getSimplifiedTree(cherryTree2, commonCherries);
			System.out.println();
		}		
		listOfCherryTrees.add(cherryTree1);
		listOfCherryTrees.add(cherryTree2);		
		return listOfCherryTrees;
	}
	
	private CherryTree getSimplifiedTree(CherryTree cherryTree, ArrayList<tNode> commonCherries){	
		for(tNode cherry: commonCherries){
			for(tNode treeNode: cherryTree.getTree()){				
				if(cherry.getTaxa().equals(treeNode.getTaxa())){
					//need some kind of holder/counter for the placeholder in the map.
					ArrayList<String> symbol = new ArrayList<String>();
					String holder = "" + cherryTree.getMap().size();
					symbol.add(holder);
					cherryTree.getMap().put(symbol.get(0), treeNode);
					cherryTree.setTree(simplifyTree(cherryTree, cherry, symbol));
					//replace every instance of the cherry in cherryTree with the symbol;
				}
			}
		}
		
		return cherryTree;
		
	}
	private ArrayList<tNode> simplifyTree(CherryTree cherryTree, tNode cherry, ArrayList<String> symbol){
		ArrayList<String> cherryTaxa = cherry.getTaxa();
		ArrayList<tNode> replacementTree = new ArrayList<tNode>();
		for(tNode n: cherryTree.getTree()){
			ArrayList<String> nodeTaxa = new ArrayList<String>();
			nodeTaxa.addAll(n.getTaxa());
			if (n.getTaxa().size() == 1  &&
					(n.getTaxa().contains(cherryTaxa.get(0)) || n.getTaxa().contains(cherryTaxa.get(1)))){
				//don't do anything
			} else if (n.getTaxa().containsAll(cherryTaxa)){		
				nodeTaxa.removeAll(cherryTaxa);
				nodeTaxa.addAll(symbol);
				replacementTree.add(new tNode(nodeTaxa, n.getHeight()));
			}else{
				replacementTree.add(n);
			}
		}
		return replacementTree;
	}
	
	
	private ArrayList<tNode> getCommonCherries(CherryTree tree1, CherryTree tree2){
		ArrayList<tNode> commonCherries = new ArrayList<tNode>();
		ArrayList<tNode> tree1Nodes = get2TaxaNodes(tree1);
		ArrayList<tNode> tree2Nodes = get2TaxaNodes(tree2);
		for(tNode n1: tree1Nodes){
			ArrayList<String> taxa1 = n1.getTaxa();
			for(tNode n2: tree2Nodes){
				ArrayList<String> taxa2 = n2.getTaxa();
				if(taxa1.equals(taxa2)){
					commonCherries.add(new tNode(taxa1, 0));
				}
			}
		}		
		return commonCherries;
	}
	
	private boolean canBeSimplified(CherryTree tree1, CherryTree tree2){
		ArrayList<tNode> tree1Nodes = get2TaxaNodes(tree1);
		ArrayList<tNode> tree2Nodes = get2TaxaNodes(tree2);
		
		boolean simple = false;
		for(int i = 0; i < tree1Nodes.size(); i++){
			ArrayList<String> taxa1 = tree1Nodes.get(i).getTaxa();
			for(int j = 0; j < tree2Nodes.size(); j++){
				ArrayList<String> taxa2 = tree2Nodes.get(j).getTaxa();				
				if(taxa1.equals(taxa2)){
					simple = true;
				}
			}			
		}	
		return simple;
	}
	private ArrayList<tNode> get2TaxaNodes(CherryTree cherryTree){
		ArrayList<tNode> treeNodes = new ArrayList<tNode>();	
		ArrayList<tNode> tree = cherryTree.getTree();
		for(int i = 0; i < tree.size(); i++){
			if(tree.get(i).getTaxa().size() == 2){
				treeNodes.add(tree.get(i));
			}
		}				
		
		return treeNodes;
	}
	
}
