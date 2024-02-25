// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import proj1.btree.BTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SetCommand;

public class MTableBTree implements IMTable {

    private BTree bTree;



    public MTableBTree(int m) {
        this.bTree = new BTree(m);
    }

    @Override
    public void set(SetCommand setCommand) {
        // not provided
    }

    @Override
    public void del(DelCommand deleteCommand) {
        // not provided
    }

    @Override
    public boolean insert(InsertCommand insertCommand) {

        return bTree.insert(insertCommand);
    }

    @Override
    public Command get(String key) {
        return bTree.searchEntry(Integer.parseInt(key));
    }

    public Command searchNode(String key) {
        return bTree.searchEntry(Integer.parseInt(key));
    }
    @Override
    public int size() {
        int nodes = bTree.countNodesAndEntries()[0];
        return nodes;
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
