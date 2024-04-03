package proj1.lsmtree.impl;

import static proj1.SkipList.SkipList.rebuild;
import static proj1.lsmtree.impl.SSTableList.get;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import proj1.SkipList.Node;
import proj1.SkipList.SkipList;
import proj1.btree.BTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.model.InsertCommand;
import java.util.Map;

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
        for (int i = 1; i <= 1500; i ++ ){
          skipList.insert(new InsertCommand(String.valueOf(i),"12345"));
        }

        System.out.println(skipList.size());

        //SSTableList ss = new SSTableList();
        //ss.write(skipList,"testForSizes.db");

      }

      @org.junit.jupiter.api.Test
      void BulkWrite() throws IOException {

        SkipList skipList = new SkipList(0.5);

        SSTableList ss = new SSTableList();

        ss.loadConfig();

        int size = (int) ( (1048576 - 65536) * 0.9);

        for (int i = 118432; i <= 150000; i++){
          skipList.insert(new InsertCommand(String.valueOf(i),"012345678901234567890123456789"));
          if (skipList.getSize() >= size){
            System.out.println("breaking at " + i + " with " + skipList.getSize());
            break;
          }
        }

        ss.write(skipList,"testBulkWithLimits1.db");

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
      }

      @org.junit.jupiter.api.Test
      void load() throws IOException {
        SSTableList ssTable = new SSTableList();
        Map<String, Object> fileInfo = ssTable.open("test_index_001.db");
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        System.out.println("Version : "+fileInfo.get("Version"));
        System.out.println("FileName : "+fileInfo.get("FileName"));
        System.out.println("UsedBlockNums : "+fileInfo.get("UsedBlocksCount"));
        System.out.println("UsedBlockIndex : "+fileInfo.get("UsedBlockIndices"));
        System.out.println("UsedBlockFirstKeys : "+fileInfo.get("UsedBlockFirstKeys"));
        System.out.println("The data : " + dataBlocksInfo);

      }

      @org.junit.jupiter.api.Test
      void Bulkload() throws IOException {
        SSTableList ssTable = new SSTableList();
        ssTable.loadConfig();
        ssTable.printConfig();
        Map<String, Object> fileInfo = ssTable.open("testBulkWithLimits.db");
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        System.out.println("Version : "+fileInfo.get("Version"));
        System.out.println("FileName : "+fileInfo.get("FileName"));
        System.out.println("UsedBlockNums : "+fileInfo.get("UsedBlocksCount"));
        //System.out.println("UsedBlockIndex : "+fileInfo.get("UsedBlockIndices"));
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
        SkipList skipList = new SkipList(0.5);
        for (int i = 0; i <= 5; i ++){
          skipList.insert(new InsertCommand(String.valueOf(i), "<>"));
        }
        SSTableList ss = new SSTableList();
        ss.write(skipList,"test_index_001.db");
        //ss.writeIndexToFile(skipList,"test_index_001.db","test_index.idx");
        skipList = new SkipList(0.5);
        for (int i = 6; i <= 11; i ++){
          skipList.insert(new InsertCommand(String.valueOf(i), "<>"));
        }
        ss = new SSTableList();
        ss.write(skipList,"test_index_002.db");
        //ss.writeIndexToFile(skipList,"test_index_002.db","test_index.idx");
      }

      @org.junit.jupiter.api.Test
      void loadIndex() throws IOException {
        SSTableList ss = new SSTableList();
        //Map res = loadIndexFile("test_index.idx");
        //System.out.println(res.get("fileName"));
        //System.out.println(res.get("FirstKeys"));
      }

      @org.junit.jupiter.api.Test
      void searchIndex() throws IOException{
        //System.out.println(get("118429", "test_bulk.idx"));
      }

      @org.junit.jupiter.api.Test
      void searchData() throws IOException{
        SSTableList ss = new SSTableList();
        ss.loadConfig();
        System.out.println(ss.searchData("100020", "testBulkWithLimits.db"));
      }

      @org.junit.jupiter.api.Test
      void delDataSimple() throws IOException {
//        SSTableList ss = new SSTableList();
//        Map<String, Object> data = ss.delInsertBasic("2", "test_index.idx", "delete", null);
//        SkipList sl = rebuild((List<List<Command>>) data.get("data"));
//        System.out.println(sl);
//        ss.write(sl, (String) data.get("fileName"));
//
//        Map<String, Object> fileInfo = ss.open((String) data.get("fileName"));
//        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
//        System.out.println("Version : "+fileInfo.get("Version"));
//        System.out.println("FileName : "+fileInfo.get("FileName"));
//        System.out.println("UsedBlockNums : "+fileInfo.get("UsedBlocksCount"));
//        //System.out.println("UsedBlockIndex : "+fileInfo.get("UsedBlockIndices"));
//        System.out.println("UsedBlockFirstKeys : "+fileInfo.get("UsedBlockFirstKeys"));
//        System.out.println("The data : " + dataBlocksInfo);
      }

      @org.junit.jupiter.api.Test
      void delDataBulk() throws IOException {
        BTree bt = new BTree(15);
        for (int i = 0; i < 10; i ++){
          bt.insert(new InsertCommand(String.valueOf(i),"1"));
        }



      }


    }