// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree;

import java.util.Map;
import proj1.SkipList.Node;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SetCommand;

public interface IMTable<T> extends Iterable<T>{



  /**
   * Stores or updates a key-value pair in the Memtable using a SetCommand.
   *
   * @param setCommand The SetCommand containing the key-value pair to be stored or updated.
   */
  void set(SetCommand setCommand);

  /**
   * Stores a delete command in the Memtable, which effectively marks a key for deletion.
   *
   * @param deleteCommand The DelCommand containing the key to be marked for deletion.
   */
  void del(DelCommand deleteCommand);

  /**
   * Attempts to insert a new key-value pair into the Memtable if the key does not already exist.
   *
   * @param entry The InsertCommand containing the key-value pair to be inserted.
   * @return True if the key-value pair was successfully inserted, false if the key already exists.
   */
  boolean insert(Command entry);

  /**
   * Returns the first command of the table.
   *
   * @return
   */
  Command get();

  /**
   * Returns the number of kvs in the memtable
   * @return
   */
  int size();

  T getRawData();

  /**
   * Returns the number of bytes for the kv pairs in the Memtable.
   *
   * @return The size of the Memtable.
   */
  int getSize();

  /**
   * Search the keys
   * @param key
   * @return
   */
  Command search(String key);


}
