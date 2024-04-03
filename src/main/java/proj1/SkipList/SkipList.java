// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.SkipList;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SetCommand;


// SkipList implementation supporting generic key-value pairs
public class SkipList implements IMTable{


  private Node head;
  private double prob; // Probability factor
  private int nodeCounter = 0; // Counter to keep track of the number of nodes
  private int size = 0;


  public SkipList(double prob) {
    this.prob = prob; // Probability factor for determining level promotion
    // Use a minimum key value at the head for comparison
    this.head = new Node(null, null);

  }
  // Searches for a key in the SkipList, with an option to print the search path
  public boolean search(String key,boolean printPath) {
    Node p = head;
    StringBuilder path = new StringBuilder();
    boolean found = false;
    // Iterate down through levels starting from the topmost level
    while (p != null) {
      if (p.key != null) {
        // Append the current node's key to the path only if printing is enabled
        if (printPath && path.length() > 0) path.append(" -> ");
        if (printPath) path.append(p.key);
      }

      if (p.key != null && p.compareTo(key) == 0) {
        found = true;
        break; // Key found
      }

      if (p.next == null || p.next.compareTo(key) > 0) {
        // Move down a level if the next key is greater than the target or there is no next node
        p = p.down;
      } else {
        // Move to the next node if the next key is less than or equal to the target
        p = p.next;
      }
    }

    // Print the search path
    if (printPath) {
      if (found) {
        System.out.println("Search path: " + path);
      } else {
        System.out.println("Key not found.");
      }
    }

    return found;
  }


  public Command search(String key) {
    Node p = head;
    // Iterate down through levels starting from the topmost level
    while (p != null) {
      // Check if the current node's key matches the search key
      if (p.key != null && p.compareTo(key) == 0) {
        return p.getCommand(); // Key found, return the node
      }

      // Determine the direction of the search:
      // Move down if at the end of the level or if the next key is greater than the search key
      if (p.next == null || (p.next.key != null && p.next.compareTo(key) > 0)) {
        p = p.down;
      } else {
        // Move to the next node if the next key is less than or equal to the search key
        p = p.next;
      }
    }

    // Key not found, return null
    return null;
  }

  public Node search(String key, String n) {
    Node p = head;
    // Iterate down through levels starting from the topmost level
    while (p != null) {
      // Check if the current node's key matches the search key
      if (p.key != null && p.compareTo(key) == 0) {
        return p; // Key found, return the node
      }

      // Determine the direction of the search:
      // Move down if at the end of the level or if the next key is greater than the search key
      if (p.next == null || (p.next.key != null && p.next.compareTo(key) > 0)) {
        p = p.down;
      } else {
        // Move to the next node if the next key is less than or equal to the search key
        p = p.next;
      }
    }

    // Key not found, return null
    return null;
  }

  // Deletes a node with the specified key from the SkipList
  @Override
  public void del(DelCommand d) {
    String key = d.getKey();
    Node p = head;
    while (p != null) {
      if (p.next == null || p.next.compareTo(key) > 0) {
        p = p.down;
      } else if (p.next.compareTo(key) == 0) {
        p.next = p.next.next; // Remove the node
        p = p.down;
      } else {
        p = p.next;
      }
    }
  }
  // Inserts a new key-value pair into the SkipList
  @Override
  public boolean insert(Command insertCommand) {
    String key = insertCommand.getKey();
    String value = insertCommand.getValue();

    int length = insertCommand.toBytes().length;
    size += length;
    nodeCounter += 1;

    Node node = search(key,"");
    if (node != null) {
      if (!node.getVal().equals(value)) {

        System.out.println("Duplicate key Detected at " + node.key + ", value replaced by the newer insert");
        node.setVal(value);
        node.getCommand().setValue(value);
        nodeCounter -= 1;
        size -= node.getCommand().toBytes().length;
        return false;
      }
    }

    try{
      // Path stack to track potential insertion points
      Stack<Node> potentialStack = new Stack<>();
      Node current = head;

      // Traverse down the skip list to locate the correct insertion point
      while (current != null){
        if(current.next == null || current.next.compareTo(key) > 0){
          potentialStack.add(current);
          current = current.down;
        }else current = current.next;
      }
      Node down = null;
      boolean flag = true;

      // Insert the new node at the identified levels
      while(!potentialStack.isEmpty()){
        Node previous = potentialStack.pop();
        double num = Math.random();

        // Perform insertion if it's the first insertion or the random number is below the threshold
        if(flag || num < this.prob){
          flag = false;
          Node temp = new Node(key, value);

          if(previous.next == null) previous.next = temp;
          else{
            temp.next = previous.next;
            previous.next = temp;
          }
          temp.down = down;
          down = temp;

        }else
          return true;
      }

      // Consider adding a new top level for the skip list
      double num = Math.random();
      if(num < this.prob){
        Node temp = new Node( key,value);
        Node minNode = new Node(null,null);
        minNode.down = head;
        minNode.next = temp;
        temp.down = down;
        head = minNode;
        //nodeCounter += 2;
      }
      return true;
    } catch (Exception e) {
      System.out.println(e);
      return false;
    }

  }

