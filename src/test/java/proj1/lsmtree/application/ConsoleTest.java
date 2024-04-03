package proj1.lsmtree.application;

import static org.junit.Assert.assertEquals;
import static proj1.lsmtree.application.Console.measureExecutionTime;
import static proj1.lsmtree.application.Console.readIniFileToMap;
import static proj1.lsmtree.application.Console.writeMapToIniFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj1.btree.BTree;
import proj1.lsmtree.impl.MeMEngine;
import proj1.lsmtree.impl.SSTableList;

/*
 *@Author : Tairan Ren
 *@Date   : 2024/3/26 16:45
 *@Title  :
 */

class ConsoleTest {

  private Console c;
  private SSTableList ss;
  private MeMEngine mm;
  private Scanner sc;

  @BeforeEach
  void setUp() throws IOException {
    // This method is run before each test method
    c = new Console();
    ss = new SSTableList();
    mm = new MeMEngine();
    c.tableName = "movies";

    ss.setDirectory(new File("test"));
    c.setDbName("test");
    mm.setDbName("test");
    sc = new Scanner(System.in);
  }
  @Test
  void processCSV() throws IOException {

    c.processCSV("movies-large.csv",mm);

    c.flush(mm);
  }
  @Test
  void deleteWhofile() throws IOException {

    //Console.deleteDirectoryWithContents("test");
  }

  @Test
  void get() throws IOException {

  }

  @Test
  void open() throws IOException {
//    c.open("test_movies_small_1.db");
//    c.open("test_movies_small_11.db");
//    c.open("test_movies_small_64.db");
//    c.open("test_movies_small_1240.db");
    c.open("test_new_id.idx");
    //c.open("test_movies_small_id.idx");



  }

  @Test
  void put() throws IOException {
    SSTableList.put("9020","test movie, test genre","test_movie_id.idx",new File("test"));

  }

  @Test
  void del() throws IOException {

    c.del("50",mm);
    //c.set("148134","test,test(1025),test", mm);
  }

  @Test
  void dir() throws IOException {

    c.get("9020",ss,mm);
  }

  @Test
  void destroy() throws IOException {
    c.processCSV("movies-large.csv",mm);
    c.flush(mm);
    c.destroy("test_1.db", mm);
  }

  @Test
  void compaction() throws IOException {
    List<List<String>> c = Console.readCSVAndDetectSequentialChunks("movies-test.csv",true);
    //System.out.println(c);
    Console.orderNoMatterInsertion(c,new File("test"),"test","movies");
  }

  @Test
  public void testBasicMerge() throws IOException {

    Console.compaction("test",new File("test"),"movies");
//    String name = "test_movies_20240330_145447_2.db";
//    String[] parts = name.split("_");
//    String numberPart = parts[parts.length - 1];
//    parts = numberPart.split("\\.");
//    numberPart = parts[0];
//    System.out.println(numberPart);

  }
  @Test
  public void btree() throws IOException {
    measureExecutionTime(() -> {
      try {
        mm = new MeMEngine("test");
        System.out.println(mm.getWritable() instanceof BTree);

        c.processCSV("movies-large.csv", mm);
        c.flush(mm);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test
  public void ini() throws IOException {
    new SSTableList();
  }

}