# Group 8:
### Quan Yuan 002703792
### Tairan Ren 00xxxxxx

## Project 1: B-tree Index

## How to Run:
```
cd src/main/java/proj1/btree

```
Download the code to a local directory, and open the directory with IntelliJ would be an easier way to run.

Click the run button on Demo.java, and then input the file name, and desired tree size. 

## Project Implementation Details:
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

The split() function deals with the situation that when a node reaches its capacity, it will split into 2 nodes, and promote 1 key-value pair to the parent node. The promoting node we choose is the middle-right one. For example, if there are 5 nodes, the 3rd node will be promoted. If there are 4 nodes, the 3rd node will be promoted. 

The Node class defines the nodes in B-tree. Each node stores a list of key and value, and a list of child nodes. As the amount is not pre-determined.

## Resources:
- [B-tree Visualization](https://www.cs.usfca.edu/~galles/visualization/BTree.html)
- B-tree pseudocode on Canvas