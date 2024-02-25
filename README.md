# Group 8:
### Tairan Ren 002772875
### Quan Yuan 002703792


## Project 1: Index 

### 1.Introduction

The objective of this project is to develop an indexing system tailored for NoSQL databases. Our approach centers on deploying the LSM tree, distinguished by its flexibility and adaptability compared to traditional, rigid tree data structures such as the B+ tree or Red-Black tree. The LSM tree, widely embraced by NoSQL solutions like HBase, LevelDB, and RocksDB, functions primarily as a sophisticated storage architecture rather than a conventional tree structure.

In the initial phases, our focus will be on constructing the foundational component of the LSM tree: the MemTable. This in-memory data structure is pivotal for holding the latest data updates, systematically arranging them by key to ensure orderliness. Unlike other data structures, the LSM tree does not prescribe a specific method for achieving this order. For instance, HBase employs a skip list to maintain key order in memory, highlighting the LSM tree's inherent flexibility.

This versatility opens avenues for incorporating various data structures for indexing purposes. In our project, we have chosen to explore and compare the efficacy of both BTree and SkipList. This comparative analysis not only facilitates potential enhancements and scalability but also enhances the system's resilience to bugs, thereby contributing to the robustness of our indexing solution.

### 2.How to Run:

```
cd src/main/java/proj1
```
Download the code to a local directory, as this is created with maven framework, open the directory with IntelliJ idea or eclipse would be an easier way to run. For now the testing data is hardcoded in the same file.

Click the run button on Test.java, and then input the file name, and desired node size, or do it via java commands, either way is fine.

The Class utilizes console printed control panel which first ask users to select which data structure to use, and then ask users for specific operations to run, which for now, includes the basic operation like insertion and searching for index.

 The testing data for demo is currently hard coded, which will be modified to file based, the read file method was also provided in btree package demo.java.

Note: 

A read from file method is provided in src\main\java\btree\Demo.java as well. If want to use self-defined data file, please put the file in the directory "src/main/java/proj1/btree/", which is the same directory with Demo.java. 


![](https://i.postimg.cc/T2K2qhN0/image.png)

### 3.Implementation Details:

#### Packages :

##### btree : 

This project implements a B-tree index structure. The implementation includes the basic functionalities of inserting and searching for keys in the B-tree. The B-tree structure ensures that the tree remains balanced even after multiple insertions and deletions, resulting in optimal search and retrieval times.

The BTree class implements a B-tree with a given number, representing the number of child nodes for each node. It allows the underlying structure to have flexible tree span. 

The public functions of BTree class are:

- BTree(int m): constructor
- getRoot(): returns the root node
- searchEntry(int key): searches for a key-value pair using given key
- searchNode(int key): searches for a node using given key
- insert(Command entry): inserts a key-value pair to the B-tree
- countNodesAndEntries(): returns the number of nodes and entries in the B-Tree
- toString():converts the B-tree to a displayable String

The split() function deals with the situation that when a node reaches its capacity, it will split into 2 nodes, and promote 1 key-value pair to the parent node. The promoting node we choose is the middle-left one. For example, if there are 5 nodes, the 3rd node will be promoted. If there are 4 nodes, the 2rd node will be promoted. This is adjustable base on future requirements.

The Node class defines the nodes in B-tree. Each node stores a list of key and value, and a list of child nodes. As the amount is not pre-determined.

A btree produced by the code :

![](https://i.postimg.cc/15kwpk9C/btree.png)

##### skiplist :

this project also provided implemented simplified version of skiplist, includes the basic node structure and functionalities. It is a probabilistic alternative to balanced trees and organizes elements in multiple levels, with higher levels providing shortcuts for traversing the list. This implementation supports generic key-value pairs, where keys are comparable to enable ordering.

- search(K key, boolean printPath): Searches for a node with the specified key. If printPath is true, the method will also print the path taken during the search. It returns true if the key is found, otherwise false.


- delete(K key): Removes the node with the specified key from the SkipList across all levels where it appears. This method ensures that the SkipList maintains its structural integrity after the deletion.


- insert(K key, V value): Inserts a new key-value pair into the SkipList. If a node with the given key already exists, the method aborts to prevent duplicate keys. The insertion process involves potentially promoting the node to higher levels based on the prob probability factor.

- toString(): Provides a string representation of the SkipList, showing the keys and values of nodes at each level, starting from the topmost level down to the bottom. This method is useful for debugging and visualizing the structure of the SkipList.

The SkipList uses a probabilistic approach to maintain balance, with the prob parameter influencing the height and structure of the list. It allows for efficient search, insertion, and deletion operations, with average time complexities that are comparable to balanced trees.
The SkipList is particularly suited for applications that require fast search operations and sequential access to elements.

A skiplist produced by the class is :

![](https://i.postimg.cc/R0Rtxqwy/skiplist.png)

##### lsmtree :

for now it defines the Command class which is a customized key value pair, which is created every time the user create a search or insertion command, and then passed to the data structures.

It also provides interfaces for MemTable, and SSTable for future implementation  , other parts will be implemented in future stages.

### 4.limitations

For now, none of the concurrency is taken into consideration.

As it is involving user inputs, the input tolerances is also not added to the console prototype. 

Only the basic functions is inserted in this stage, may consider adding deletion and change functions to the structures later.

## Project 2 ： 

### 1. Design and Proposal

Name:

LSM DB

Type of index:

Interchangeable, BTree or SkipList, or perhaps doing a comparison between the two self-implemented with built in structures like treemap etc,.

Test Data :

This project implemented it's own kv structure so basically could accommodate any data. 

The provided movies data will be used as testing data.

Key:

As long as the key is comparable, then it doesn't have to be integer. 

For this stage of the project, it assumes that keys are integer. For further implementation, it will consider using timestamp or something similar as keys.

Size of record:

from the given movies.csv, we assume the upper bound for each record is limited to 128 bytes.

Sample data :

| ID   | TITLE                              | GENRE                  |
| ---- | ---------------------------------- | ---------------------- |
| 3    | Grumpier Old Men (1995)            | Comedy\|Romance        |
| 4    | Waiting to Exhale (1995)           | Comedy\|Drama\|Romance |
| 5    | Father of the Bride Part II (1995) | Comedy                 |

Allocation Method :

The key index facilitates a form of direct access by storing the offset of each key within the file, which can be seen as a simplified form of indexed allocation, but it's specific to the SSTable structure and not a general-purpose file system indexed allocation method.

Free block method :

At this stage, it doesn't seem to be a must to use free block management, but we assume a simple bitmap could be added.

Overview of the LSM strucuture:

for the memory tables, structures are given in the previous chapters, for the disk drive file format, we intend to implement sstable, which looks like :

| data                       | index                  | fcb             |
| -------------------------- | ---------------------- | --------------- |
| {"3":{"..." : "...", ...}} | {"3" : "OFFESETS" ...} | Meta(Date, ...) |

 A typical lsm would have multiple levels, for simplicity, , this project reduce it to this:

![](https://img2.imgtp.com/2024/02/26/Gixt0ud6.png)

Developing Env:

Java 11. 

Intellj Idea,

[Github]: https://github.com/saltsdealer/NoSQLDB



## Resources:
- [B-tree Visualization](https://www.cs.usfca.edu/~galles/visualization/BTree.html)
- B-tree pseudocode on Canvas
- [LsmTree](https://www.cs.umb.edu/%7Eponeil/lsmtree.pdf)
- [SkipTree](https://www.geeksforgeeks.org/skip-list/)

  

  

  

  

