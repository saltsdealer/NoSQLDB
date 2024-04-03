// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SearchCommand;
import proj1.lsmtree.model.SetCommand;

/**
 * Represents a B-Tree, a self-balancing tree data structure that maintains sorted data in a way that
 * allows for efficient insertion, deletion, and search operations. B-Trees are optimized for systems
 * that read and write large blocks of data. They are commonly used in databases and file systems.
 */
public class BTree implements IMTable {

  // The order of the B-Tree, determining the range of children per node.
  private final int m;

  // The minimum number of keys that a node (except the root) must have.
  private final int min;

  // The root node of the B-Tree, which may be null if the tree is empty.
  private Node root;

  private int currentId;

  private int size = 0;

  private Command firstEntry;


  /**
   * Initializes a B-Tree with a specified order. The order of the tree defines the maximum number
   * of children that each node can have.
   *
   * @param m The order of the B-Tree, must be greater than 2 for a valid B-Tree structure.
   */
  public BTree(int m) {
    if (m <= 2) {
      throw new IllegalArgumentException("B-Tree order must be greater than 2");
    }
    this.m = m;
    // Calculate minimum number of keys in a node
    this.min = (int) Math.ceil(m / 2.0) - 1;


  }

  /**
   * Retrieves the root node of the B-Tree. The root node is the topmost node in the tree structure
   * and the entry point for most B-Tree operations.
   *
   * @return The root node of the B-Tree, which may be null if the tree is empty.
   */
  public Node getRoot() {
    return root;
  }

  // Additional methods for B-Tree operations like insertion, deletion, and search might go here.
  /**
   * Initiates a search for an entry with a specific key in the B-Tree starting from the root node.
   *
   * @param key The key of the entry to search for.
   * @return The entry with the specified key if found, otherwise {@code null}.
   */

  public Command search(String key) {
    return searchEntry(root, Integer.parseInt(key),"");
  }

  /**
   * searches for an entry with the specified key, starting from a given node.
   *
   * @param node The current node in the B-Tree being searched.
   * @param key The key of the entry to search for.
   * @return The entry with the specified key if it is found within this subtree, otherwise {@code null}.
   */
  private Command searchEntry(Node node, int key, String path) {
    // Base case: when the node is null, meaning the search has reached a leaf without finding the key
    if (node == null) {
      //System.out.println("No Key Found");
      return null;
    }

    int index = Collections.binarySearch(node.getEntries(), new SearchCommand(String.valueOf(key)));
    if (index >= 0) {
      // Key found, construct the path and print it
      String currentPath = path + node.getNumber(); // Append the current node's number to the path
      System.out.println("Key found at path: " + currentPath);
      return node.getEntries().get(index);
    } else {
      // Key not found in the current node, proceed to the appropriate child node if any
      if (node.getChildNodes().isEmpty()) {
        // If there are no child nodes and the key wasn't found, it means the search has ended
        // "No Key Found" should be printed after the recursion ends, not here
        return null;
      }
      // Update the path for the next level of recursion
      String nextPath = path + node.getNumber() + " -> ";
      // Recur to the next child node that could contain the key
      return searchEntry(node.getChildNodes().get(-index - 1), key, nextPath);
    }
  }


  /**
   * Initiates a search for a node containing the specified key, starting from the root node.
   * This method serves as a public interface for the search functionality.
   *
   * @param key The key to search for within the B-tree.
   * @return The node containing the specified key, or {@code null} if the key is not found.
   */
  public Node searchNode(int key) {
    return searchNode(root, key); // Assuming 'root' is a field representing the root node of the B-tree
  }

