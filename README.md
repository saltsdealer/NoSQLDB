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

### 2. Actual Implementation 

This project's execution is based on the designs and proposals outlined in the preceding chapters, successfully realizing a majority of the envisioned functionalities. However, the practical application revealed that integrating an LSM tree contradicted certain course requirements, such as file indexing. Moreover, the data volume did not necessitate compaction due to its insufficiency to trigger the process and there is no need for logs as it is not actually a part of the database but rather addons.

Nevertheless, a compaction strategy was devised to address the accumulation of smaller files. While both BTree and SkipList data structures are viable and can be used interchangeably, empirical evidence showed a slight performance edge for SkipLists. Therefore, unless otherwise specified, SkipLists are the default data structure for in-memory operations.

Data storage is facilitated through SSTables, employing a byte stream format. This choice was influenced by the data encapsulation within a Command class, allowing for a standardized and consistent serialization method.

A notable highlight of the system is its capability for sequential data insertion, which stands out as a key feature.

#### 2.1 Major Classes

In-memory Structures:

The `MemEngine` class orchestrates the in-memory data structures, managing the initial loading of data batches. Contrary to the conventional approach in LSM trees, where the most recent data segment is retained in memory, this implementation ensures all data is systematically flushed to disk storage, adhering to the course's stipulations.

Persistent Storage:

While the system doesn't employ the traditional leveling associated with LSM trees, it incorporates a compaction mechanism. The `SSTable` class encapsulates functionalities for interacting with disk-stored data, including reading, searching, writing, and deleting operations. Furthermore, it is engineered to parse configuration files, allowing for customizable settings such as head and data block sizes, enhancing its adaptability.

Console:

The console serves as the interface for user interaction, guiding the flow and execution of functions. It is the point of engagement where users can invoke the system's capabilities, making it a crucial component for operational accessibility.

### 3. Assumption 

The system is designed to accommodate keys that are either integers or comparable strings, ensuring flexibility in handling various data types. During the initial data loading phase, it's assumed that the input data is pre-sorted. This approach streamlines the insertion process, leveraging the efficiency of working with ordered datasets.

Nevertheless, the system is also equipped to manage unsorted data inputs. While this capability is not explicitly exposed as a direct feature, it becomes operational during subsequent data insertions, beyond the initial load. This design choice presupposes that the initial bulk data ingestion is preprocessed and curated by ETL (Extract, Transform, Load) engineers to ensure data quality and order. Subsequent insertions, which might introduce unsorted data, activate the system's built-in mechanisms to handle such scenarios, maintaining the integrity and performance of the data management process.

### 4 Sequential and Indexing Features

The system is optimized for maximum efficiency when handling ordered data, which is why both BTree and SkipList data structures, known for maintaining order, are utilized. This ordering principle extends to the indexing mechanism as well:

- The index file plays a crucial role by recording the key of the first data entry in each file, alongside the corresponding file names. This approach facilitates quick access to the appropriate data file during search operations, eliminating the need to scan all files.
- Within each data file, the head block is designated for a special index block. This index block contains the keys of the first entry in each subsequent data block. This structure allows for rapid navigation within the file, significantly reducing the search space when looking for specific entries.

This indexing strategy is inherently scalable. While the current setup involves indexing the first key of every data block, it can adapt to changing file sizes. For instance, if the file size increases significantly, the system could be adjusted to index the first key of every fifth block instead. This modification would not markedly impact efficiency, as it still provides a structured pathway to locate data quickly, demonstrating the system's flexibility and scalability in accommodating growing data volumes.

