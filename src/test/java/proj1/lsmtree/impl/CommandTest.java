package proj1.lsmtree.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import proj1.SkipList.Node;
import proj1.SkipList.SkipList;
import proj1.lsmtree.CommandEnum;
import proj1.lsmtree.model.InsertCommand;
import java.util.Map;
import org.openjdk.jol.info.ClassLayout;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/3/9 19:53
 *@Title  : 
 */
    
    class CommandTest {



      @org.junit.jupiter.api.Test
      void compareTo() {
      }

      @org.junit.jupiter.api.Test
      void toBytes() {
        InsertCommand command = new InsertCommand("123", "-->123456789");
        byte[] bytes = command.toBytes();
        //System.out.println(bytes.length);
      }

      @org.junit.jupiter.api.Test
      void simpleWrite() throws IOException {
        InsertCommand c =  new InsertCommand("3","12345");
        byte[] b = c.toBytes();
        InsertCommand c1 =  new InsertCommand("4","12345");
        byte[] b1 = c1.toBytes();
        // Allocate a ByteBuffer with capacity equal to the sum of both byte arrays
        ByteBuffer combinedBuffer = ByteBuffer.allocate(256);

        // Put both byte arrays into the buffer
        combinedBuffer.put(b);
        combinedBuffer.put(b1);
        SkipList skipList = new SkipList(0.5);

        skipList.insert(c);
        skipList.insert(c1);

        for (Object o : skipList){
          System.out.println(((Node) o).getCommand());
        }

        RandomAccessFile file = new RandomAccessFile("test", "rw");
        file.write(combinedBuffer.array());
      }

      @org.junit.jupiter.api.Test
      void Write() throws IOException {
        InsertCommand c =  new InsertCommand("3","12345");
        InsertCommand c1 =  new InsertCommand("4","12345");
        SkipList skipList = new SkipList(0.5);

        int size = 0;
        for (int i = 1; i < 1000; i ++ ){
          skipList.insert(new InsertCommand(String.valueOf(i),"12345"));
        }

        System.out.println(skipList.getSizeBytes());
        System.out.println(skipList.getSize());
        //SSTableList ss = new SSTableList();
        //ss.write(skipList,"testForSizes.db");

      }

      @org.junit.jupiter.api.Test
      void BulkWrite() throws IOException {

        SkipList skipList = new SkipList(0.5);

        SSTableList ss = new SSTableList();

        ss.loadConfig();

        int size = (int) ( (1048576 - 65536) * 0.9);

        for (int i = 100000; i <= 150000; i++){
          skipList.insert(new InsertCommand(String.valueOf(i),"012345678901234567890123456789"));
          if (skipList.getSize() >= size){
            System.out.println("breaking at " + i);
            break;
          }
        }

        ss.write(skipList,"testBulkWithLimits.db");

      }

      @org.junit.jupiter.api.Test
      void simpleLoad() throws IOException {
        RandomAccessFile file = new RandomAccessFile("testWithWrite", "r");
        ByteBuffer bf =  ByteBuffer.allocate(256);
        file.seek(256);
        file.read(bf.array());
        // Close the file to release resources
        file.close();
        // Use the array from ByteBuffer as the input for the deserialization method
        deserializeCommand(bf);
      }

      @org.junit.jupiter.api.Test
      void load() throws IOException {
        SSTableList ssTable = new SSTableList();
        Map<String, Object> fileInfo = ssTable.load("testWithIndexAndHeadBlock.db");
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        System.out.println("Version : "+fileInfo.get("Version"));
        System.out.println("FileName : "+fileInfo.get("FileName"));
        System.out.println("UsedBlockNums : "+fileInfo.get("UsedBlocksCount"));
        System.out.println("UsedBlockIndex : "+fileInfo.get("UsedBlockIndices"));
        System.out.println("UsedBlockFirstKeys : "+fileInfo.get("UsedBlockFirstKeys"));
        System.out.println("The data : " + dataBlocksInfo);
        List blockZero  = (List) dataBlocksInfo.get(0);
        Command test = (Command) blockZero.get(0);
        System.out.println(test);

      }

      @org.junit.jupiter.api.Test
      void Bulkload() throws IOException {
        SSTableList ssTable = new SSTableList();
        ssTable.loadConfig();
        ssTable.printConfig();
        Map<String, Object> fileInfo = ssTable.load("testBulkWithLimits.db");
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        System.out.println("Version : "+fileInfo.get("Version"));
        System.out.println("FileName : "+fileInfo.get("FileName"));
        System.out.println("UsedBlockNums : "+fileInfo.get("UsedBlocksCount"));
        System.out.println("UsedBlockIndex : "+fileInfo.get("UsedBlockIndices"));
        System.out.println("UsedBlockFirstKeys : "+fileInfo.get("UsedBlockFirstKeys"));
        System.out.println("The data : " + dataBlocksInfo);
        List blockZero  = (List) dataBlocksInfo.get(0);
        Command test = (Command) blockZero.get(0);
        System.out.println(test);

      }

      @org.junit.jupiter.api.Test
      void loadIni() throws IOException {
        SSTableList ssTable = new SSTableList();
        ssTable.loadConfig();
        ssTable.printConfig();

      }

      @org.junit.jupiter.api.Test
      void writeIndex() throws IOException {
        InsertCommand c =  new InsertCommand("3","12345");
        InsertCommand c1 =  new InsertCommand("4","12345");
        SkipList skipList = new SkipList(0.5);
        skipList.insert(c);
        skipList.insert(c1);
        SSTableList ss = new SSTableList();
        ss.writeIndexToFile(skipList,"data.db","index.idx");
      }

      @org.junit.jupiter.api.Test
      void loadIndex() throws IOException {
        SSTableList ss = new SSTableList();
        List<String> names = new ArrayList<>();
        List<String> firstKey = new ArrayList<>();
        ss.loadIndexFile("index.idx",names,firstKey);
        System.out.println(names);
        System.out.println(firstKey);
      }


      public static void deserializeCommand(ByteBuffer buffer) {

        while (buffer.hasRemaining()) {
          // Deserialize key-length
          int keyLength = buffer.getInt();
          System.out.println("The key length == " + keyLength);
          if (keyLength == 0 ) continue;
          // Deserialize key-value
          byte[] keyBytes = new byte[keyLength];
          buffer.get(keyBytes);
          String key = new String(keyBytes);

          // Deserialize cmd
          int cmd = buffer.getInt();

          // Deserialize value-length
          int valueLength = buffer.getInt();

          // Deserialize value
          byte[] valueBytes = new byte[valueLength];
          buffer.get(valueBytes);
          String value = new String(valueBytes);

          // Print deserialized InsertCommand
          System.out.println("Key: " + key + ", Cmd: " + cmd + ", Value: " + value);
        }
      }


    }