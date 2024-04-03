// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import static proj1.SkipList.SkipList.rebuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import proj1.SkipList.Node;
import proj1.SkipList.SkipList;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.ISSTable;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.KeyIndex;
import proj1.lsmtree.model.SearchCommand;
import proj1.lsmtree.model.SetCommand;
import org.ini4j.Ini;
import java.io.InputStream;




public class SSTableList implements ISSTable {


  private RandomAccessFile randomAccessFile = null;
  private static int MAX_FILE_SIZE = 1024 * 1024; // 1MB
  private static int BLOCK_SIZE = 256; // 256 bytes per block
  private static int HEAD_BLOCK_SIZE = BLOCK_SIZE; // Head block size, could be more than one block if needed
  private static int MAX_BLOCKS = (MAX_FILE_SIZE - HEAD_BLOCK_SIZE)/ BLOCK_SIZE ; // Maximum number of blocks in a file
  private BitSet freeSpaceBitmap; // Bitmap for managing free space in blocks
  private static int INT_SIZE = Integer.SIZE / Byte.SIZE;
  private List<Integer> usedBlockIndices; // List to store indices of used blocks
  private int usedBlocks; //
  public static String CONFIG_FILE_PATH = "config.ini";
  private static int triggerSize;
  private Scanner scanner = new Scanner(System.in);
  private static File dataDirectory;

  public SSTableList() throws IOException {
    // Initialize the bitmap with all blocks as free initially
    loadConfig();
    //System.out.println(printConfig());
//    this.freeSpaceBitmap = new BitSet(MAX_BLOCKS);
//    this.freeSpaceBitmap.set(0, MAX_BLOCKS - 1, true); // Mark all blocks as free, except for the head block
    this.usedBlockIndices = new ArrayList<>();
  }

  public String printConfig(){
    return  (MAX_FILE_SIZE + " " + BLOCK_SIZE + " " + HEAD_BLOCK_SIZE + " " + MAX_BLOCKS + " " + freeSpaceBitmap);
  }

  public String loadConfig() throws IOException {
    try (InputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + CONFIG_FILE_PATH);
      }

      Ini ini = new Ini(inputStream);

