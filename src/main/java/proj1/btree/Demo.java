// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.btree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;

public class Demo {
  public static void buildTree(String fileName, BTree bTree) {

    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
        String[] tokens = line.split(",");
        for (String token : tokens){
          Command entry = new InsertCommand(token.trim(), "");
          bTree.insert(entry);
        }
      }
      reader.close();
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
    }

    System.out.println(bTree.toString());
  }


  public static void main(String[] args) {

    Scanner scanner = new Scanner(System.in);

    System.out.print("Please enter desired tree size: ");
    String userInput = scanner.nextLine();
    int size = 0;

    // Parse the user input as an integer
    try {
      size = Integer.parseInt(userInput);
    } catch (NumberFormatException e) {
      System.err.println("Error: Invalid input. Please enter an integer.");
    }

    BTree bTree = new BTree(size);

    System.out.print("1 for build, 2 for insert, 3 for search, others to quit: ");
    userInput = scanner.nextLine();
    while (true){
      if (userInput.equals("1")){
        System.out.print("Please enter input data file name: ");
        String fileName = scanner.nextLine();
        buildTree("./src/main/java/proj1/btree/" + fileName, bTree);
      } else if (userInput.equals("2")){
        System.out.print("Key: ");
        String key = scanner.nextLine();
        System.out.print("Value: ");
        String value = scanner.nextLine();
        Command entry = new InsertCommand(key, value);
        bTree.insert(entry);
      } else if (userInput.equals("3")){
        System.out.print("Key to search: ");
        String keyString = scanner.nextLine();
        int key = Integer.parseInt(keyString);
        System.out.println(bTree.searchEntry(key));
      } else {
        break;
      }
      System.out.print("1 for build, 2 for insert, 3 for search, others to quit: ");
      userInput = scanner.nextLine();
    }

    scanner.close();
  }
}