// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.impl.MTableSkipList;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SearchCommand;
import proj1.lsmtree.model.SetCommand;

public class LSMEngineList {

  private MTableSkipList writable;
  private MTableSkipList readOnly;
  private int storeThreshold;

//  private List<ISSTable> issTableList;
//  private

  public LSMEngineList(int storeThreshold) {
    this.storeThreshold = storeThreshold;
    this.writable = new MTableSkipList();
  }

  public boolean set(String key, String value) {

    try {
      if (writable.getRawData().size() >= storeThreshold) {
        switchIndex();
      }
      SetCommand setCommand = new SetCommand(key, value);

      writable.set(setCommand);

    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    }
    return true;
  }

  public void del(String key){
    DelCommand command = new DelCommand(key);
    try {
      if (writable.getRawData().size() >= storeThreshold) {
        switchIndex();
      }
      writable.del(command);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public boolean insert(String key, String value) {

    try {
      if (writable.getRawData().size() >= storeThreshold) {
        switchIndex();
      }
      InsertCommand command = new InsertCommand(key, value);

      writable.insert(command);

    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    }
    return true;
  }

  public String get(String key) {

    try {
      Command command = writable.get(key);
      if (command != null) {
        return command.getValue();
      }
      if(readOnly!=null){
        command = readOnly.get(key);
        if (command != null) {
          return command.getValue();
        }
      }
      /*
      codes to read from files
      */

    } catch (Exception e) {
      e.printStackTrace();
    }


    return null;
  }


  private void switchIndex() {
    try {
      readOnly = writable; // Make the current writable memtable read-only
      writable = new MTableSkipList(); // Create a new writable memtable
      // this is where sstable should be triggered to disk
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