  /**
   * Recursively searches for a node containing the specified key, starting from the given node.
   * This method employs binary search on the entries of each node and recursive descent into child nodes if necessary.
   *
   * @param node The node from which to start the search.
   * @param key The key to search for within the B-tree starting from the given node.
   * @return The node containing the specified key, or {@code null} if the key is not found in the subtree rooted at the given node.
   */
  private Node searchNode(Node node, int key) {
    if (node == null) {
      return null; // Base case: node is null, search path ends
    }
    // Perform a binary search among the entries of the current node
    int index = Collections.binarySearch(node.getEntries(), new SearchCommand(Integer.toString(key)),
        Comparator.comparingInt(e -> Integer.parseInt(e.getKey())));
    if (index >= 0) {
      // Key found in the current node's entries
      return node;
    } else {
      // Key not found in the current node's entries, proceed to search in the child nodes
      if (node.getChildNodes().isEmpty()) {
        // Current node is a leaf, end of search path
        return null;
      }
      // Calculate the child node index to search next and recursively search in that child node
      int childIndex = -index - 1;
      return searchNode(node.getChildNodes().get(childIndex), key);
    }
  }

  /**
   * Inserts a new entry into the B-tree. If the root is null, a new node is created as the root.
   * Otherwise, the insertion is delegated to a private method that handles it recursively.
   *
   * @param entry The entry to insert into the B-tree. The entry contains a key-value pair.
   */
  @Override
  public boolean insert(Command entry) {
    if (firstEntry == null) firstEntry = new InsertCommand(entry.getKey(), entry.getValue());
    Command c = search(entry.getKey());
    int length = entry.toBytes().length;
    size += length;

    if (c != null){
      System.out.println("Duplicate key Detected at " + c.getKey() + " , value replaced by the newer insert");
      size -= c.toBytes().length;
      c.setValue(entry.getValue());
      return true;
    }

    // If the root is null, create a new node and make it the root
    if (root == null) {
      Node node = new Node();
      node.add(entry); // Assuming 'add' method adds an entry to the node
      root = node;
      root.setNumber(0);
      this.currentId = 0;
      return true;
    } else {
      // Start the recursive insertion process from the root
      return insert(root, entry);
    }
  }

  /**
   * Recursively inserts an entry into the B-tree, starting from the given node.
   * Handles splitting the node if it's full after insertion.
   *
   * @param node The node from which to start the insertion process.
   * @param entry The entry to insert into the B-tree.
   */
  private boolean insert(Node node, Command entry) {
    boolean insertedSuccessfully = false;
    // If the node is a leaf (has no children)
    if (node.getChildNodes().isEmpty()) {
      // Add the entry to the node
      node.getEntries().add(entry); // Assume 'getEntries()' returns the list of entries in the node
      Collections.sort(node.getEntries(), Comparator.comparingInt(e -> Integer.parseInt(e.getKey()))); // Ensure entries are sorted after insertion
      insertedSuccessfully = true;
      // If the node exceeds the maximum number of entries, split the node
      if (node.getEntries().size() > m - 1) {
        split(node); // Assume 'split' method handles splitting the node
      }
    } else {
      // The node is not a leaf, find the correct child node to insert the entry
      int index = Collections.binarySearch(node.getEntries(), entry, Comparator.comparingInt(e ->Integer.parseInt(e.getKey())));
      if (index < 0) {
        // Entry is not present; find the child node where the entry should be inserted
        insertedSuccessfully = insert(node.getChildNodes().get(-index - 1), entry);
      }
      // If entry is found in the node, this part of the code could handle updates or duplicates based on your B-tree's policy
    }
    return insertedSuccessfully;
  }

