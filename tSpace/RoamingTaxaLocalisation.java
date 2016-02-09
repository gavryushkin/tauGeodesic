package tTree;
import java.util.*;

public class RoamingTaxaLocalisation {
	boolean hasRoamer;
	Set<String> localisedRoamer;
	
	public RoamingTaxaLocalisation(){
		hasRoamer = false;
		localisedRoamer = new HashSet<String>();
	}
	public RoamingTaxaLocalisation(boolean hasRoamer, Set<String> localisedRoamer){
		this.hasRoamer = hasRoamer;
		this.localisedRoamer = localisedRoamer;
	}
	public void setHasRoamer(boolean hasRoamer){
		this.hasRoamer = hasRoamer;
	}
	public boolean getHasRoamer(){
		return hasRoamer;
	}
	public void setLocalisedRoamer(Set<String> localisedRoamer){
		this.localisedRoamer = localisedRoamer;
	}
	public Set<String> getLocalisedRoamer(){
		return localisedRoamer;
	}
	
	public RoamingTaxaLocalisation findRoamer(CherryTree tree1, CherryTree tree2){
		ArrayList<tNode> tree1Taxa2Nodes = tree1.getNTaxaNodes(tree1,2);
		ArrayList<tNode> tree2Taxa2Nodes = tree1.getNTaxaNodes(tree2,2);
		RoamingTaxaLocalisation roamer = new RoamingTaxaLocalisation();
		for(tNode n1: tree1Taxa2Nodes){
			ArrayList<String> taxa1 = n1.getTaxa();
			for(tNode n2: tree2Taxa2Nodes){
				ArrayList<String> taxa2 = n2.getTaxa();	
				int height = 2;
				while(hasOneDifference(taxa1, taxa2)){
					height++;
					taxa1 = getNextTaxaList(tree1,taxa1,height);
					taxa2 = getNextTaxaList(tree2,taxa2,height);				
				}				
				if(taxa1.equals(taxa2)){
					System.out.println(tree1);
					System.out.println(tree2);
					Set<String> localisedTaxa = new HashSet<String>();
					localisedTaxa.addAll(taxa1);
					localisedTaxa.addAll(taxa2);
					roamer = new RoamingTaxaLocalisation(true, localisedTaxa);
					simplifyClade(tree1,taxa1);
					simplifyClade(tree2, taxa2);
					
				}	
				if(roamer.getHasRoamer()){
					break;
				}
			}
			if(roamer.getHasRoamer()){
				break;
			}
		}
		return roamer;
	}
	
	private void simplifyClade(CherryTree cherryTree, ArrayList<String> cladeRoot){
		Iterator<tNode> iter = cherryTree.getTree().iterator();
		while (iter.hasNext()){
			tNode n = iter.next();
			if(cladeRoot.containsAll(n.getTaxa()) && n.getTaxa().size() != 1){
				ArrayList<tNode> nodes = new ArrayList<tNode>();
				nodes.add(n);
				cherryTree.getSimplifiedTree(cherryTree, nodes);
				cladeRoot.removeAll(n.getTaxa());
				cladeRoot.add(""+(cherryTree.getMap().size()-1));
				iter = cherryTree.getTree().iterator();
			}
		}			
	}
	//find a taxaList in the cherryTree which is of the right length and contains taxa;
	private ArrayList<String> getNextTaxaList(CherryTree tree, ArrayList<String> taxa, int n){
		ArrayList<String> taxaList  = new ArrayList<String>();
		ArrayList<tNode> treeNodes = tree.getNTaxaNodes(tree, n);
		for(tNode node: treeNodes){
			if(node.getTaxa().containsAll(taxa)){
				taxaList.addAll(node.getTaxa());
			}
		}
		if(taxaList.isEmpty()){
			taxaList.addAll(taxa);
		}		
		return taxaList;
	}
	
	
	//return true, only if there is a single difference between the arrayLists.
	private boolean hasOneDifference(ArrayList<String> taxa1, ArrayList<String> taxa2){
		int difference = Math.abs(taxa1.size() - taxa2.size());
		int minLength = Math.min(taxa1.size(), taxa2.size());
		if (difference >= 2){
			return false; 
		}		
		int matches = 0;
		for(String s1: taxa1){
			for(String s2: taxa2){
				if (s1.equals(s2)){
					matches++;
				}
			}
		}
		boolean oneDifference = false;
		if(taxa1.size() == taxa2.size() && matches == taxa1.size() - 1){
			oneDifference = true;
		}else if(difference == 1 && matches == minLength){
			oneDifference = true;
		}
		return oneDifference;
	}
}
