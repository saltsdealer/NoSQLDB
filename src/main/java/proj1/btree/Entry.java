package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/5 21:16
 *@Title  :
 */

/**
 * Represents an entry in a B-Tree node. Each entry contains a key and a value.
 * The key is used for sorting and searching within the B-Tree structure, while the value
 * holds the associated data.
 */
public class Entry implements Comparable<Entry>{

    // The key associated with this entry. Used for ordering within the B-tree.
    final int key;

    // The value associated with this entry. Represents the data stored in the B-tree.
    String value;

    /**
     * Constructs a new Entry with the specified key and value.
     *
     * @param key The key associated with the entry.
     * @param value The value associated with the entry.
     */
    public Entry(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Retrieves the key of this entry.
     *
     * @return The key of this entry.
     */
    public int getKey() {
        return key;
    }

    /**
     * Retrieves the value of this entry.
     *
     * @return The value of this entry.
     */
    public String getValue() {
        return value;
    }

    /**
     * Compares this entry with another entry based on their keys.
     * This method is used to maintain order within the B-Tree nodes.
     *
     * @param o The entry to compare this entry against.
     * @return A negative integer, zero, or a positive integer as this entry's key
     *         is less than, equal to, or greater than the specified entry's key.
     */
    @Override
    public int compareTo(Entry o) {
        return Integer.compare(key, o.getKey());
    }

    @Override
    public String toString() {
        return "Entry{" +
            "key=" + key +
            ", value='" + value + '\'' +
            '}';
    }
}