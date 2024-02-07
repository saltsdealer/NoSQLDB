package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/5 21:49
 *@Title  :
 */

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;

/**
 * Represents a B-Tree, a self-balancing tree data structure that maintains sorted data in a way that
 * allows for efficient insertion, deletion, and search operations. B-Trees are optimized for systems
 * that read and write large blocks of data. They are commonly used in databases and file systems.
 */
public class BTree {

  // The order of the B-Tree, determining the range of children per node.
  private final int m;

  // The minimum number of keys that a node (except the root) must have.
  private final int min;

  // The root node of the B-Tree, which may be null if the tree is empty.
  private Node root;

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
  public Entry searchEntry(int key) {
    return searchEntry(root, key);
  }

  /**
   * searches for an entry with the specified key, starting from a given node.
   *
   * @param node The current node in the B-Tree being searched.
   * @param key The key of the entry to search for.
   * @return The entry with the specified key if it is found within this subtree, otherwise {@code null}.
   */
  private Entry searchEntry(Node node, int key) {
    while (node != null) {
      List<Entry> entries = node.getEntries(); // Store entries in a local variable to avoid multiple calls.
      int index = Collections.binarySearch(entries, new Entry(key, null));

      if (index >= 0) {
        // Key found in the current node's entries.
        return entries.get(index);
      } else {
        // Key not found, proceed to the appropriate child node.
        int childIndex = -(index + 1); // Calculate the child index to search next.
        if (childIndex < node.getChildNodes().size()) {
          node = node.getChildNodes().get(childIndex); // Move to the child node and continue the loop.
        } else {
          // No more children to search, key not found.
          return null;
        }
      }
    }
    return null;
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
    int index = Collections.binarySearch(node.getEntries(), new Entry(key, null), Comparator.comparingInt(e -> e.key));
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
  public void insert(Entry entry) {
    // If the root is null, create a new node and make it the root
    if (root == null) {
      Node node = new Node();
      node.add(entry); // Assuming 'add' method adds an entry to the node
      root = node;
    } else {
      // Start the recursive insertion process from the root
      insert(root, entry);
    }
  }

  /**
   * Recursively inserts an entry into the B-tree, starting from the given node.
   * Handles splitting the node if it's full after insertion.
   *
   * @param node The node from which to start the insertion process.
   * @param entry The entry to insert into the B-tree.
   */
  private void insert(Node node, Entry entry) {
    // If the node is a leaf (has no children)
    if (node.getChildNodes().isEmpty()) {
      // Add the entry to the node
      node.getEntries().add(entry); // Assume 'getEntries()' returns the list of entries in the node
      Collections.sort(node.getEntries(), Comparator.comparingInt(e -> e.key)); // Ensure entries are sorted after insertion

      // If the node exceeds the maximum number of entries, split the node
      if (node.getEntries().size() > m - 1) {
        split(node); // Assume 'split' method handles splitting the node
      }
    } else {
      // The node is not a leaf, find the correct child node to insert the entry
      int index = Collections.binarySearch(node.getEntries(), entry, Comparator.comparingInt(e -> e.key));
      if (index < 0) {
        // Entry is not present; find the child node where the entry should be inserted
        insert(node.getChildNodes().get(-index - 1), entry);
      }
      // If entry is found in the node, this part of the code could handle updates or duplicates based on your B-tree's policy
    }
  }

  /**
   * Splits the given node into two nodes at the median entry. The median entry is moved up to the node's parent.
   * If the node is the root, a new root is created.
   *
   * @param node The node to be split.
   */
  private void split(Node node) {
    // right node to promote
    //int midIndex = node.getEntries().size() / 2; // Determine the median entry's index
    // left node to promote
    int midIndex = (node.getEntries().size() - 1) / 2;
    // Create two new nodes to hold the entries before and after the median
    Node leftNode = new Node();
    Node rightNode = new Node();

    // Distribute the entries into the new nodes
    leftNode.getEntries().addAll(node.getEntries().subList(0, midIndex));
    rightNode.getEntries().addAll(node.getEntries().subList(midIndex + 1, node.getEntries().size()));

    // Handle the child nodes if this is not a leaf node
    if (!node.getChildNodes().isEmpty()) {
      // Distribute the child nodes into the new nodes
      leftNode.getChildNodes().addAll(node.getChildNodes().subList(0, midIndex + 1));
      rightNode.getChildNodes().addAll(node.getChildNodes().subList(midIndex + 1, node.getChildNodes().size()));

      // Update parent references for the child nodes
      leftNode.getChildNodes().forEach(child -> child.setParentNode(leftNode));
      rightNode.getChildNodes().forEach(child -> child.setParentNode(rightNode));
    }

    // Handle the case where the node being split is the root
    if (node.getParentNode() == null) {
      // Create a new root node and add the median entry
      Node newRoot = new Node();
      newRoot.getEntries().add(node.getEntries().get(midIndex));
      newRoot.getChildNodes().add(leftNode);
      newRoot.getChildNodes().add(rightNode);

      // Update the root reference and parent references for the new nodes
      root = newRoot;
      leftNode.setParentNode(root);
      rightNode.setParentNode(root);
    } else {
      // If the node is not the root, insert the median entry into the parent node
      Node parentNode = node.getParentNode();
      parentNode.getEntries().add(node.getEntries().get(midIndex));
      Collections.sort(parentNode.getEntries(), Comparator.comparingInt(e -> e.key)); // Ensure parent entries are sorted

      // Replace the original node with the two new nodes in the parent's children list
      int nodeIndexInParent = parentNode.getChildNodes().indexOf(node);
      parentNode.getChildNodes().remove(nodeIndexInParent);
      parentNode.getChildNodes().add(nodeIndexInParent, rightNode);
      parentNode.getChildNodes().add(nodeIndexInParent, leftNode);

      // Update parent references for the new nodes
      leftNode.setParentNode(parentNode);
      rightNode.setParentNode(parentNode);

      // Check if the parent node needs to be split
      if (parentNode.getEntries().size() > m - 1) {
        split(parentNode);
      }
    }
  }


  @Override
  public String toString() {
    if (root == null) {
      return "Empty B-Tree";
    }

    StringBuilder builder = new StringBuilder();
    Queue<Node> queue = new LinkedList<>();
    HashMap<Node, String> nodeIdentifiers = new HashMap<>();
    int nodeId = 0;

    // Start with the root node
    queue.offer(root);
    nodeIdentifiers.put(root, "N" + nodeId++);

    while (!queue.isEmpty()) {
      int levelSize = queue.size();
      for (int i = 0; i < levelSize; i++) {
        Node currentNode = queue.poll();
        assert currentNode != null;

        // Print current node's entries and a pointer to child nodes
        builder.append(nodeIdentifiers.get(currentNode)).append(": [");
        StringJoiner joiner = new StringJoiner(", ");
        for (Entry entry : currentNode.getEntries()) {
          joiner.add(String.valueOf(entry.key));
        }
        builder.append(joiner.toString()).append("]");

        // Append pointers to child nodes
        if (!currentNode.getChildNodes().isEmpty()) {
          builder.append(" -> {");
          StringJoiner childJoiner = new StringJoiner(", ");
          for (Node child : currentNode.getChildNodes()) {
            String childIdentifier = "N" + nodeId++;
            nodeIdentifiers.put(child, childIdentifier);
            childJoiner.add(childIdentifier);
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

}