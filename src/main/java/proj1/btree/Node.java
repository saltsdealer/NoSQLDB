package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/5 21:20
 *@Title  :
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import proj1.lsmtree.impl.Command;

/**
 * Represents a node in a B-Tree structure. Each node contains a list of entries and can have child nodes.
 * Nodes are structured in a hierarchical manner, where each node can point to its child nodes,
 * forming the B-Tree structure.
 */
class Node implements Comparable<Node> {

    // List of entries within the node. Each entry contains a key-value pair.
    // private final List<Entry> entries;
    private final List<Command> entries;

    // List of child nodes. This allows the B-Tree to have a hierarchical structure.
    private final List<Node> childNodes;

    // Reference to the parent node of this node. Root node has a null parent.
    private Node parentNode;

    // node number, not the most useful thing, but for proj demo
    private int nodeId;

    public int getNumber() {
        return nodeId;
    }

    public void setNumber(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Constructs a new, empty Node with no entries and no child nodes.
     */
    public Node() {
        entries = new ArrayList<>();
        childNodes = new ArrayList<>();
        nodeId = 0;
    }

    /**
     * Retrieves the list of entries within this node. Each entry in the list contains a key-value pair.
     *
     * @return A list of {@link Command} objects representing the entries within this node. The returned list
     *         is a direct reference to the internal list, so changes to the returned list will affect the node's
     *         internal state.
     */
    public List<Command> getEntries() {
        return entries;
    }

    /**
     * Retrieves the list of child nodes of this node. Child nodes represent the next level of the tree
     * structure beneath this node.
     *
     * @return A list of {@link Node} objects representing the child nodes of this node. Similar to {@code getEntries()},
     *         the returned list is a direct reference to the internal list, and modifications to it will affect
     *         the node's internal structure.
     */
    public List<Node> getChildNodes() {
        return childNodes;
    }

    /**
     * Retrieves the parent node of this node. The parent node is the node one level above this node
     * in the tree structure. The root node of the tree has a null parent.
     *
     * @return The {@link Node} object representing the parent of this node, or null if this node is the root.
     */
    public Node getParentNode() {
        return parentNode;
    }
    /**
     * Adds an entry to this node. Entries within a node are kept sorted.
     *
     * @param entry The entry to be added to the node.
     * @return The node itself, allowing for method chaining.
     */
    public Node add(Command entry) {
        entries.add(entry);
        Collections.sort(entries); // Ensure entries are sorted after adding a new one.
        return this;
    }

    /**
     * Adds a child node to this node. Child nodes are kept sorted based on their first entry.
     *
     * @param node The child node to be added.
     * @return The node itself, allowing for method chaining.
     */
    public Node addChild(Node node) {
        childNodes.add(node);
        Collections.sort(childNodes); // Ensure child nodes are sorted after adding a new one.
        return this;
    }

    /**
     * Compares this node with another node based on the key of the first entry.
     * This method is used to maintain order among nodes, especially when sorting child nodes.
     *
     * @param o The node to compare this node against.
     * @return A negative integer, zero, or a positive integer as the first entry's key of this node
     *         is less than, equal to, or greater than the first entry's key of the specified node.
     */
    @Override
    public int compareTo(Node o) {

        return Integer.compare(Integer.parseInt(entries.get(0).getKey()), Integer.parseInt(o.getEntries().get(0).getKey()));
    }


    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // Add information about entries in the current node
        builder.append("Node Entries: ");
        for (Command entry : entries) {
            builder.append(entry.getKey()).append(", "); // Using entry's key directly
        }

        // Trim the last comma and space if there are entries
        if (!entries.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }

        // Add information about child nodes' entries
        builder.append("\nChild Nodes' Entries: [");
        for (Node child : childNodes) {
            builder.append("("); // Start of child node's entries
            for (Command entry : child.entries) {
                builder.append(entry.getKey()).append(", "); // Append each entry's key
            }

            // Trim the last comma and space if the child has entries
            if (!child.entries.isEmpty()) {
                builder.setLength(builder.length() - 2); // Remove the last comma and space
            }

            builder.append("), "); // End of this child node's entries and prepare for the next child
        }

        // Trim the last comma and space if there are child nodes
        if (!childNodes.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove the last comma and space
        }

        builder.append("]"); // End of child nodes' entries section

        return builder.toString();
    }

}