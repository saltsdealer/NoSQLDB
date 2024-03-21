// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import proj1.SkipList.Node;
import proj1.SkipList.SkipList;
import proj1.btree.BTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.ISSTable;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.InsertCommand;
import proj1.lsmtree.model.KeyIndex;
import proj1.lsmtree.model.SearchCommand;
import proj1.lsmtree.model.SetCommand;
import org.ini4j.Ini;
import org.ini4j.Wini;
import java.io.InputStream;
import java.io.IOException;
// index 单独写成文件，在触发刷写机制的时候

public class SSTableList implements ISSTable {

  int commandLength;
  int indexLength;

  private static int version = 1;
  private String fileName;
  private File file = null;
  private RandomAccessFile randomAccessFile = null;
  private int index;
  private LinkedList<KeyIndex> keyIndexs = null;
  private static int MAX_FILE_SIZE = 1024 * 1024; // 1MB
  private static int BLOCK_SIZE = 256; // 256 bytes per block
  private static int HEAD_BLOCK_SIZE = BLOCK_SIZE; // Head block size, could be more than one block if needed
  private static int MAX_BLOCKS = MAX_FILE_SIZE / BLOCK_SIZE; // Maximum number of blocks in a file
  private BitSet freeSpaceBitmap; // Bitmap for managing free space in blocks
  private static int INT_SIZE = Integer.SIZE / Byte.SIZE;
  private List<Integer> usedBlockIndices; // List to store indices of used blocks
  private static String CONFIG_FILE_PATH = "config.ini";


  public SSTableList() {
    // Initialize the bitmap with all blocks as free initially
    this.freeSpaceBitmap = new BitSet(MAX_BLOCKS);
    this.freeSpaceBitmap.set(0, MAX_BLOCKS - 1, true); // Mark all blocks as free, except for the head block
    this.usedBlockIndices = new ArrayList<>();
  }

  public void loadConfig() throws IOException {
    try (InputStream inputStream = SSTableList.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + CONFIG_FILE_PATH);
      }

      Ini ini = new Ini(inputStream);

