package proj1.SkipList;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/13 22:58
 *@Title  :
 */


import java.util.Stack;


// SkipList implementation supporting generic key-value pairs
public class SkipList<K extends Comparable<K>, V> {


  private Node head;
  private double prob; // Probability factor
  private int nodeCounter; // Counter to keep track of the number of nodes

  public SkipList(double prob) {
    this.prob = prob; // Probability factor for determining level promotion
    // Use a minimum key value at the head for comparison
    this.head = new Node(null, null);
  }
  // Searches for a key in the SkipList, with an option to print the search path
  public boolean search(K key,boolean printPath) {
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

      if (p.key != null && p.key.compareTo(key) == 0) {
        found = true;
        break; // Key found
      }

      if (p.next == null || p.next.key.compareTo(key) > 0) {
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
  // Deletes a node with the specified key from the SkipList
//  public void delete(K key) {
//    Node p = head;
//    while (p != null) {
//      if (p.next == null || p.next.key.compareTo(key) > 0) {
//        p = p.down;
//      } else if (p.next.key.compareTo(key) == 0) {
//        p.next = p.next.next; // Remove the node
//        p = p.down;
//      } else {
//        p = p.next;
//      }
//    }
//  }
  // Inserts a new key-value pair into the SkipList
  public void insert(K key, V value) {
    if(search(key,false))
      return;
    // Path stack to track potential insertion points
    Stack<Node> potentialStack = new Stack<>();
    Node current = head;

    // Traverse down the skip list to locate the correct insertion point
    while (current != null){
      if(current.next == null || current.next.key.compareTo(key) > 0){
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
        return;
    }

    // Consider adding a new top level for the skip list
    double num = Math.random();
    if(num < this.prob){
      Node temp = new Node(key,value);
      Node minNode = new Node(null,null);
      minNode.down = head;
      minNode.next = temp;
      temp.down = down;
      head = minNode;
    }
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
}
