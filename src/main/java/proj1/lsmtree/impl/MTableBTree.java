// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import proj1.btree.BTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SetCommand;

// can be obsoleted
public class MTableBTree {

    private BTree bTree;



    public MTableBTree(int m) {
        this.bTree = new BTree(m);
    }


    public void set(SetCommand setCommand) {
        // not provided
    }


    public void del(DelCommand deleteCommand) {
        // not provided
    }


    public boolean insert(InsertCommand insertCommand) {

        return bTree.insert(insertCommand);
    }


    public Command get(String key) {
        return bTree.searchEntry(Integer.parseInt(key));
    }

    public Command searchNode(String key) {
        return bTree.searchEntry(Integer.parseInt(key));
    }

    public int size() {
        int nodes = bTree.countNodesAndEntries()[0];
        return nodes;
    }


    public BTree getRawData() {
        return bTree;
    }

    public int sizeEntries() {
        int entry = bTree.countNodesAndEntries()[1];
        return entry;
    }


    public boolean isEmpty() {
        return bTree.getRoot()==null;
    }

    @Override
    public String toString() {
        return "MTableBTree{" +
            bTree.toString() +
            '}';
    }

}