      // Read configuration values directly
      version = ini.get("Settings", "version", int.class);
      MAX_FILE_SIZE = ini.get("Settings", "MAX_FILE_SIZE", int.class);
      BLOCK_SIZE = ini.get("Settings", "BLOCK_SIZE", int.class);
      HEAD_BLOCK_SIZE = ini.get("Settings", "HEAD_BLOCK_SIZE", int.class);


    } catch (IOException e) {
      System.err.println("Failed to load config: " + e.getMessage());
      e.printStackTrace();
    }
  }


  public void write(IMTable memTable, String fileName) {
    List<Command> cs = new ArrayList<>();
    Command c; // The first command of all data

    // Assuming memTable.get(null) is a valid way to obtain the first command for both BTree and SkipList
    c = memTable.get(null);
    cs.add(c); // Adding the very first data

    System.out.println("Writing to file: " + fileName);
    try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
      file.setLength(MAX_FILE_SIZE); // Set file size to 1 MB
      List<ByteBuffer> buffersToWrite = new ArrayList<>();

      // Initialize buffer with BLOCK_SIZE - INT_SIZE to leave space for metadata
      ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE - INT_SIZE);

      // Assuming memTable can be iterated over to get all commands
      for (Object obj : memTable) {
        Node<?, ?> node = (Node<?, ?>) obj; // Casting required to access the Node type
        byte[] commandBytes = node.getCommand().toBytes();
        System.out.println("Processing command: " + node.getCommand());

        // Check if buffer is full before adding new command
        if (buffer.position() + commandBytes.length > buffer.limit()) {
          int blockIndex = findNextFreeBlock();
          if (blockIndex == -1) {
            throw new RuntimeException("SSTable file is full.");
          }

          System.out.println("Buffer full. Preparing to write to block index: " + blockIndex);
          buffersToWrite.add(prepareBufferForFile(buffer, blockIndex));

          buffer = ByteBuffer.allocate(BLOCK_SIZE - INT_SIZE);
          c = node.getCommand(); // Store the first command of the new block
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
      writeIndexToFile(memTable, fileName, "test_index.idx");
    } catch (Exception e) {
      System.err.println("Error during file write: " + e.getMessage());
      e.printStackTrace();
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
    blockBuffer.put(buffer.array(), 0, buffer.position()); // Add buffer's content
    buffer.clear(); // Clear buffer for reuse
    markBlockAsUsed(blockIndex); // Update block usage tracking
    return blockBuffer;
  }

  private ByteBuffer prepareHeadBlock(List<Command> cs, String fileName) throws IOException {
    // version(int) + filenamelength(int) + filename + usedblockcounts + usedblockindices + keylength(int)
    // + key
    ByteBuffer headBuffer = ByteBuffer.allocate(HEAD_BLOCK_SIZE);
    headBuffer.putInt(version); // Write version info, maybe change to timestamp later
    headBuffer.putInt(fileName.length());
    headBuffer.put(fileName.getBytes());
    headBuffer.putInt(usedBlockIndices.size()); // Number of used blocks
    for (int i = 0; i < usedBlockIndices.size(); i++) {
      Integer index = usedBlockIndices.get(i); // Get the index at position i
      headBuffer.putInt(index); // Store the index in the buffer
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


  public void writeIndexToFile(IMTable memTable, String dataFileName, String indexFile){
    Command c ;
    c = memTable.get(null);
    System.out.println(c);
    KeyIndex ki =  new KeyIndex(dataFileName,c.getKey());
    try (RandomAccessFile file = new RandomAccessFile(indexFile, "rw")) {
      file.seek(file.length()); // Move the pointer to the end of the file
      file.write(ki.toBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  public static void loadIndexFile(String filePath, List<String> fileNames, List<String> keys) {
    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
      while (raf.getFilePointer() < raf.length()) {
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
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String findIndexFileName(List<String> list, List<String> name, int number) {
    if (list.isEmpty() || name.isEmpty() || list.size() != name.size()) {
      return null;
    }

    int low = 0;
    int high = list.size() - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int midValue = Integer.parseInt(list.get(mid));

      if (midValue == number) {
        return name.get(mid); // Or return mid + 1 if you want to insert after duplicates
      } else if (midValue < number) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    if (low < list.size()) {
      return name.get(low); // Safe to return if low is within bounds
    } else {
      return null; // Or handle this case as needed, e.g., return the last name or an indication of "not found"
    }
    // At this point, low is the insertion point

  }



  public List<Command> scan(String startKey, String endKey) {
    return null;
  }

  public Map<String, Object> load(String fileName) throws IOException {
    // head block : version(int) + filenamelength(int) + filename + usedblockcounts + usedblockindices(multi)
    // + (keylength(int) + key)(multi)
    Map<String, Object> fileInfo = new HashMap<>();
    ArrayList<List<Command>> dataBlocksInfo = new ArrayList<>();

    try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
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
        file.seek((long) index * BLOCK_SIZE + HEAD_BLOCK_SIZE); // Position the file pointer at the start of the data block

        ByteBuffer dataBuffer = ByteBuffer.allocate(BLOCK_SIZE);

        file.read(dataBuffer.array());

        // Deserialize the data block using deserializeCommand method
        List deserializedData = deserializeCommand(dataBuffer);

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
    while (buffer.hasRemaining()) {
      // Deserialize key-length
      int keyLength = buffer.getInt();

      if (keyLength == 0 ) continue;

      // Deserialize key-value
      byte[] keyBytes = new byte[keyLength];
      buffer.get(keyBytes);
      String key = new String(keyBytes);

      // Deserialize cmd
      int cmd = buffer.getInt();
      if (cmd == -1) continue;

      // Deserialize value-length
      int valueLength = buffer.getInt();

      // Deserialize value
      byte[] valueBytes = new byte[valueLength];
      buffer.get(valueBytes);
      String value = new String(valueBytes);

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

  public void destory() {
    this.close();
    if (file != null) {
      file.delete();
    }
  }
}