      // Read configuration values directly
      MAX_FILE_SIZE = ini.get("Settings", "MAX_FILE_SIZE", int.class);
      BLOCK_SIZE = ini.get("Settings", "BLOCK_SIZE", int.class);
      HEAD_BLOCK_SIZE = ini.get("Settings", "HEAD_BLOCK_SIZE", int.class);
      double multiplier = ini.get("Settings","MULTIPLIER",double.class);
      //System.out.println(MAX_BLOCKS);
      MAX_BLOCKS = (MAX_FILE_SIZE - HEAD_BLOCK_SIZE)/ BLOCK_SIZE;
      this.freeSpaceBitmap = new BitSet(MAX_BLOCKS);
      this.freeSpaceBitmap.set(0, MAX_BLOCKS - 1, true);
      this.triggerSize = (int) ((MAX_FILE_SIZE - HEAD_BLOCK_SIZE) * multiplier );
      return "1";
    } catch (IOException e) {
      e.printStackTrace();
      return ("Failed to load config: " + e.getMessage());
    }
  }

  public String bulkWrite(IMTable memTable, String fileName, String indexName){
    String first = write(memTable,fileName);
    writeIndexToFile(memTable,fileName,indexName);
    return null;
  }

  public String write(IMTable memTable, String fileName) {
    List<Command> cs = new ArrayList<>();
    Command c; // The first command of all data

    // Assuming memTable.get(null) is a valid way to obtain the first command for both BTree and SkipList
    c = memTable.get();
    cs.add(c); // Adding the very first data

    System.out.println("Writing to file: " + fileName);
    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "rw")) {
      file.setLength(MAX_FILE_SIZE); // Set file size to 1 MB
      List<ByteBuffer> buffersToWrite = new ArrayList<>();

      // Initialize buffer with BLOCK_SIZE - INT_SIZE to leave space for metadata
      ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE - INT_SIZE);
      byte[] commandBytes;
      Node node = null;
      // Assuming memTable can be iterated over to get all commands
      for (Object obj : memTable) {
        if (obj instanceof Command){
          commandBytes = ((InsertCommand) obj).toBytes();
        } else {
          node = (Node) obj; // Casting required to access the Node type
          commandBytes = node.getCommand().toBytes();
        //System.out.println("Processing command: " + node.getCommand());
        }
        // Check if buffer is full before adding new command
        if (buffer.position() + commandBytes.length > buffer.limit()) {
          int blockIndex = findNextFreeBlock();
          if (blockIndex == -1) {
            throw new RuntimeException("Too many data.");
          }

          //System.out.println("Buffer full. Preparing to write to block index: " + blockIndex);
          buffersToWrite.add(prepareBufferForFile(buffer, blockIndex));

          buffer = ByteBuffer.allocate(BLOCK_SIZE - INT_SIZE);

          if (obj instanceof InsertCommand){

            c = (Command) obj;
          } else {
            c = node.getCommand();
          }// Store the first command of the new block
          cs.add(c);
        }
        buffer.put(commandBytes);
      }

      // Write any remaining data in the buffer to a new block
      if (buffer.position() > 0) {
        int blockIndex = findNextFreeBlock();
        if (blockIndex != -1) {
          System.out.println("Writing remaining data to block index: " + blockIndex);
          buffersToWrite.add(prepareBufferForFile(buffer, blockIndex));
        }
      }

      // Prepare head block with metadata
      ByteBuffer headBlock = prepareHeadBlock(cs, fileName);

      // Write all buffers to file in a single operation
      writeFile(file, headBlock, buffersToWrite);
      return "1";
    } catch (Exception e) {
      e.printStackTrace();
      return ("Error during file write: " + e.getMessage());
    }
  }

  private int findNextFreeBlock() {
    return freeSpaceBitmap.nextSetBit(0); // Find the next free block index
  }

  private void markBlockAsUsed(int blockIndex) {
    freeSpaceBitmap.clear(blockIndex); // Mark block as used
    usedBlockIndices.add(blockIndex); // Record used block index
  }

  private ByteBuffer prepareBufferForFile(ByteBuffer buffer, int blockIndex) throws IOException {
    ByteBuffer blockBuffer = ByteBuffer.allocate(BLOCK_SIZE);
    blockBuffer.putInt(blockIndex); // Prepend block index as metadata
    // position?
    blockBuffer.put(buffer.array(), 0, buffer.position()); // Add buffer's content
    buffer.clear(); // Clear buffer for reuse
    markBlockAsUsed(blockIndex); // Update block usage tracking
    return blockBuffer;
  }

  private ByteBuffer prepareHeadBlock(List<Command> cs, String fileName) throws IOException {
    // version(int) + filenamelength(int) + filename + usedblockcounts + /usedblockindices/ + keylength(int)
    // + key
    ByteBuffer headBuffer = ByteBuffer.allocate(HEAD_BLOCK_SIZE);
    String time = getCurrentTimestamp();
    headBuffer.putInt(time.getBytes().length);
    headBuffer.put(time.getBytes());
    headBuffer.putInt(fileName.length());
    headBuffer.put(fileName.getBytes());
    headBuffer.putInt(usedBlockIndices.size()); // Number of used blocks
    for (int i = 0; i < usedBlockIndices.size(); i++) {
      //Integer index = usedBlockIndices.get(i); // Get the index at position i
      //headBuffer.putInt(index); // Store the index in the buffer
      String c = cs.get(i).getKey();
      // in case the id turned out not to be integer directly, still, it is 4 bytes
      headBuffer.putInt(c.length());
      headBuffer.put(c.getBytes());
    }
    headBuffer.flip(); // Prepare buffer for writing
    return headBuffer;
  }

  private void writeFile(RandomAccessFile file, ByteBuffer headBlock, List<ByteBuffer> buffers) throws IOException {
    // Move file pointer to the beginning for writing
    file.seek(0);
    // Write head block first
    file.write(headBlock.array());

    // Write each data block
    for (ByteBuffer buffer : buffers) {
      file.write(buffer.array());
    }

    System.out.println("File written with all data and metadata.");
  }

  public void writeIndexToFile(IMTable memTable, String dataFileName, String indexFileName){
    Command c ;
    c = memTable.get();
    //System.out.println(c);
    KeyIndex ki =  new KeyIndex(dataFileName,c.getKey());
    File f = new File(dataDirectory, indexFileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "rw")) {
      file.seek(file.length()); // Move the pointer to the end of the file
      String version = getCurrentTimestamp();
      file.writeInt(version.length());
      file.writeBytes(version);
      file.write(ki.toBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, Object> loadIndexFile(String filePath) {
    Map<String, Object> indexInfo = new HashMap<>();
    List<String> fileNames = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    List<String> ts = new ArrayList<>();
    File file = new File(dataDirectory, filePath);
    try (RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "r")) {
      while (raf.getFilePointer() < raf.length()) {
        int versionL = raf.readInt();
        byte[] versionB = new byte[versionL];
        raf.readFully(versionB);

        String version = new String(versionB);
        // Read the length of the filename
        int fileNameLength = raf.readInt();
        // Read the filename
        byte[] fileNameBytes = new byte[fileNameLength];
        raf.readFully(fileNameBytes);
        String fileName = new String(fileNameBytes);

        // Read the length of the key
        int keyLength = raf.readInt();
        // Read the key
        byte[] keyBytes = new byte[keyLength];
        raf.readFully(keyBytes);
        String key = new String(keyBytes);

        // Add the filename and key to their respective lists
        fileNames.add(fileName);
        keys.add(key);
        ts.add(version);
      }
      indexInfo.put("version",ts);
      indexInfo.put("fileName",fileNames);
      indexInfo.put("FirstKeys",keys);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return indexInfo;
  }

  public static int search(List<String> list, int number) {
    if (number < 0){
      System.out.println("Invalid input");
      return -1;
    }
    if (list.isEmpty() ) {
      return -1;
    }
    if (list.size() == 1 && list.get(0).equals("0")) {
      return 0 ; // Return 0 if searching for 0, otherwise -1
    }
    if (number >= Integer.parseInt(list.get(list.size()-1))){
      return list.size()-1;
    }

    int low = 0;
    int high = list.size() - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midValue = Integer.parseInt(list.get(mid));

      if (midValue == number) {
        return mid; // Or return mid + 1 if you want to insert after duplicates
      } else if (midValue < number) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    if (low < list.size()) {
      return low - 1; // Safe to return if low is within bounds
    } else {
      return -1;
    }
    // At this point, low is the insertion point
  }

  public static int searchFileInsert(List<String> list, int number) {
    if (number < 0){
      System.out.println("Invalid input");
      return -1;
    }
    if (list.isEmpty() ) {
      return -1;
    }
    if (list.size() == 1 && list.get(0).equals("0")) {
      return 0 ; // Return 0 if searching for 0, otherwise -1
    }
    if (number >= Integer.parseInt(list.get(list.size()-1))){
      return list.size()-1;
    }
    if (number < Integer.parseInt(list.get(0))){
      return 0;
    }

    int low = 0;
    int high = list.size() - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midValue = Integer.parseInt(list.get(mid));

      if (midValue == number) {
        return mid; // Or return mid + 1 if you want to insert after duplicates
      } else if (midValue < number) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    if (low < list.size()) {
      return low - 1; // Safe to return if low is within bounds
    } else {
      return -1;
    }
    // At this point, low is the insertion point
  }

  public static Command get(String key, String indexName, SSTableList ss) throws IOException {
     String filename = ss.searchIndex(key,indexName);
     if (filename == null) return null;
     return ss.searchData(key, filename);
  }

  public String searchIndex(String key, String indexfile){
    Map<String, Object> indexs = loadIndexFile(indexfile);
    int idx = search((List<String>) indexs.get("FirstKeys"),Integer.parseInt(key));
    if (idx != -1) {
      System.out.println("Index file visited, Returning File index : " + idx);
    }
    if (idx == -1){
      System.out.println("Filename not found");
      return null;
    }
    return ((List<String>)indexs.get("fileName")).get(idx);
  }


  public Command searchData(String key, String fileName){
    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "r")) {
      Map<String, Object> headblock = readHeadBlock(file);
      List<String> keys = (List<String>) headblock.get("UsedBlockFirstKeys");
      int index = search(keys,Integer.parseInt(key));
      file.seek(index * BLOCK_SIZE + HEAD_BLOCK_SIZE);
      ByteBuffer dataBuffer = ByteBuffer.allocate(BLOCK_SIZE);
      file.read(dataBuffer.array());
      dataBuffer.getInt();
      List<Command> deserializedData = deserializeCommand(dataBuffer);
      for (Command c : deserializedData) {
        if (c.getKey().equals(key)) {
          System.out.println("HeadBlock index Visited, Found in DataBlock : " + index);
          return c;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  public void writeMeta(String dbName,List<String> fileNames, int kvNums, String metaName, Boolean append) {
    // length + dbName + fileNames.size + kvNums + blockSize(int) + filenames(multi)
    // kvnumbs should be added in the top level
    File f = new File(dataDirectory, metaName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "rw")) {
      if (append)
        file.seek(file.length()); // Move the pointer to the end of the file
      String version = getCurrentTimestamp();
      file.writeInt(version.length());
      file.writeBytes(version);
      // Writing the length of dbName
      file.writeInt(dbName.length());
      // Writing the dbName itself
      file.writeBytes(dbName);
      // Writing the totalSize in MB
      file.writeInt(fileNames.size());
      // Writing kvNums
      file.writeInt(kvNums);
      // Writing blockSize (you might choose to use or not use this directly)
      file.writeInt(BLOCK_SIZE);
      // Writing each filename followed by a newline character for separation
      for (String fileName : fileNames) {
        file.writeInt(fileName.length());
        file.writeBytes(fileName); // Using "\n" to separate filenames
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String,Object> readMeta(String metaName) {
    File f = new File(dataDirectory, metaName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "r")) {
      Map<String,Object> map = new HashMap<>();
      Map<String,Object> info = new HashMap<>();
      while (file.getFilePointer() < file.length()) {
        // reading time stamp
        int versionL = file.readInt();
        byte[] versionB = new byte[versionL];
        file.read(versionB);
        String version = new String(versionB);
        // Reading the length of dbName and then the dbName itself
        int dbNameLength = file.readInt();
        byte[] dbNameBytes = new byte[dbNameLength];
        file.read(dbNameBytes);
        String dbName = new String(dbNameBytes);
        // Reading the size of fileNames and kvNums
        int fileNamesSize = file.readInt();
        int kvNums = file.readInt();
        int blockSize = file.readInt();
        // Reading filenames
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < fileNamesSize; i++) {
          int fileNameLength = file.readInt(); // Reading the length of the filename
          byte[] fileNameBytes = new byte[fileNameLength];
          file.read(fileNameBytes);
          String fileName = new String(fileNameBytes);
          fileNames.add(fileName);
        }
        info.put("size",fileNamesSize); info.put("kvNums",kvNums);
        info.put("blocks",blockSize); info.put("fileNames", fileNames);
        info.put("version",version);

        map.put(dbName,info);
        // Print the read data
        printMeta(dbName, fileNames, kvNums, blockSize, version);

      }
      return map;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  // Print Method
  public void printMeta(String dbName, List<String> fileNames, int kvNums, int block, String version) {
    StringBuilder msg = new StringBuilder();
    msg.append("Version: " + version + " " +
        "\nDatabase Name: " + dbName + " " +
        "\nKey-Value Numbers: " + kvNums + " " +
        "\nBlock-Size: " + block + " " +
        "\nFilenames: ");
    for (String fileName : fileNames) {
      msg.append(" - " + fileName);
    }
    System.out.println(msg.toString());
  }

  public Map<String, Object> open(String fileName) throws IOException {
    Map<String, Object> fileInfo = new HashMap<>();
    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "r")) {
      // Read the head block
      fileInfo = readHeadBlock(file);
      // Extracted fileInfo from headBlockInfo
      List<List<Command>> dataBlocksInfo = readDataBlocks(file);
      fileInfo.put("DataBlocksInfo", dataBlocksInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fileInfo;
  }

  public Map<String, Object> readHeadBlock(RandomAccessFile file) throws IOException {
    // head block : version(int) + filenamelength(int) + filename + usedblockcounts + (keylength(int) + key)(multi)
    Map<String, Object> fileInfo = new HashMap<>();
    ByteBuffer headBuffer = ByteBuffer.allocate(HEAD_BLOCK_SIZE);
    file.read(headBuffer.array());
    headBuffer.rewind();

    int tsLength = headBuffer.getInt();
    byte[] ts = new byte[tsLength];
    headBuffer.get(ts);
    fileInfo.put("Version", new String(ts));
    int nameLength = headBuffer.getInt();
    byte[] fileNameByte = new byte[nameLength];
    headBuffer.get(fileNameByte);
    int usedBlocksCount = headBuffer.getInt();
    fileInfo.put("FileName", new String(fileNameByte));
    fileInfo.put("UsedBlocksCount", usedBlocksCount);

    // Extract indices of used blocks
    //List<Integer> usedBlockIndices = new ArrayList<>();
    List<String> usedBlockFirstKeys = new ArrayList<>();
    for (int i = 0; i < usedBlocksCount; i++) {
      //int index = headBuffer.getInt();
      int stringLength = headBuffer.getInt();
      byte[] stringBytes = new byte[stringLength];
      headBuffer.get(stringBytes);
      //usedBlockIndices.add(index);
      usedBlockFirstKeys.add(new String(stringBytes));
    }
    fileInfo.put("UsedBlockFirstKeys", usedBlockFirstKeys);
    //fileInfo.put("UsedBlockIndices", usedBlockIndices);
    this.usedBlocks = usedBlocksCount;
    return fileInfo; // Return the fileInfo map
  }

  public List<List<Command>> readDataBlocks(RandomAccessFile file) throws IOException {
    List<List<Command>> dataBlocksInfo = new ArrayList<>();
    for (int i = 0; i < usedBlocks; i++) { // use the class field directly
      file.seek((long) i * BLOCK_SIZE + HEAD_BLOCK_SIZE); // Position the file pointer at the start of the data block
      ByteBuffer dataBuffer = ByteBuffer.allocate(BLOCK_SIZE);
      file.read(dataBuffer.array());
      dataBuffer.getInt();
      // Deserialize the data block using deserializeCommand method
      List<Command> deserializedData = deserializeCommand(dataBuffer);
      // Add the deserialized data of this block to the list of all data blocks' info
      dataBlocksInfo.add(deserializedData);
    }
    return dataBlocksInfo;
  }

  public Map<String, Object> load(String fileName) throws IOException {
    // head block : version(int) + filenamelength(int) + filename + usedblockcounts + usedblockindices(multi)
    // + (keylength(int) + key)(multi)
    Map<String, Object> fileInfo = new HashMap<>();
    ArrayList<List<Command>> dataBlocksInfo = new ArrayList<>();
    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "r")) {
      // Read the head block
      ByteBuffer headBuffer = ByteBuffer.allocate(HEAD_BLOCK_SIZE);
      file.read(headBuffer.array());
      headBuffer.rewind();

      // Extract version and used blocks count from the head block
      int version = headBuffer.getInt();

      fileInfo.put("Version", version);
      int nameLength = headBuffer.getInt();
      byte[] fileNameByte = new byte[nameLength];
      headBuffer.get(fileNameByte);
      int usedBlocksCount = headBuffer.getInt();
      fileInfo.put("FileName", new String(fileNameByte));
      fileInfo.put("UsedBlocksCount", usedBlocksCount);

      // Extract indices of used blocks
      List<Integer> usedBlockIndices = new ArrayList<>();
      List<String> usedBlockFirstKeys = new ArrayList<>();
      for (int i = 0; i < usedBlocksCount; i++) {
        int index = headBuffer.getInt();
        int stringLength = headBuffer.getInt();
        byte[] stringBytes = new byte[stringLength];
        headBuffer.get(stringBytes);
        usedBlockIndices.add(index);
        usedBlockFirstKeys.add(new String(stringBytes));
      }
      fileInfo.put("UsedBlockFirstKeys", usedBlockFirstKeys);
      fileInfo.put("UsedBlockIndices", usedBlockIndices);

      // Iterate through each used block index
      for (int index : usedBlockIndices) {
        //System.out.println("Working on block " + index) ;

        file.seek((long) index * BLOCK_SIZE + HEAD_BLOCK_SIZE); // Position the file pointer at the start of the data block

        ByteBuffer dataBuffer = ByteBuffer.allocate(BLOCK_SIZE);

        file.read(dataBuffer.array());

        dataBuffer.getInt();


        // Deserialize the data block using deserializeCommand method
        List<Command> deserializedData = deserializeCommand(dataBuffer);

        // Add the deserialized data of this block to the list of all data blocks' info
        dataBlocksInfo.add(deserializedData);
      }

      // Add the data blocks' information list to the fileInfo map
      fileInfo.put("DataBlocksInfo", dataBlocksInfo);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return fileInfo;
  }

  public static List<Command> deserializeCommand(ByteBuffer buffer) {

    List commands = new ArrayList<>();
    if (buffer.array().length != BLOCK_SIZE){
      return null;
    }


    while (buffer.hasRemaining()) {
      // Deserialize key-length
      if (buffer.remaining() < Integer.BYTES) break;

      int keyLength = buffer.getInt();

      if (keyLength <= 0 || keyLength > buffer.remaining() - 2 * Integer.BYTES) break;

      // Deserialize key-value
      byte[] keyBytes = new byte[keyLength];
      buffer.get(keyBytes);
      String key = new String(keyBytes);

      if (buffer.remaining() < 2 * Integer.BYTES) break;
      //System.out.println(key);
      // Deserialize cmd
      int cmd = buffer.getInt();
      if (cmd == -1) continue;

      // Deserialize value-length
      int valueLength = buffer.getInt();
      if (valueLength < 0 || valueLength > buffer.remaining()) break;
      // Deserialize value
      byte[] valueBytes = new byte[valueLength];
      buffer.get(valueBytes);
      String value = new String(valueBytes);
      //System.out.println(value);
      switch (cmd) {
        case 1:
          commands.add(new InsertCommand(key, value));
          break; // Prevents fall-through
        case 0:
          commands.add(new SetCommand(key, value));
          break; // Prevents fall-through
        case 2:
          commands.add(new SearchCommand(key));
          break; // Prevents fall-through
        default:
          // Optionally handle unexpected cmd values
          break;
      }
      // Print deserialized InsertCommand
    }
    return commands;
  }

  public void close() {
    if (randomAccessFile != null) {
      try {
        randomAccessFile.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void printByteBufferAsBinary(ByteBuffer buffer) {
    // Reset the position of the buffer to the beginning to read all data
    buffer.rewind();

    while (buffer.hasRemaining()) {
      byte b = buffer.get();

      // Convert the byte to an int while avoiding sign extension
      int value = b & 0xFF;

      // Convert the int to a binary string
      String binaryString = Integer.toBinaryString(value);

      // Ensure the binary string is 8 bits long, padding with leading zeros if necessary
      String formattedBinaryString = String.format("%8s", binaryString).replace(' ', '0');

      System.out.print(formattedBinaryString + " ");
    }

    // Reset the position of the buffer to its original state after reading
    buffer.rewind();

    System.out.println(); // Print a newline at the end for readability
  }

  public static void setConfigFilePath(String configFilePath) {
    CONFIG_FILE_PATH = configFilePath;
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

  public int getTriggerSize() {
    return triggerSize;
  }

  public static void del(String key, String idxName, File directory) throws IOException {
    // get del put set 这四个方法需要是静态的，put 和 set 直接仿写这个方法就好
    SSTableList ss = new SSTableList();
    ss.setDirectory(directory);
    Map<String, Object> info = ss.delBasic(key,idxName,"delete",null);
    if (info == null) return;
    SkipList sl = ss.reConstruct((List<List<Command>>) info.get("data"));
    ss.write( sl, (String) info.get("fileName"));
  }

  public static void set(String key, String idxName, String value, File directory) throws IOException {
    // get del put set 这四个方法需要是静态的，put 和 set 直接仿写这个方法就好
    SSTableList ss = new SSTableList();
    ss.setDirectory(directory);
    Map<String, Object> info = ss.delBasic(key,idxName,"set",value);
    if (info == null) return;
    SkipList sl = ss.reConstruct((List<List<Command>>) info.get("data"));
    ss.write( sl, (String) info.get("fileName"));
  }

  public static boolean put(String key, String value, String idxName, File directory)
      throws IOException {
    SSTableList ss = new SSTableList();
    ss.setDirectory(directory);;
    Map<String, Object> info = ss.delBasic(key,idxName,"insert",value);
    SkipList sl = ss.reConstruct((List<List<Command>>) info.get("data"));
    ss.write( sl, (String) info.get("fileName"));
    return (boolean) info.get("isReplaced");
  }


  public SkipList reConstruct(List<List<Command>> data){
    return rebuild(data);
  }
  public Map<String,Object> delBasic(String key, String idxName, String operation, String value) {
    Map<String, Object> info = new HashMap<>();
    String fileName = "";

    if (operation.equals("insert")){
      Map<String, Object> indexs = loadIndexFile(idxName);
      System.out.println(indexs);
      int idx = searchFileInsert((List<String>) indexs.get("FirstKeys"),Integer.parseInt(key));
      fileName = ((List<String>)indexs.get("fileName")).get(idx);
    } else {
      fileName = searchIndex(key, idxName);
    }
    System.out.println("filename + " + fileName);

    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    if (value == null && operation.equals("insert")) return null;

    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "r")) {
      SSTableList ss = new SSTableList();
      Map head = ss.readHeadBlock(file);
      ss.usedBlocks = (int) head.get("UsedBlocksCount");
      int blockIndex = search((List<String>) head.get("UsedBlockFirstKeys"), Integer.parseInt(key));
      System.out.println("blockIndex + " + blockIndex);

      if (!operation.equals("insert") && blockIndex == -1) {
        System.out.println("Data Not Found");
        return null;
      }
      //del and set
      if (!operation.equals("insert")) {
        List<List<Command>> data = ss.readDataBlocks(file);
        List<Command> commands = data.get(blockIndex);
        for (Command c : commands) {
          if (c.getKey().equals(key)) {
            if (c.getValue().equals("null") && operation.equals("delete")){
              System.out.println("Data Has Been Deleted");
              return null;
            }
            ss.handleOperation(c, operation, value);
            break;
          }
        }
        info.put("isReplaced",false);
        info.put("fileName",fileName); info.put("data", data);
      }
      //insert
      boolean flag = false;
      boolean isReplaced = false;
      if (operation.equals("insert")){
        Map<String, Object> headBlock = head;
        List<List<Command>> data = ss.readDataBlocks(file);
        if (blockIndex != -1){
          List<Command> commands = data.get(blockIndex);
          for (Command c : commands) {
            if (c.getKey().equals(key)) {
              System.out.println(value);
              isReplaced = promptForValue(c,value);
              flag = true;
              break;
            }
          }
          if (!flag){
            data.get(blockIndex).add(new InsertCommand(key, value));
          }

        }
        info.put("isReplaced",isReplaced);
        info.put("fileName",headBlock.get("FileName")); info.put("data",data);
      }
      return info;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void handleOperation(Command command, String operation, String value) {
    switch (operation) {
      case "delete":
        command.setValue("null");
        break;
      case "set":
        command.setValue(value != null && !value.isEmpty() ? value : scanner.nextLine());
        break;
    }
  }

  private boolean promptForValue(Command command, String value) {
      String response = "";
      while (!response.equalsIgnoreCase("Y") && !response.equalsIgnoreCase("N")) {
        if (!command.getValue().equals("null")){
          System.out.println("Already Data Exists, Replace with new value? (Y/N)");
          response = scanner.nextLine();
          if (response.equalsIgnoreCase("Y")) {
            System.out.println("Setting : ");
            command.setValue(value != null && !value.isEmpty() ? value : scanner.nextLine());
            return true;
          } else if (response.equalsIgnoreCase("N")) {
            System.out.println("Operation Abandoned.");
            break;
          } else {
            System.out.println("Invalid input, please enter 'Y' for Yes or 'N' for No.");
          }
        } else {
          System.out.println("Setting : " + value);
          command.setValue(value != null && !value.isEmpty() ? value : scanner.nextLine());
          break;
        }
      }
    return false;
  }


  public void setDirectory(File directory) {
    this.dataDirectory = directory;
  }

  public void updateIndex(List<String> firstKeys, List<String>  fileNames, List<String> times, String indexFileName) {
    if (firstKeys.size() != fileNames.size()) return;
    List<KeyIndex> kis = new ArrayList<>();
    for (int i = 0 ; i < firstKeys.size(); i++){
      kis.add(new KeyIndex(fileNames.get(i),firstKeys.get(i)));
    }

    File f = new File(dataDirectory, indexFileName);
    try (RandomAccessFile file = new RandomAccessFile(f.getAbsolutePath(), "rw")) {
      for (KeyIndex ki : kis){// Move the pointer to the end of the file
        String version = getCurrentTimestamp();
        file.writeInt(version.length());
        file.writeBytes(version);
        file.write(ki.toBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void clearFileContents(String fileName) throws IOException {
    File f = new File(dataDirectory, fileName);
    try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {
      file.setLength(0);
      file.setLength(MAX_FILE_SIZE);// This will truncate the file to zero length, effectively clearing it
    }
  }
}
