// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1;
import proj1.lsmtree.application.Console;
import proj1.btree.BTree;

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SearchCommand;

public class Test {

  public static void main(String[] args) {
    BTree bTree = new BTree(5);
    System.out.println("Inserting entries...");
    int[] test = new int[]{29,41,44,62,46,49,27,76,91,30,100,47,34,53,9,45};
    Console cos = new Console();
    cos.menuBasic(test);

  }
}

