package proj1.lsmtree.application;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

/*
 *@Author : Tairan Ren
 *@Date   : 2024/4/4 20:33
 *@Title  :
 */

class ConsoleTest {

  @Test
  void getCurrentTimestamp() {
    Map<String, String> testMap = new HashMap<>();
    testMap.put("file_with_tableName.txt", "value1");
    testMap.put("unrelated_file.txt", "value2");
    testMap.put("another_tableName_file.txt", "value3");
    testMap.put("filename_in_args.txt", "value4");
    String tableName = "with";
    List<String> temp = new ArrayList<>();
    temp.add("args");

    Iterator<Entry<String, String>> iterator = testMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String key = entry.getKey();
      for (String filename : temp) { // Iterate over each provided filename
        if (key.contains(tableName) || key.contains(filename)) { // Check if the key contains the table name or any of the filenames
          iterator.remove(); // Safely remove the current entry from the map
          System.out.println("Removed file records from map with key: " + key);
          continue; // Exit the filenames loop as we've already removed the key
        }
      }
    }
    System.out.println(testMap);
  }
}