  /**
   * Splits the given node into two nodes at the median entry. The median entry is moved up to the node's parent.
   * If the node is the root, a new root is created.
   *
   * @param node The node to be split.
   */
  private void split(Node node) {
    // right
    // int midIndex = node.getEntries().size() / 2;
    // left
    int midIndex = (node.getEntries().size()-1) / 2;
    Node leftNode = new Node();
    Node rightNode = new Node();

    leftNode.getEntries().addAll(node.getEntries().subList(0, midIndex));
    rightNode.getEntries().addAll(node.getEntries().subList(midIndex + 1, node.getEntries().size()));

    if (!node.getChildNodes().isEmpty()) {
      leftNode.getChildNodes().addAll(node.getChildNodes().subList(0, midIndex + 1));
      rightNode.getChildNodes().addAll(node.getChildNodes().subList(midIndex + 1, node.getChildNodes().size()));

      leftNode.getChildNodes().forEach(child -> child.setParentNode(leftNode));
      rightNode.getChildNodes().forEach(child -> child.setParentNode(rightNode));

    }
    // left split should be the orginial node's id
    // right split should be the new node
    leftNode.setNumber(node.getNumber());
    rightNode.setNumber(++this.currentId);
    // Assign numbers to nodes right before they are added to the tree structure.

    if (node.getParentNode() == null) {
      Node newRoot = new Node();
      newRoot.setNumber(++this.currentId);
      newRoot.getEntries().add(node.getEntries().get(midIndex));
      newRoot.getChildNodes().add(leftNode);
      newRoot.getChildNodes().add(rightNode);

      root = newRoot;
      leftNode.setParentNode(root);
      rightNode.setParentNode(root);

      // Assign number to newRoot only when it's confirmed to be part of the tree.
      newRoot.setNumber(rightNode.getNumber()+1);
    } else {
      Node parentNode = node.getParentNode();
      parentNode.getEntries().add(node.getEntries().get(midIndex));
      Collections.sort(parentNode.getEntries(), Comparator.comparingInt(e -> Integer.parseInt(e.getKey())));

      int nodeIndexInParent = parentNode.getChildNodes().indexOf(node);
      parentNode.getChildNodes().remove(nodeIndexInParent);
      parentNode.getChildNodes().add(nodeIndexInParent, rightNode);
      parentNode.getChildNodes().add(nodeIndexInParent, leftNode);

      leftNode.setParentNode(parentNode);
      rightNode.setParentNode(parentNode);

      if (parentNode.getEntries().size() > m - 1) {
        split(parentNode);
      }
    }
  }



  /**
   * Counts the number of nodes and entries in the B-Tree starting from the root node.
   *
   * @return An array where the first element is the total number of nodes and the second element is the total number of entries in the B-Tree.
   */
  public int size() {
    TreeCounts counts = new TreeCounts();
    countNodesAndEntries(root, counts);
    return counts.entries;
  }

  /**
   * Recursively counts the number of nodes and entries in the subtree rooted at the given node, updating the provided TreeCounts object.
   *
   * @param node The current node from which to count the number of nodes and entries in its subtree.
   * @param counts The TreeCounts object that holds the current counts of nodes and entries.
   */
  private void countNodesAndEntries(Node node, TreeCounts counts) {
    if (node != null) {
      // Increment node count for the current node
      counts.incrementNodes();

      // Add the number of entries in the current node to the entries count
      counts.addEntries(node.getEntries().size());

      // Recursively count nodes and entries for each child
      for (Node child : node.getChildNodes()) {
        countNodesAndEntries(child, counts);
      }
    }
  }

  // Method to start the process of setting node IDs from the root
//  public void setNodeIds() {
//// Reset currentId to 0 every time setNodeIds is called
//    setNodeIds(root);
//  }
//
//  // Recursive helper method to set node IDs without needing a return value or arguments
//  private void setNodeIds(Node node) {
//    if (node == null) {
//      return;
//    }
//
//    // Set the current node's ID and increment currentId for the next node
//    node.setNumber(currentId++);
//
//    // Recursively set IDs for child nodes
//    for (Node child : node.getChildNodes()) {
//      setNodeIds(child);
//    }
//  }

  public int getSize() {
    return size;
  }



