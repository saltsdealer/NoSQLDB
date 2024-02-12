package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/5 23:35
 *@Title  :
 */

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SearchCommand;

public class Test {

    public static void main(String[] args) {
        BTree bTree = new BTree(3);
        System.out.println("Inserting entries...");
        for (int i = 1; i <= 10; i++) {
            InsertCommand entry = new InsertCommand(Integer.toString(i),"-->" + i);

            System.out.println(bTree.insert(entry));
        }
        System.out.println("\nB-Tree structure:");
        System.out.println(bTree);
        System.out.println(bTree.searchNode(10).getEntries());
        InsertCommand test = new InsertCommand("10",null);
        System.out.println(bTree.insert(test));
        System.out.println(bTree);
    }
}
