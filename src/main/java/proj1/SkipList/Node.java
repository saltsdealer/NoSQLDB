package proj1.SkipList;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/14 11:43
 *@Title  :
 */

public class Node <K extends Comparable<K>, V>{
    protected K key;
    protected V val;
    protected Node next;
    protected Node down;
    int number = 0;

    protected Node(K key, V value) {
        this.key = key;
        this.val = value;
        this.next = this.down = null;
    }

    public K getKey() {
        return key;
    }

    public V getVal() {
        return val;
    }

    public Node getNext() {
        return next;
    }

    public Node getDown() {
        return down;
    }

    public int getNumber() {
        return number;
    }
}
