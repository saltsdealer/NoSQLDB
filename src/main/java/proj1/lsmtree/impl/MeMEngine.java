// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;



import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.ini4j.Ini;
import proj1.SkipList.SkipList;
import proj1.btree.BTree;
import proj1.btree.PackedBTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.SetCommand;

public class MeMEngine {

  private IMTable writable;
  private IMTable readOnly;
  private ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();
  private String CONFIG_FILE_PATH = "config.ini";
  private int triggerSize;
  private List<String> files;
  private String dbName;
  private String indexName;
  private int counter = 0;
  private int KVNums = 0;
  private List<Integer> kvs = new ArrayList<>();

  public MeMEngine() throws IOException {
    this.writable = new SkipList(0.5);
    this.files = new ArrayList<>();
    loadConfig();
  }

  public MeMEngine(String notmeaningful) throws IOException {
    this.writable = new BTree(10);
    this.readOnly = new BTree(10);
    this.files = new ArrayList<>();
    loadConfig();
  }

  public String loadConfig() throws IOException {
    try (InputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + CONFIG_FILE_PATH);
      }

      Ini ini = new Ini(inputStream);

      // Read configuration values directly
      int MAX_FILE_SIZE = ini.get("Settings", "MAX_FILE_SIZE", int.class);
      int HEAD_BLOCK_SIZE = ini.get("Settings", "HEAD_BLOCK_SIZE", int.class);
      double multiplier = ini.get("Settings","MULTIPLIER",double.class);
      this.triggerSize = (int) ((MAX_FILE_SIZE - HEAD_BLOCK_SIZE) * multiplier);
      return "1";
    } catch (IOException e) {
      e.printStackTrace();
      return ("Failed to load config: " + e.getMessage());
    }
  }

  public boolean set(String key, String value) {

    try {

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
      writable.del(command);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  // btree
  public boolean put(int type, String key, String value,String dbName,String indexName) {

    try {
      if (writable.getSize() >= triggerSize) {
        System.out.println("triggered, flushing ---- at " + triggerSize);
        switchIndex(type,dbName,indexName);
      }
      InsertCommand command = new InsertCommand(key, value);
      writable.insert(command);

    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    }
    return true;
  }

  // skiplist
  public boolean put(Command c,String dbName,String indexName) {

    try {
      if (writable.getSize() >= triggerSize) {
        System.out.println("triggered, flushing ----");
        switchIndex(dbName,indexName);
      }

      writable.insert(c);

    } catch (Exception exception) {
      exception.printStackTrace();
      return false;
    }
    return true;
  }

  public Command get(String key) {
    try {
      if(writable.size() == 0 || Integer.parseInt(key) <
          Integer.parseInt(writable.get().getKey())){
        return null;
      }

      Command command = writable.search(key);
      if (command != null ) {
        return command;
      }
      if(readOnly!=null){
        command = readOnly.search(key);
        if (command != null) {
          return command;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }


  private void switchIndex(int type, String dbName, String indexName) {
    try {
      readOnly = writable; // Make the current writable memtable read-only
      writable = new BTree(10); // Create a new writable memtable

      PackedBTree temp = new PackedBTree( ((BTree)readOnly));
      String fileName = dbName + "_" +  counter + ".db";
      counter += 1;
      this.kvs.add(readOnly.size());
      this.files.add(fileName);
      this.KVNums += readOnly.size();
      SSTableList ss = new SSTableList();
      ss.bulkWrite(temp,fileName,indexName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void switchIndex(String dbName,String indexName) {
    try {
      readOnly = writable; // Make the current writable memtable read-only
      writable = new SkipList(0.5); // Create a new writable memtable
      String fileName = dbName + "_" +  counter + ".db";
      counter += 1;
      this.kvs.add(readOnly.size());
      this.files.add(fileName);
      this.KVNums += readOnly.size();
      SSTableList ss = new SSTableList();
      ss.bulkWrite(readOnly,fileName,indexName);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<String> getFiles() {
    return files;
  }

  public void flush(String filename,String indexName) throws IOException {
    if (writable.getSize() > 0 && writable.size() > 0) {
      this.files.add(filename);
      SSTableList ss = new SSTableList();
      ss.bulkWrite(writable, filename, indexName);
      this.KVNums += writable.size();
      this.kvs.add(writable.size());
      System.out.println("Flushed : " + writable.size());
      writable = new SkipList(0.5);
      readOnly = new SkipList(0.5);
    }else {
      System.out.println("Nothing to flush");
    }
  }

  public void flush(int type, String filename,String indexName) throws IOException {
    if (writable.getSize() > 0 && writable.size() > 0) {
      PackedBTree temp = new PackedBTree( ((BTree)writable));
      this.files.add(filename);
      SSTableList ss = new SSTableList();
      ss.bulkWrite(temp, filename, indexName);
      this.KVNums += writable.size();
      this.kvs.add(writable.size());
      System.out.println("Flushed : " + writable.size());
      writable = new SkipList(0.5);
      readOnly = new SkipList(0.5);
    }else {
      System.out.println("Nothing to flush");
    }
  }
  public IMTable getWritable() {
    return writable;
  }

  public void setWritable(IMTable writable) {
    this.writable = writable;
  }


  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public int getKVNums() {
    return KVNums;
  }

  public static String getCurrentTimestamp() {
    // Get the current date and time
    LocalDateTime now = LocalDateTime.now();

    // Define the desired format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Format the current date and time
    String formattedTimestamp = now.format(formatter);

    return formattedTimestamp;
  }

  public List<Integer> getKvs() {
    return kvs;
  }
}