  @Override
  public String toString() {
    if (root == null) {
      return "Empty B-Tree";
    }

    StringBuilder builder = new StringBuilder();
    Queue<Node> queue = new LinkedList<>();

    // Start with the root node
    queue.offer(root);

    int l = 0;

    while (!queue.isEmpty()) {

      int levelSize = queue.size();
      builder.append("L-"+ l++ + ": ");
      for (int i = 0; i < levelSize; i++) {
        Node currentNode = queue.poll();
        assert currentNode != null;

        // Use the nodeId for the current node
        builder.append("N").append(currentNode.getNumber()).append(": [");

        StringJoiner joiner = new StringJoiner(", ");
        for (Command entry : currentNode.getEntries()) {
          joiner.add(entry.toString());
        }
        builder.append(joiner.toString()).append("]");

        // Append pointers to child nodes
        if (!currentNode.getChildNodes().isEmpty()) {
          builder.append(" -> {");
          StringJoiner childJoiner = new StringJoiner(", ");
          for (Node child : currentNode.getChildNodes()) {
            childJoiner.add("N" + child.getNumber());
            queue.offer(child); // Add child node to the queue for processing
          }
          builder.append(childJoiner.toString()).append("}");
        }

        if (i < levelSize - 1) {
          builder.append(" | "); // Separator between nodes at the same level
        }
      }
      builder.append("\n"); // New line after each level
    }

    return builder.toString();
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

  @Override
  public void del(DelCommand deleteCommand) {

  }

  // Method to loop through all nodes and sort commands by key
  public List<Command> getSortedCommands() {
    List<Command> allCommands = new ArrayList<>();
    collectCommands(root, allCommands);
    allCommands.sort((c1, c2) -> Integer.compare(Integer.parseInt(c1.getKey()), Integer.parseInt(c2.getKey())));
    return allCommands;
  }

  // Helper method to recursively collect commands from each node
  private void collectCommands(Node node, List<Command> allCommands) {
    if (node == null) {
      return;
    }

    // Add all commands from the current node
    allCommands.addAll(node.getEntries());

    // Recurse for all the child nodes
    for (Node child : node.getChildNodes()) {
      collectCommands(child, allCommands);
    }
  }


  @Override
  public Command get() {
    return firstEntry;
  }


  @Override
  public Object getRawData() {
    return null;
  }


  @Override
  public void forEach(Consumer action) {
  }

  @Override
  public Spliterator spliterator() {
    return null;
  }

  // a helper class
  private static class TreeCounts {
    int nodes = 0;
    int entries = 0;

    // Increment node count
    void incrementNodes() {
      nodes++;
    }

    // Increment entries count by the number of entries in a node
    void addEntries(int entriesCount) {
      entries += entriesCount;
    }
  }


  public class BTreeIterator implements Iterator<Command> {
    private Stack<Node> stack = new Stack<>();
    private Node currentNode;
    private int commandIndex;

    public BTreeIterator(Node root) {
      this.currentNode = findLeftMostLeaf(root);
      this.commandIndex = 0;
    }

    private Node findLeftMostLeaf(Node node) {
      while (!node.getChildNodes().isEmpty()) {
        stack.push(node);
        node = node.getChildNodes().get(0);
      }
      return node;
    }

    @Override
    public boolean hasNext() {
      // Check if the current node or the stack has more elements
      return currentNode != null && (commandIndex < currentNode.getEntries().size() || !stack.isEmpty());
    }

    @Override
    public Command next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      Command nextCommand = null;

      if (commandIndex < currentNode.getEntries().size()) {
        nextCommand = currentNode.getEntries().get(commandIndex++);
      }

      // Move to the next node if we have visited all commands in the current node
      if (commandIndex >= currentNode.getEntries().size()) {
        if (!stack.isEmpty()) {
          Node parent = stack.peek();
          int childIndex = parent.getChildNodes().indexOf(currentNode) + 1;

          // Move to the next sibling if exists
          if (childIndex < parent.getChildNodes().size()) {
            currentNode = findLeftMostLeaf(parent.getChildNodes().get(childIndex));
            commandIndex = 0;
          } else {
            // If no more siblings, pop the stack and set the parent as the current node
            currentNode = stack.pop();
            commandIndex = childIndex - 1; // Move to the command after the last child
          }
        } else {
          // If the stack is empty, we are done
          currentNode = null;
        }
      }

      return nextCommand;
    }
  }

  // Other BTree methods here

  // Method to get the iterator
  @Override
  public Iterator<Command> iterator() {
    return new BTreeIterator(root);
  }

}