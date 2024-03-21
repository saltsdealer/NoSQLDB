// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.application;

import java.util.Scanner;
import proj1.SkipList.SkipList;
import proj1.btree.BTree;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.InsertCommand;

public class Console {

    private Object dataStructure;
    private int flag; // 1 for BTree, 2 for SkipList

    public void menuBasic(int[] list) {
        try (Scanner scanner = new Scanner(System.in)) {
            selectDataStructure(scanner, list);

            while (true) {
                System.out.println("\n(1) Look-up (2) Insert (3) Change Data Structure (q) Quit: ");
                String input = scanner.nextLine();

                switch (input) {
                    case "1":
                        lookup(scanner);
                        break;
                    case "2":
                        insert(scanner, list);
                        break;
                    case "3": // New option to change the data structure
                        selectDataStructure(scanner, list); // Re-select data structure
                        break;
                    case "q":
                        System.out.println("Quitting...");
                        return;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            }
        } // Scanner is automatically closed here
    }


    private void selectDataStructure(Scanner scanner, int[] list) {
        System.out.println("Pick which structures to use: 1. BTree 2. SkipList");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                setupBTree(scanner, list);
                break;
            case "2":
                setupSkipList(scanner, list);
                break;
            default:
                System.out.println("Invalid option, try again.");
                selectDataStructure(scanner, list); // Recursively call until valid input
        }
    }

    private void setupBTree(Scanner scanner, int[] list) {
        this.flag = 1;
        System.out.println("Insert the nodesize of the tree (the order equals nodesize + 1):");
        int size = Integer.parseInt(scanner.nextLine());
        BTree bTree = new BTree(size+1);
        for (int value : list) {
            bTree.insert(new InsertCommand(String.valueOf(value),  "<>"));
        }
        this.dataStructure = bTree;
        System.out.println(bTree);
    }

    private void setupSkipList(Scanner scanner, int[] list) {
        this.flag = 2;
        System.out.println("Insert the probability of promotion:");
        double prob = Double.parseDouble(scanner.nextLine());
        SkipList skipList = new SkipList(prob);
        for (int value : list) {

            skipList.insert(new InsertCommand(String.valueOf(value),"<>"));
        }
        this.dataStructure = skipList;
        System.out.println(skipList);
    }

    private void lookup(Scanner scanner) {
        System.out.println("Enter the key to search for:");
        String searchKey = scanner.nextLine();
        try {
            if (flag == 1) {
                BTree bTree = (BTree) this.dataStructure;
                Command result = bTree.searchEntry(Integer.parseInt(searchKey));
                System.out.println(result != null ? "Found: " + result : "No Key Found");
            } else if (flag == 2) {
                SkipList skipList = (SkipList) this.dataStructure;
                boolean found = skipList.search(searchKey,true);
                System.out.println(found ? "Key Found" : "No Key Found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid key format. Please enter a valid integer.");
        }
    }

    private void insert(Scanner scanner, int[] list) {
        System.out.println("Enter the key to insert:");
        String key = scanner.nextLine();
        System.out.println("Enter the value to insert:");
        String value = scanner.nextLine();

        try {
            if (flag == 1) {
                BTree bTree = (BTree) this.dataStructure;
                bTree.insert(new InsertCommand(key, value));
            } else if (flag == 2) {
                SkipList skipList = (SkipList) this.dataStructure;
                skipList.insert(new InsertCommand(key,value));
            }
            System.out.println(this.dataStructure);
        } catch (NumberFormatException e) {
            System.out.println("Invalid key format. Please enter a valid integer.");
        }
    }
}