  @Override
  public void set(SetCommand setCommand) {
    try {
      Command c = search(setCommand.getKey());
      if (c!= null){
        c.setValue(setCommand.getValue());
      } else {
        throw new NullPointerException();
      }
    } catch (Exception e){
      System.out.println("Failed, Node Not Exists");
    }

  }

  // getting the first node
  @Override
  public Command get() {
    Node current = head;

    // Traverse down to the bottom level
    while (current.down != null) {
      current = current.down;
    }

    // Move right at the bottom level to find the first node with a non-null value
    while (current.next != null && current.next.val == null) {
      current = current.next;
    }

    // Return the first node with a non-null value at the bottom level, or the dummy head if all are null
    return current.next.getCommand();
  }

  // getting the last node
  public Command getPenultimate() {
    Node current = head;

    // Traverse down to the bottom level
    while (current.down != null) {
      current = current.down;
    }

    // Move right at the bottom level to reach the last non-null node
    while (current.next != null && current.next.next != null) {
      current = current.next;
    }

    // At this point, 'current' should be the node before the last node
    // Check if 'current' is not the dummy head itself
    if (current != head) {
      return current.getCommand(); // Return the command of the penultimate node
    } else {
      return null; // If 'current' is still the dummy head, it means the list is empty or has only one node
    }
  }

  public int size(){
    return nodeCounter;
  }


  @Override
  public Object getRawData() {
    Node current = head;
// Move to the lowest level
    while (current.down != null) {
      current = current.down;
    }
    return current;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Node p = head;
    while (p != null) {
      sb.append("head");
      Node node = p.next;
      while (node != null) {
        sb.append("-->(").append(node.key).append(", ").append(node.val).append(")");
        node = node.next;
      }
      sb.append("-->NULL\n"); // Add a newline for each level for better readability
      p = p.down;
    }
    return sb.toString();
  }

  public Iterator<Node> iterator() {
    return new SkipListIterator();
  }

  // Nested Iterator class
  private class SkipListIterator implements Iterator<Node> {
    private Node current;

    // Constructor
    public SkipListIterator() {
      // Initialize the current pointer to the start of the bottom level
      this.current = head;
      // Move to the bottom level
      while (current.down != null) {
        current = current.down;
      }
    }

    // Check if the next element exists
    @Override
    public boolean hasNext() {
      return current.next != null;
    }

    // Move to the next element and return it
    public Node next() {
      current = current.next; // Move to the next node
      return current; // Return the value of the current node
    }


  }

  public int getSize() {
    return size;
  }

  public int getSizeBytes() {
    Iterator<Node> iterator = this.iterator();
    int size = 0;
    while (iterator.hasNext()) {
      Node currentNode = iterator.next(); // Get the current node
      size += currentNode.getCommand().toBytes().length;

    }
    return size;
  }

  public static SkipList rebuild(List<List<Command>> data){
    SkipList sl = new SkipList(0.5);
    for (List<Command> blocks : data){
      for (Command c : blocks){
        sl.insert(c);
      }
    }
    return sl;
  }

  public int getNodeCounter() {
    return nodeCounter;
  }
}
