// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import proj1.lsmtree.model.InsertCommand;

public class LSMEngineTree {
    private MTableBTree writable;
    private MTableBTree readOnly;
    private int storeThreshold;
    private int m;

    public LSMEngineTree(int storeThreshold, int m) {
        this.storeThreshold = storeThreshold;
        this.m = m;
        this.writable = new MTableBTree(m);
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
    public boolean insert(String key, String value) {

        try {
            if (writable.sizeEntries() >= storeThreshold) {
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
    private void switchIndex() {
        try {
            readOnly = writable; // Make the current writable memtable read-only
            this.writable = new MTableBTree(m);// Create a new writable memtable
            // this is where sstable should be triggered to disk
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
