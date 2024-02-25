// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.SkipList;

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
