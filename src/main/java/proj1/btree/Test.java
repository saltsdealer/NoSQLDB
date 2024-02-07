package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/5 23:35
 *@Title  :
 */

public class Test {

    public static void main(String[] args) {
        int[] item = {1, 2, 3, 4, 5, 6, 7};
        BTree tree = new BTree(4);
        for (int i : item) {
            tree.insert(new Entry(i, "-->" + i));
        }
        System.out.println("----------------------printing the tree");
        System.out.println(tree.toString());
        //System.out.println(tree.searchEntry(1).toString());
        System.out.println(tree.searchNode(2).toString());
        System.out.println(tree.searchNode(3).toString());
    }
}
