package proj1.lsmtree.impl;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:15
 *@Title  :
 */

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.SetCommand;
import proj1.lsmtree.model.InsertCommand;
/**
 * Memtable class that uses a skip list for storing key-value pairs.
 * This implementation uses ConcurrentSkipListMap for simplicity and thread-safety,
 * ensuring that operations on the Memtable are safe in a concurrent environment.
 */
public class Memtable {
    // Skip list structure to store commands. It provides efficient search, insert, and delete operations.
    private Map<String, Command> skipList;

    /**
     * Constructor to initialize the Memtable with a ConcurrentSkipListMap.
     */
    public Memtable() {
        this.skipList = new ConcurrentSkipListMap<>();
    }

    /**
     * Returns a map view of the Memtable's data.
     *
     * @return A map containing all key-command pairs stored in the Memtable.
     */
    public Map<String, Command> getRawData() {
        return skipList;
    }

    /**
     * Stores or updates a key-value pair in the Memtable using a SetCommand.
     *
     * @param setCommand The SetCommand containing the key-value pair to be stored or updated.
     */
    public void set(SetCommand setCommand){
        skipList.put(setCommand.getKey(), setCommand);
    }

    /**
     * Stores a delete command in the Memtable, which effectively marks a key for deletion.
     *
     * @param deleteCommand The DelCommand containing the key to be marked for deletion.
     */
    public void del(DelCommand deleteCommand){
        skipList.put(deleteCommand.getKey(), deleteCommand);
    }

    /**
     * Attempts to insert a new key-value pair into the Memtable if the key does not already exist.
     *
     * @param insertCommand The InsertCommand containing the key-value pair to be inserted.
     * @return True if the key-value pair was successfully inserted, false if the key already exists.
     */
    public boolean insert(InsertCommand insertCommand) {
        // Use putIfAbsent to ensure the command is only inserted if the key is not already present
        return skipList.putIfAbsent(insertCommand.getKey(), insertCommand) == null;
    }

    /**
     * Searches for and returns a command associated with a given key in the Memtable.
     *
     * @param key The key for which to search.
     * @return The command associated with the key if found, or null if the key is not present.
     */
    public Command search(String key) {
        return skipList.get(key); // Returns the command or null if the key is not found
    }

    /**
     * Returns the number of key-command pairs in the Memtable.
     *
     * @return The size of the Memtable.
     */
    public int size() {
        return skipList.size();
    }

    /**
     * Checks whether the Memtable is empty.
     *
     * @return True if the Memtable is empty, false otherwise.
     */
    public boolean isEmpty() {
        return skipList.isEmpty();
    }
}
