// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.SkipList;

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;

public class Node implements Comparable {
    protected String key;
    protected String val;
    protected Command command;
    protected Node next;
    protected Node down;
    int number = 0;

    protected Node(String key, String value)  {
        this.key = key;
        this.val = value;
        this.command = new InsertCommand((String) key, (String) value);
        this.next = this.down = null;
    }

    protected Node(Command c) {
        this.key =  c.getKey();
        this.val =  c.getValue();
        this.command = c;
        this.next = this.down = null;
    }

    public Command getCommand() {
        return command;
    }

    public String getKey() {
        return key;
    }

    public String getVal() {
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

    public void setKey(String key) {
        this.key = key;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public int compareTo(Object o) {
        if (Integer.parseInt(key) == Integer.parseInt((String) o)) return 0;
        return Integer.parseInt(key) > Integer.parseInt((String) o) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "Node{" +
            "key='" + key + '\'' +
            ", val='" + val + '\'' +
            '}';
    }
}