![](https://i.postimg.cc/524t5jjy/temp.png)

#### 4.1 Searching Alog:

Leveraging the ordered nature of both the index file and the index blocks within data files, the system employs a modified version of binary search for efficiently locating both filenames and specific data blocks. This adaptation of binary search aligns with the structured order of data storage, allowing for quick access to information.

The time complexity of these search operations is primarily determined by the number of data blocks, denoted as log(NUM(datablocks)). In a scenario where the total data volume is approximately 4 MB, the system might only need to manage around 5 to 6 files. In such a context, the overhead introduced by searching among filenames is minimal and hardly impacts the overall efficiency. However, in scenarios involving larger datasets, the time complexity could extend to log(NUM(Datas/filesizes)) + log(NUM(datablocks)), reflecting the added layer of searching through a greater number of files before narrowing down to specific data blocks.

In the current setup, the number of data blocks, estimated at 3800, is treated as a constant. This assumption simplifies the complexity model, focusing on the scalability of file management and access within those constraints. The efficiency of the system in handling data retrieval tasks underscores its robustness in managing datasets of varying sizes while maintaining quick access through its hierarchical indexing and binary search mechanisms.



#### 4.2 The numeric Relationships

In the process, the project noticed that there is a quite possible polynomial relationship between head block size, data block size max file size , that is :
$$
NUM(datablocs) * 4 + 1000 = HEADBLOCK
$$
However, it is not this project's purposes to looking into such math problems and hence a rough number is given in default settings.

For now the configuration is setting as :

```ini
[Settings]
version = 1
# would require something more than datablocks * 4 
MAX_FILE_SIZE = 1048576
BLOCK_SIZE = 256
HEAD_BLOCK_SIZE = 40000
MULTIPLIER = 0.85
META_BLOCK_SIZE = 128

[Console]
COMMA_OUTSIDE_QUOTES_PATTERN = (?=([^"*"^"*^"]*$))
DOWNLOAD_REGEX = (.*?\(\d+\)),(.*)
```

 This will return with 3800 + ish data blocks.

### 5 Compaction 

The program is designed with the expectation that users may perform smaller batch data insertions, introducing the potential for unsorted or "dirty" data. To maintain data integrity and order within the system, a compaction method plays a critical role.

The compaction process begins with sorting the incoming data to establish a clear sequence. This sorted sequence helps in determining the most efficient order for inserting the data into the existing structure. The primary goal during this phase is to merge and compact data in a way that preserves the overall order and optimizes storage efficiency.

However, there's a recognition within the system that the insertion of new, sorted data during compaction could lead to files exceeding their optimal size. To address this issue, the system adopts a strategy reminiscent of LSM tree compaction mechanisms. Should a file grow too large as a result of compaction, the system is prepared to perform a more extensive operation: it will reload and reprocess the data, effectively starting the compaction process anew.

This approach ensures that the system can adapt to the complexities of managing and organizing data, particularly when faced with the challenge of integrating unsorted insertions into an ordered structure. It reflects a balance between maintaining order and efficiency in data storage and allowing for the flexibility needed to accommodate user-driven data insertions.

![](https://i.postimg.cc/ZnQL8XxK/mr.png)



### 6 Use Cases:

The program offers a suite of commands designed to facilitate various database operations:

1. **`init [database name]`**: This command initializes a new database with the specified name. It sets up the necessary environment and structures for storing and managing data within the newly created database.
2. **`use [database name] [tablename]`**: This command is used to select an existing database and table for subsequent operations. It's useful when you want to work with a specific dataset without creating a new database.
3. **`put [filename.csv] [tablename]`**: This command loads data from a CSV file into the specified table. It's designed for bulk data insertion, allowing for efficient data import from external sources.
4. **`get [filename]`**: This command exports data to a CSV file, facilitating data retrieval and export for analysis or backup purposes. It mirrors the `put` command but for data extraction.
5. **`add`**: This command supports the insertion of single data entries, providing a means to add individual records to the database. It's particularly useful for incremental data updates or additions.
6. **`del`**: This command is used for deleting single data entries. It allows for precise removal of specific records, maintaining data integrity and relevance.
7. **`set`**: With this command, users can update the value of an existing data entry. It's essential for maintaining the accuracy and currency of the stored data.
8. **`search`**: This command facilitates data retrieval based on specific criteria. It enables users to query the database for information, supporting a range of search requirements from simple lookups to more complex queries.

These commands collectively provide a comprehensive toolkit for database management, from initialization and data loading to querying, updating, and deleting records. They cater to a wide range of data manipulation needs, ensuring flexibility and efficiency in database operations.



###  

## Resources:
- [B-tree Visualization](https://www.cs.usfca.edu/~galles/visualization/BTree.html)
- B-tree pseudocode on Canvas
- [LsmTree](https://www.cs.umb.edu/%7Eponeil/lsmtree.pdf)
- [SkipTree](https://www.geeksforgeeks.org/skip-list/)

  

  

  

  

