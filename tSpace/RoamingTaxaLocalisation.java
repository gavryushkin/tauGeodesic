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
	
	public RoamingTaxaLocalisation getRoamer(CherryTree tree1, CherryTree tree2){
		RoamingTaxaLocalisation roamer =  new RoamingTaxaLocalisation();
		//go through tree and return an ArrayList<String> of the smallest node. 
		ArrayList<tNode> prevClades = new ArrayList<tNode>();
		boolean roamerIsNotFound = true;
		ArrayList<String> commonClade = new ArrayList<String>();
		//add a while loop until either a clade is found or we reach some terminal point (tree root)
		while(roamerIsNotFound){
		
		commonClade = findSmallestCommonClade(tree1, tree2, prevClades);
		System.out.println(prevClades);
		System.out.println(commonClade);
		//with this clade: if size = 3
		if(commonClade.isEmpty()){
			
			break;
		}
		
		if(commonClade.size() == 3 ){
			//deal with a size 3 --> can definitely be simplied. Roamer is always common between the two.
			roamer.setHasRoamer(true);
			ArrayList<tNode> t1Nodes = tree1.getNTaxaNodes(tree1,  2);
			ArrayList<tNode> t2Nodes = tree2.getNTaxaNodes(tree2,  2);
			ArrayList<String> t1Taxa = new ArrayList<String>();
			ArrayList<String> t2Taxa = new ArrayList<String>();
			for(tNode n1: t1Nodes){
				if(commonClade.containsAll(n1.getTaxa())){
					t1Taxa.addAll(n1.getTaxa());
				}
			}
			for(tNode n2: t2Nodes){
				if(commonClade.containsAll(n2.getTaxa())){
					t2Taxa.addAll(n2.getTaxa());
				}
			}
			String roamingTaxon = "";
			for(String s1: t1Taxa){
				for(String s2: t2Taxa){
					if(s1.equals(s2)){
						roamingTaxon = s1;
					}
				}
			}
			Set<String> roaming = new HashSet<String>();
			roaming.add(roamingTaxon);
			roamer.setLocalisedRoamer(roaming);
			
		}else{
			//compare subtrees/clades: down all lines where subclades>1
			//send clade and trees away, and get back a set containing the potential 3 taxa or an empty set.
			roamer.setLocalisedRoamer(findRoamingTaxon(tree1.getTree(), tree2.getTree(), commonClade, commonClade));
			System.out.println(roamer.getLocalisedRoamer());
		}
		if(roamer.getLocalisedRoamer() != null){
			roamerIsNotFound = false;
		}
	}
		
		
		if(roamer.getLocalisedRoamer() !=null && !roamer.getLocalisedRoamer().isEmpty()){
			roamer.setHasRoamer(true);
			ArrayList<String> commonClade2 = new ArrayList<String>();
			commonClade2.addAll(commonClade);
			simplifyClade(tree1, commonClade);
			simplifyClade(tree2, commonClade2);
		}
		return roamer;
		
	}
	
	private ArrayList<String> findSmallestCommonClade(CherryTree tree1, CherryTree tree2, ArrayList<tNode> prevClades){
		ArrayList<String> commonClade = new ArrayList<String>();
		int counter = 3;
		while(commonClade.isEmpty()){
			ArrayList<tNode> tree1CurrentClades = tree1.getNTaxaNodes(tree1, counter);
			ArrayList<tNode> tree2CurrentClades = tree2.getNTaxaNodes(tree2, counter);
			for(tNode n1:  tree1CurrentClades){
				ArrayList<String> t1Taxa = n1.getTaxa();
				for(tNode n2: tree2CurrentClades){
					ArrayList<String> t2Taxa = n2.getTaxa();
					if (t1Taxa.containsAll(t2Taxa) && !isContained(t1Taxa, prevClades)){
						//then lets see if this clade can be simplified :), note if this clade can't be, then anything above it can't either...
						commonClade.addAll(t1Taxa);
						prevClades.add(n1);
					}					
				}
			}		
			counter++;
			if (counter >= 10){ //this needs to be modified
				break;
			}
		}		
		return commonClade;
	}
	private boolean isContained(ArrayList<String> taxa, ArrayList<tNode> prevClades){
		boolean isContained = false;
		for(tNode n: prevClades){
			if(n.getTaxa().containsAll(taxa) && n.getTaxa().size() == taxa.size()){
				isContained = true;
			}
		}
		return isContained;
	}
	
	private Set<String> findRoamingTaxon(ArrayList<tNode> tree1, ArrayList<tNode> tree2,
			ArrayList<String> commonClade1, ArrayList<String> commonClade2){
		
		System.out.println(tree1);
		System.out.println(tree2);
		System.out.println(commonClade1);
		System.out.println(commonClade2);
		if(((commonClade1.size() == 3 && commonClade2.size() ==2) || (commonClade1.size() == 2 && commonClade2.size()==3))
				&& (commonClade1.containsAll(commonClade2) || commonClade2.containsAll(commonClade1))){
			Set<String> roamingTaxa = new HashSet<String>();
			roamingTaxa.addAll(commonClade1);
			roamingTaxa.addAll(commonClade2);
			System.out.println("ROAMING TAXON/3: " + roamingTaxa);
			return roamingTaxa;
		}
		
		if(commonClade1.size() == commonClade2.size() && commonClade1.size() == 2){
			Set<String> roamingTaxa = new HashSet<String>();
			roamingTaxa.addAll(commonClade1);
			roamingTaxa.addAll(commonClade2);
			System.out.println("ROAMING TAXON: " + roamingTaxa);
			return roamingTaxa;
		}
		
				
		//find subClade which contains the commonClade and below.
		ArrayList<tNode> clade1 = getSubClade(tree1, commonClade1);
		ArrayList<tNode> clade2 = getSubClade(tree2, commonClade2);
		System.out.println("CLADE1: " +clade1);
		System.out.println("CLADE2: " + clade2);
		//find next two subclades: get largest in clade1 and if commonClade-1 > largest --> find the other, else it's just one.
		
		
		ArrayList<tNode> tree1Clades = new ArrayList<tNode>();
		ArrayList<tNode> tree2Clades = new ArrayList<tNode>();
		if(clade1.size() != 1){		
			tree1Clades = getLargestSubClades(clade1);
		}else{
			tree1Clades.addAll(clade1);
		}
		if(clade2.size() != 1){
			tree2Clades = getLargestSubClades(clade2);
		}else{
			tree2Clades.addAll(clade2);
		}
		System.out.println("TreeClade1: " + tree1Clades);
		System.out.println("TreeClade2: " + tree2Clades);
		//compare the two groups of subClades:
		int minClades = Math.min(tree1Clades.size(), tree2Clades.size());
		System.out.println("minClade: " + minClades);
		int matches = 0;
		
	
		
		for(tNode n1: tree1Clades){
			boolean closeTo = false;
			ArrayList<String> s1 = n1.getTaxa();
			for(tNode n2: tree2Clades){
				ArrayList<String> s2 = n2.getTaxa();
				if((s1.containsAll(s2) || s2.containsAll(s1))  ||  (s1.size() == s2.size() && s1.size() > 2 && hasOneDifference(s1,s2))){
					if(closeTo){
						//problem it matches two of the subClades --> >1 roamer.
						matches = 3; //can only be 1 or 2 matches ever.
						break;
					}else{
						closeTo = true;
						matches++;
					}
				}
			}
		}
		if(tree1Clades.size() == 1 && tree2Clades.size() == 1 &&
				tree1Clades.get(0).getTaxa().size() == 2 && tree2Clades.get(0).getTaxa().size() == 2){
			if(hasOneDifference(tree1Clades.get(0).getTaxa(), tree2Clades.get(0).getTaxa())){
				matches = 1;
			}
		}
		
		
		System.out.println("matches: " + matches);
		if(matches != minClades){
			return null; //theres more than one roamer;
		}
		//else matches == minClades which is great.
		//have two subclades which we want to go down until the end of time
		if(tree1Clades.size() == tree2Clades.size() && tree1Clades.get(0).getTaxa().size() == 2){
			Set<String> roamingTaxa = new HashSet<String>();
			roamingTaxa.addAll(tree1Clades.get(0).getTaxa());
			roamingTaxa.addAll(tree2Clades.get(0).getTaxa());
			System.out.println("ROAMING TAXON: " + roamingTaxa);
			return roamingTaxa;
		}
		
		Set<String> roamer1 = new HashSet<String>();
		Set<String> roamer2 = new HashSet<String>();
		int num = 0;
		
		//match up the subclades:
		for(tNode n1: tree1Clades){
			ArrayList<String> s1 = n1.getTaxa();
			for(tNode n2: tree2Clades){
				if(num > minClades){
					break;
				}
				ArrayList<String> s2 = n2.getTaxa();
				if(s1.containsAll(s2) || s2.containsAll(s1)){
					roamer1 = findRoamingTaxon(tree1, tree2, s1, s2);
					num++;
				} else if(hasOneDifference(s1, s2) && s1.size() == s2.size() && s1.size() > 2){
					if(differenceRemovedNextGen(s1, getNextGenTaxa(s1, s1.size()-1, clade1) , s2,getNextGenTaxa(s2, s2.size()-1, clade2) )){
						roamer1 = findRoamingTaxon(tree1, tree2, s1, s2);
						num++;
					}
				}			
			}
		}
		
		Set<String> roamers = new HashSet<String>();
		if (roamer1 == null || roamer2 == null){
			return null;
		}
		roamers.addAll(roamer1);
		roamers.addAll(roamer2);
		System.out.println("R1: " + roamer1);
		System.out.println("R2: " + roamer2);
		System.out.println("R:  " + roamers);
		
				
		return roamers;
	}
	
	private ArrayList<String> getNextGenTaxa(ArrayList<String> current, int n, ArrayList<tNode> tree){
		ArrayList<String> taxaList = new ArrayList<String>();
		CherryTree treeT = new CherryTree(tree, null);		
		ArrayList<tNode> treeNodes = treeT.getNTaxaNodes(treeT, n);
		
		for(tNode node: treeNodes){
			if(current.containsAll(node.getTaxa())){
				taxaList.addAll(node.getTaxa());
			}
		}
		
		
		return taxaList;
	}
	private boolean differenceRemovedNextGen(ArrayList<String> t1Root, ArrayList<String> t1Next,
			ArrayList<String> t2Root, ArrayList<String> t2Next){
		boolean differenceRemoved = false;
		ArrayList<String> blacklist = new ArrayList<String>();
		
		for(String s1: t1Root){
			if(!t2Root.contains(s1)){
				blacklist.add(s1);
			}
		}
		for(String s2: t2Root){
			if(!t1Root.contains(s2)){
				blacklist.add(s2);
			}
		}
		System.out.println("BlackList: " + blacklist);
		//blacklist size must be 2 otherwise we wouldn't be here.
		if(blacklist.size() != 2){
			return false;
		}
		int count = 0;
		for(String s: blacklist){
			if(t1Next.contains(s)){
				count++;
			}
			if(t2Next.contains(s)){
				count++;
			}
		}
		if(count == 1){
			differenceRemoved = true;
		}	
		
		return differenceRemoved;		
	}
	
	
	private ArrayList<tNode> getLargestSubClades(ArrayList<tNode> clade){
		ArrayList<tNode> subClades = new ArrayList<tNode>();
		//find greatest, then find remainder.
		clade = tNode.mergeSort(clade);
		tNode cladeRoot = clade.get(clade.size()-1);
	
		tNode largest = clade.get(clade.size()-2);
		subClades.add(largest);
		if(largest.getTaxa().size()+1 != cladeRoot.getTaxa().size()){
			//add other clade
			ArrayList<String> taxa = new ArrayList<String>();
			taxa.addAll(cladeRoot.getTaxa());
			taxa.removeAll(largest.getTaxa());
			//find subclade with the same taxa as taxa;
			for(tNode n: clade){
				if(taxa.containsAll(n.getTaxa()) && taxa.size() == n.getTaxa().size()){
					subClades.add(n);
				}
			}		
		}
		
		return subClades;
	}
	private ArrayList<tNode> getSubClade(ArrayList<tNode> tree, ArrayList<String> commonClade){
		ArrayList<tNode> subClade = new ArrayList<tNode>();
		for(tNode n: tree){
			ArrayList<String> taxa = n.getTaxa();
			if(commonClade.containsAll(taxa)){
				subClade.add(n);
			}
		}
		return subClade;
	}
	
	
	
	private void simplifyClade(CherryTree cherryTree, ArrayList<String> cladeRoot){
		//get list of all nodes in clade;
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
	//n = size of desired list
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
			return false; //must be >1 difference
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
