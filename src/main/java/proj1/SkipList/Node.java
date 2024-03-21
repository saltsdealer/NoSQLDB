// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.SkipList;

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;

public class Node <K extends Comparable<K>, V>{
    protected K key;
    protected V val;
    protected Command command;
    protected Node next;
    protected Node down;
    int number = 0;

    protected Node(K key, V value) {
        this.key = key;
        this.val = value;
        this.command = new InsertCommand((String) key, (String) value);
        this.next = this.down = null;
    }

    protected Node(Command c) {
        this.key = (K) c.getKey();
        this.val = (V) c.getValue();
        this.command = c;
        this.next = this.down = null;
    }

    public Command getCommand() {
        return command;
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

    public void setKey(K key) {
        this.key = key;
    }

    public void setVal(V val) {
        this.val = val;
    }
}
