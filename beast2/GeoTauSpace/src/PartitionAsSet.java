import beast.evolution.tree.Tree;

import java.util.HashSet;

/**
 * Created by alex on 21/10/14.
 */
public class PartitionAsSet {

    private HashSet partition;

    public Partition () {

    }

    public Partition (Tree tree) {

        createPartition(tree);

    }

    public HashSet getPartition() {
        if (partition == null) {
            // do something
        }
        return partition;
    }

    private void createPartition(Tree tree) {
        HashSet newPartition = new HashSet();
        // here is the code to create a partition from the tree and store it to the field partition
        partition = newPartition;
    }

    public HashSet getElement(String taxon) {
        HashSet element = new HashSet();
        // code to create the Element newSet.add();
        return element;
    }

    // Tree tree = ...;

    //PartitionAsSet partition1 = new Partition(tree);

    //HashSet myNewElement = partition1.getElement("cat");
}
