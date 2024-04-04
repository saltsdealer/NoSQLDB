// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.application;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import proj1.SkipList.Node;
import proj1.SkipList.SkipList;
import proj1.btree.BTree;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.impl.MeMEngine;
import proj1.lsmtree.impl.SSTableList;
import proj1.lsmtree.model.InsertCommand;


public class Console {

    private static final String CONFIG_FILE_PATH = "configConsole.ini";
    private IMTable dataStructure;
    private int flag;
    public String dbName = "test";
    public String tableName = "default";
    public File dataDirectory;
    private SSTableList ss;
    private MeMEngine mm;
    private int readyFlag = 0;
    private Map<String,String> reads = new HashMap<>();
    private static Pattern COMMA_OUTSIDE_QUOTES_PATTERN = Pattern.compile(
        ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", Pattern.MULTILINE);
    private static Pattern FILE_INDEX_PATTERN = Pattern.compile("_(\\d+)\\.db");
    private static Pattern DOWNLOAD_REGEX = Pattern.compile("(.*?\\(\\d+\\)),(.*)");

    public Console() throws IOException {
    }

    public static void writeMapToIniFile(Map<String, String> map1) {
        File file = new File(CONFIG_FILE_PATH);

        boolean appendMode = false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, appendMode))) {
            // Write the map's entries to the file only if the map is not empty
            if (!map1.isEmpty()) {
                for (Map.Entry<String, String> entry : map1.entrySet()) {
                    writer.println(entry.getKey() + "=" + entry.getValue());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> readIniFileToMap() {
        Map<String, String> map = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore empty lines and lines starting with a comment character
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    String[] parts = line.split("=", 2); // Split the line into key and value
                    if (parts.length == 2) { // Ensure that the line is in valid "key=value" format
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        map.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(
                "ini file for CONFIG_FILE_PATH is not created, use quit to shut down the programme");
        }

        return map;
    }


    public String loadConfig() throws IOException {
        try (InputStream inputStream = SSTableList.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + CONFIG_FILE_PATH);
            }

            Ini ini = new Ini(inputStream);
            DOWNLOAD_REGEX = Pattern.compile(ini.get("Console","DOWNLOAD_REGEX",String.class));
            COMMA_OUTSIDE_QUOTES_PATTERN = Pattern.compile(ini.get("Console","COMMA_OUTSIDE_QUOTES_PATTERN",String.class));
        } catch (IOException e) {
            e.printStackTrace();
            return ("Failed to load config: " + e.getMessage());
        }
        return "Successfully loaded";
    }

    public void menu(){
        try (Scanner scanner = new Scanner(System.in)) {
            Terminal terminal = TerminalBuilder.terminal();

            LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new FileNameCompleter())
                .history(new DefaultHistory())
                .build();

            System.out.println("System Started, Welcome.");

            while (true) {

                System.out.print(">>>");
                String input = lineReader.readLine().trim();
                String[] s = input.split(" ");

                if (input.equals("read")){
                    System.out.println("Already Loaded datafile and tables: " + reads.keySet());
                    continue;
                }

                if (input.equals("current")){
                    System.out.println("Working at : " + this.dbName +"_"+ this.tableName);
                    continue;
                }

                if (input.equals("help")) {
                    helpConsole();
                    continue;
                }

                if (input.equals("quit")){
                    writeMapToIniFile(reads);
                    System.exit(0);
                }

                if (s.length < 2 && !s[0].equals("dir") && !s[0].equals("kill")) {
                    System.out.println(">>> Invalid input");
                    continue;
                }
                if (readyFlag == 0 && !s[0].equals("init") && !s[0].equals("use")) {
                    System.out.println(">>> Please use 'init' or 'use' command to initialize first.");
                    continue; // Skip the rest of the loop and prompt for input again
                }

                switch (s[0]) {

                    case "init":
                        init(s[1]);
                        break;
                    case "use":
                        if (s.length == 3)
                            setDirectory(s[1], s[2]);
                        else {
                            System.out.println(
                                ">>> Input the database and table name to use the table");
                        }
                        break;
                    case "open":
                        if (readyFlag == 0) continue;
                        open(s[1]);
                        break;
                    case "search":
                        if (readyFlag == 0) continue;
                            if (s.length > 1) {
                                String[] finalS = s;
                                measureExecutionTime(() -> {
                                get(finalS[1], ss, mm);
                        });
                            }
                        break;
                    case "put":
                        if (readyFlag == 0) continue;
                        String[] finalS1 = s;
                        measureExecutionTime(() -> {
                            // if data was inserted, just stop the command
                            if (reads.isEmpty()) {
                                System.out.println(">>> First Time Loading Data");
                            } else if (checkIfInserted(finalS1[1])) {
                                System.out.println(">>> Data already Inserted");
                                return; // Use return instead of break to exit the Runnable
                            }

                            if (finalS1.length < 3) {
                                System.out.println(">>> Table Name Not Given");
                                return;
                            }
                            // check if table was created
                            if (!checkIfInserted(finalS1[2])) {
                                System.out.println("Creating New Table, Loading Data: ");
                                this.tableName = finalS1[2];
                                reads.put(finalS1[2],dbName);
                                System.out.println(">>> In this case, All thing will be put into Hard Disk");
                                System.out.println(">>> Writing Data : ");
                                try {
                                    processCSV(finalS1[1], mm);
                                    reads.put(finalS1[1], dbName);
                                    System.out.println("Flushing Memory : ");
                                    flush(mm,"sl");
                                    System.out.println("Meta Data: ");
                                    writeMeta(mm,false);
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            } else {
                                // only happens when inserting into the current table since its logically ok,
                                // not going to change this
                                System.out.println(">>> Inserting new File to " + tableName);
                                try {

                                    List<List<String>> c = Console.readCSVAndDetectSequentialChunks(
                                        finalS1[1],true);
                                    orderNoMatterInsertion(c,new File(dbName),dbName,tableName);
                                    compaction(dbName,new File(dbName),tableName);
                                    reads.put(finalS1[1], dbName);
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            }
                        });
                        break;
                    case "putBTree":
                        if (readyFlag == 0) continue;
                        String[] finalS2 = s;
                        measureExecutionTime(() -> {

                            if (reads.isEmpty()) {
                                System.out.println(">>> First Time Loading Data");
                            } else if (checkIfInserted(finalS2[1])) {
                                System.out.println(">>> Data already Inserted");
                                return; // Use return instead of break to exit the Runnable
                            }

                            if (finalS2.length < 3) {
                                System.out.println(">>> Table Name Not Given");
                                return;
                            }
                            this.tableName = finalS2[2];
                            System.out.println(">>> In this case, All thing will be put into Hard Disk");
                            System.out.println(">>> Writing Data : ");
                            try {
                                MeMEngine mm = new MeMEngine("BTree");
                                mm.setDbName(dbName);
                                processCSV(finalS2[1], mm);
                                System.out.println("Flushing Memory : ");
                                flush(mm,"BTree");
                                System.out.println("Meta Data: ");
                                writeMeta(mm,false);
                                reads.put(finalS2[1], dbName);
                                reads.put(finalS2[2],dbName);
                            } catch (IOException e) {
                                System.out.println(e);
                            }

                        });
                        break;
                    case "dir":
                        if (readyFlag == 0) continue;
                        if (dbName == null) {
                            System.out.println(
                                ">>> Please use 'init' or 'use' command to initialize first.");
                            continue;
                        }
                        dir(dbName);
                        break;
                    case "add":
                        if (readyFlag == 0) continue;
                        if (s.length > 3) {
                            String combined = Arrays.stream(s, 2, s.length) // Create a stream starting from the third element
                                .collect(Collectors.joining(" ")); // Join them with a space in between

                            s = new String[]{s[0], s[1], combined}; // Create a new array with the first two elements and the combined string
                        }
                        if (s.length < 3){
                            put(scanner,mm,ss);
                            break;
                        }
                        put(scanner,s,mm,ss);
                        break;
                    case "del":
                        if (readyFlag == 0) continue;
                        if (s.length < 2){
                            measureExecutionTime(() -> {
                                try {
                                    del(scanner,mm);
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            });
                            break;
                        }
                        try {
                            del(s[1],mm);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        break;
                    case "set":
                        if (readyFlag == 0) continue;
                        if (s.length > 3) {
                            String combined = Arrays.stream(s, 2, s.length) // Create a stream starting from the third element
                                .collect(Collectors.joining(" ")); // Join them with a space in between

                            s = new String[]{s[0], s[1], combined}; // Create a new array with the first two elements and the combined string
                        }
                        if (s.length < 3){
                            measureExecutionTime(() -> {
                                try {
                                    set(scanner,mm);
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            });
                            break;
                        }
                        String[] finalS3 = s;
                        measureExecutionTime(() -> {
                            try {
                                set(finalS3[1], finalS3[2],mm);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                        });
                        break;
                    case "rm":
                        try {
                            if (readyFlag == 0)
                                continue;
                            destroy(s[1], mm);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        break;
                    case "kill":
                        if (s.length == 1){
                            System.out.println("Deleting Entire Database");
                            deleteDirectoryWithContents(dbName,reads,dbName);
                            this.dbName = null;
                            ss = new SSTableList();
                            mm = new MeMEngine();
                        } else if (s.length == 3){
                            System.out.println("Deleting Entire Table");
                            this.tableName = null;
                            ss = new SSTableList();
                            mm = new MeMEngine();
                            deleteFilesContainingTableName(dbName,s[1],s[2],reads);
                        }
                        this.readyFlag = 0;

                        break;

                    case "get":
                        try{
                            if (s.length < 3 && s[1].equals("all")) return;
                            if (s[1].equals("all") && s.length == 3){
                                if (tableName.equals("default") || dbName == null){
                                    System.out.println("init or select db first");
                                    return;
                                }
                                downLoadAll(tableName,new File(dbName));
                                break;
                            }
                            downLoad(s[1],new File(dbName));
                        } catch (Exception e){
                            System.out.println(e);
                        }
                        break;
                    default:
                        System.out.println(">>> Unrecognized Command, use help for more");
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }



    private void helpConsole() {
        System.out.println("Available Commands:");
        System.out.println("  Database Operations:");
        System.out.println("    init  [dbName]                -> Initialize a new database with the specified name");
        System.out.println("    use   [dbName] [tableName]    -> Select a database and table to work with");
        System.out.println("    dir   [dbName]                -> List all tables within the specified database");
        System.out.println("    kill  [target]                -> Remove an entire database or a single table. Specify 'entire db' or '[tableName] [dataFilename]'");

        System.out.println("\n  Table Operations:");
        System.out.println("    put   [filename.csv] [tableName]  -> Import data from a CSV file to a table. Use the same name for appending");
        System.out.println("    get   [filename]/[all] [tableName] -> Export data to a file or get all data from a table");
        System.out.println("    rm    [filename]                   -> Remove a file");

        System.out.println("\n  Data Manipulation:");
        System.out.println("    set   [key] [value]    -> Set a key-value pair");
        System.out.println("    add   [key] [value]    -> Add a new key-value pair");
        System.out.println("    del   [key]            -> Delete a key-value pair by key");
        System.out.println("    read                   -> Returns the loaded data files");
    }

    public boolean checkIfInserted(String filename){
       if (reads.get(filename) != null) return true;
           return false;
    }


    // init dbName
    public void init(String dbName) throws IOException {
        createConfigFile(SSTableList.CONFIG_FILE_PATH);
        initializeComponents(dbName); // Initialize common components
        // Create a File object for the data directory
        dataDirectory = new File(System.getProperty("user.dir"), dbName);
        ss.setDirectory(dataDirectory);

        // Attempt to create the directory if it doesn't exist
        if (!dataDirectory.exists()) {
            if (dataDirectory.mkdirs()) {
                System.out.println(">>> Data directory created successfully.");
            } else {
                System.err.println(">>> Failed to create data directory. Please check your permissions.");
            }
        }

        readyFlag = 1;
        System.out.println(">>> System initialized with database: " + this.dbName);
    }

    public void setDirectory(String dbName, String tableName) throws IOException {
        createConfigFile(SSTableList.CONFIG_FILE_PATH);
        initializeComponents(dbName); // Initialize common components
        if (reads.isEmpty())
        this.reads = readIniFileToMap();
        dataDirectory = new File(System.getProperty("user.dir"), dbName);
        ss.setDirectory(dataDirectory);

        this.tableName = tableName;

        if (!dataDirectory.exists()) {
            System.err.println(">>> Database directory does not exist. Please initialize it first.");
        } else {
            readyFlag = 1;
            System.out.println(">>> Database directory set to: " + this.dbName);
            System.out.println(">>> Table is set to : " + tableName);
        }
    }

    private void initializeComponents(String dbName) throws IOException {
        // Common component initialization for both init and setDirectory
        ss = new SSTableList();
        mm = new MeMEngine();
        mm.setDbName(dbName); // Assuming you want to set the dbName here. Adjust if needed.
        this.dbName = dbName;
    }



    public void open(String filename) throws IOException {
        if (filename.endsWith(".idx")) {
            // Use ss.loadIndexFile to load the item
            Map<String, Object> fileInfo = ss.loadIndexFile(filename);
            System.out.println(fileInfo.get("fileName"));
            System.out.println(fileInfo.get("FirstKeys"));

            return;
        }
        else if (filename.endsWith(".meta")) {
            ss.readMeta( dbName + "_" + tableName +"_meta.meta");
            return;
        }
        Map<String, Object> fileInfo = ss.open(filename);
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        System.out.println("Version : " + fileInfo.get("Version"));
        System.out.println("FileName : " + fileInfo.get("FileName"));
        System.out.println("UsedBlockNums : " + fileInfo.get("UsedBlocksCount"));
        System.out.println("UsedBlockFirstKeys : " + ((List)fileInfo.get("UsedBlockFirstKeys")).get(0) + "...");

        // Check if dataBlocksInfo has elements
        if (!dataBlocksInfo.isEmpty()) {
            System.out.println("First Data Block : " + dataBlocksInfo.get(0));

            // Print the last data block only if there is more than one block
            if (dataBlocksInfo.size() > 1) {
                System.out.println("Last Data Block : " + dataBlocksInfo.get(dataBlocksInfo.size() - 1));
            }
        } else {
            System.out.println(">>> No Data Blocks available.");
        }
    }

    public void flush(MeMEngine mm, String type) throws IOException {
        if (mm.getWritable() == null || mm.getWritable().size() == 0) {
            System.out.println("Memory has been unloaded");
        }
        if (type.equals("BTree")){
            mm.flush(1,dbName + "_" + tableName + "_" + getCurrentTimestamp() + "_last.db",
                dbName + "_" + tableName + "_id.idx");
        }
        mm.flush(dbName + "_" + tableName + "_" + getCurrentTimestamp() + "_last.db",
            dbName + "_" + tableName + "_id.idx");
    }



    public void get(String searchKey, SSTableList ss, MeMEngine mm) {
        if (searchKey == null || Integer.parseInt(searchKey) < 0) {
            System.out.println(">>> Invalid input");
            return;
        }
        try {
            Command c = mm.get(searchKey);
            if (c == null) {
                c = SSTableList.get(searchKey, dbName + "_" + tableName + "_id.idx", ss);
            }
            String fileName = dbName + "_" + tableName + "_small_id.idx";
            File file = new File(dbName + File.separator + fileName);
            if (c == null && file.exists()){
                c = SSTableList.get(searchKey, dbName + "_" + tableName + "_small_id.idx", ss);
            }
            if (c == null || "null".equals(c.getValue())) {
                System.out.println("Key not found or the associated value has been deleted.");
            } else {
                System.out.printf("Found key '%s' with value '%s'.%n%n", c.getKey(), c.getValue());
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: The key format is invalid. Please ensure you enter a valid key.");
        } catch (IOException e) {
            System.out.println("Error: An I/O exception occurred while trying to retrieve the data.");
            e.printStackTrace(); // Consider logging the stack trace or handling it more gracefully.
        }

    }

    public void del(String key,MeMEngine mm) throws IOException {
        // no need to update index, update the meta kv nums minus 1
        if (checkMemorySize(mm,key)){
            SSTableList.del(key,dbName + "_" + tableName + "_id.idx", new File(dbName));
            metaDataChange("del",ss,dbName,dataDirectory,tableName);
            return;
        }

        mm.del(key);
    }

    public void del(Scanner sc, MeMEngine mm) throws IOException {
        // no need to update index, update the meta kv nums minus 1
        System.out.println(">>> Enter the key to delete:");
        String key = sc.nextLine();

        if (checkMemorySize(mm,key)){
            SSTableList.del(key,dbName + "_" + tableName + "_id.idx", new File(dbName));
            metaDataChange("del",ss,dbName,dataDirectory,tableName);
            return;
        }

        mm.del(key);
    }

    public boolean checkMemorySize(MeMEngine mm, String key){
        return mm.getWritable() == null || mm.getWritable().getSize() == 0 ||
            key.compareTo(mm.getWritable().get().getKey()) < 0;
    }

    public void set(String key, String value, MeMEngine mm) throws IOException{
        if (checkMemorySize(mm,key)){
            SSTableList.set(key,dbName + "_" + tableName + "_id.idx", value, new File(dbName));
            return;
        }

        mm.set(key,value);
    }

    public void set(Scanner sc,  MeMEngine mm) throws IOException{
        System.out.println(">>> Enter the key to insert:");
        System.out.print(">>>");
        String key = sc.nextLine();
        System.out.println(">>> Enter the value to insert:");
        System.out.print(">>>");
        String value = sc.nextLine();
        if (checkMemorySize(mm,key)){
            SSTableList.set(key,dbName + "_" + tableName + "_id.idx", value, new File(dbName));
            return;
        }

        mm.set(key,value);
    }
    private void put(Scanner scanner, MeMEngine mm, SSTableList ss) {
        // update both index (first key may vary) and meta (kv nums + 1)
        System.out.println(">>> Enter the key to insert:");
        System.out.print(">>>");
        String key = scanner.nextLine();
        System.out.println(">>> Enter the value to insert:");
        System.out.print(">>>");
        String value = scanner.nextLine();


        try {
            String op = "";
            if (mm.getWritable().getSize() == 0 || key.compareTo(mm.getWritable().get().getKey()) < 0){
                boolean isReplaced = SSTableList.put(key,value,dbName + "_" + tableName + "_id.idx",new File(dbName));
                if (!isReplaced) op = "add";
                metaDataChange(op,ss,dbName ,dataDirectory,tableName);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid key format. Please enter a valid integer.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void put(Scanner sc, String[] kv, MeMEngine mm, SSTableList ss) {
        // update both index (first key may vary) and meta (kv nums + 1)
        String key = null, value;
        if (kv.length < 2){
            System.out.println(">>> Enter the value to insert:");
            System.out.print(">>>");
            value = sc.nextLine();
            if (value == null || value.length() == 0){
                value = "default";
            }
        } else {
            key = kv[1];
            value = kv[2];
        }

        try {
            String op = "";
            if (mm.getWritable().getSize() == 0 || key.compareTo(mm.getWritable().get().getKey()) < 0){
                boolean isReplaced = SSTableList.put(key,value,dbName + "_" + tableName + "_id.idx",new File(dbName));
                if (!isReplaced) op = "add";
                metaDataChange(op,ss,dbName ,dataDirectory,tableName);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid key format. Please enter a valid integer.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void processCSV(String inputFilePath, MeMEngine engine) throws IOException {
        String line;
        try ( var br = Files.newBufferedReader(Paths.get(inputFilePath))) {

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] columns = COMMA_OUTSIDE_QUOTES_PATTERN.split(line, -1);
                //String[] columns = line.split(",");
                if (columns.length >= 3) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 1; i < columns.length; i++) {
                        joiner.add(columns[i]);
                    }
                    if (engine.getWritable() instanceof BTree){
                        engine.put(1,columns[0], joiner.toString(),
                            dbName + "_" + tableName+ "_" + getCurrentTimestamp(), dbName + "_" + tableName + "_id.idx");

                    } else {
                        engine.put(new InsertCommand(columns[0], joiner.toString()),
                        dbName + "_" + tableName+ "_" + getCurrentTimestamp(), dbName + "_" + tableName + "_id.idx");
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void writeMeta(MeMEngine mm, boolean append) throws IOException {
        SSTableList ss = new SSTableList();
        ss.writeMeta(dbName,mm.getFiles(),mm.getKVNums(),dbName + "_" + tableName + "_meta.meta", append);
    }

    public void destroy(String fileName, MeMEngine mm) throws IOException {
        String tableName = fileName.split("_")[1];
        if (!tableName.equals(this.tableName)){
            System.out.println("Currently Managing different table, change table first");
            return;
        }

        String newFileName = dbName + File.separator + fileName;
        Path path = Paths.get(newFileName);

        if (isCriticalFile(fileName) && !confirmDeletion()) {
            System.out.println(">>> File deletion cancelled.");
            return;
        }

        updateMetaData(fileName, mm);
        updateIndex(fileName, mm);
        Files.deleteIfExists(path);
        System.out.println(">>> File deleted successfully.");

    }

    private boolean isCriticalFile(String fileName) {
        return fileName.endsWith(".idx") || fileName.endsWith(".meta");
    }

    private boolean confirmDeletion() {
        System.out.print(">>> The file is either an index or meta data. Are you sure you want to delete it (yes/no)? ");
        try (Scanner scanner = new Scanner(System.in)) {
            String confirmation = scanner.nextLine();
            return "yes".equalsIgnoreCase(confirmation);
        }
    }

    private void updateMetaData(String fileName, MeMEngine mm) throws IOException {
        Map<String, Object> meta = ss.readMeta(dbName +"_" + tableName+ "_meta.meta");
        Map<String, Object> detail = (Map<String, Object>) meta.get(dbName);
        List<String> fileNames = (List<String>) detail.get("fileNames");
        System.out.println(fileNames);
        System.out.println(fileName);
        int fileIndex = fileNames.indexOf(fileName);
        System.out.println(fileIndex);

        if (fileIndex != -1) {
            fileNames.remove(fileIndex);
            List<Integer> kvs = new ArrayList<>();
            int counter = 0;
            if (mm.getKvs().isEmpty()){
                Map<String, Object> data =  ss.open(fileName);
                List<List<Command>> dataBlocks = (List<List<Command>>) data.get("DataBlocksInfo");

                for (List<Command> block : dataBlocks){
                    counter += block.size();
                }

            } else kvs = mm.getKvs();
            int newTotalKv = ((int) detail.get("kvNums")) - (kvs.isEmpty() ? counter : kvs.get(fileIndex));
            detail.put("kvNums", newTotalKv);
            detail.put("fileNames", fileNames);
            meta.put(dbName, detail);
            File f = new File(dataDirectory, dbName + "_" + tableName + "_meta.meta");
            try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {
                file.setLength(0); // This will truncate the file to zero length, effectively clearing it
            }
            ss.writeMeta(dbName, fileNames, newTotalKv, dbName + "_" + tableName + "_meta.meta", false);
        }
    }

    private void updateIndex(String fileName, MeMEngine mm) throws IOException {
        Map<String, Object> index = ss.loadIndexFile(dbName + "_" + tableName + "_id.idx");
        List<String> ifileNames = (List<String>) index.get("fileName");
        List<String> firstKeys = (List<String>) index.get("FirstKeys");
        List<String> timeStamps = (List<String>) index.get("version");
        int indexToRemove = ifileNames.indexOf(fileName);

        if (indexToRemove != -1) {
            ifileNames.remove(indexToRemove);
            firstKeys.remove(indexToRemove);
            timeStamps.remove(indexToRemove);
            File f = new File(dataDirectory, dbName + "_" + tableName + "_id.idx");
            try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {
                file.setLength(0); // This will truncate the file to zero length, effectively clearing it
            }
            ss.updateIndex(firstKeys, ifileNames, timeStamps, dbName + "_" + tableName + "_id.idx");
        }
    }

    public void dir(String directoryPath){
        try {
            // Get all files from the directory
            Files.list(Paths.get(directoryPath))
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // Process each file
                        dir(file);
                    } catch (IOException e) {
                        System.err.println("Error processing file: " + file.getFileName());
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            System.err.println("Error listing files in the directory.");
            e.printStackTrace();
        }
    }

    private static void dir(Path filePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
            // Read the length of the timestamp
            int timestampLength = file.readInt();

            // Read the timestamp
            byte[] timestampBytes = new byte[timestampLength];
            file.readFully(timestampBytes);
            String timestamp = new String(timestampBytes);

            // Output the timestamp and file size
            System.out.printf("%-50s %-20s %d bytes%n", "File: " + filePath.getFileName(), "Timestamp: " + timestamp, file.length());
        }
    }

    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public static void metaDataChange(String operation, SSTableList ss, String dbName, File dataDirectory, String tableName) throws FileNotFoundException {
        int flag = 0;
        if (operation.equals("del")){
            flag = -1;
        } else if (operation.equals("add")) {
            flag = 1;
        }
        Map<String, Object> meta = ss.readMeta(dbName + "_" + tableName +"_meta.meta");
        Map<String, Object> detail = (Map<String, Object>) meta.get(dbName);
        int kvNums = (int) detail.get("kvNums");
        flag += kvNums;
        File f = new File(dataDirectory, dbName + "_" + tableName + "_meta.meta");
        try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {
            file.setLength(0); // This will truncate the file to zero length, effectively clearing it
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ss.writeMeta(dbName, (List<String>) detail.get("fileNames"), flag, dbName +"_" + tableName + "_meta.meta", false);
    }

    public void downLoad(String filename, File dataDirectory) throws IOException {
        SSTableList ss = new SSTableList();
        ss.loadConfig();
        ss.setDirectory(dataDirectory);
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        if (filename.endsWith(".idx")) {
            // Load the index file and write its contents to a text file
            Map<String, Object> fileInfo = ss.loadIndexFile(filename);
            writeIndexFileToText(fileInfo, filename, outputDir);
            System.out.println("Index file download successful");
            return;
        } else if (filename.endsWith(".meta")) {
            // Read meta data and write it to a text file
            Map<String, Object> metaData = ss.readMeta(filename);
            writeMetaToText(metaData, filename, outputDir);
            System.out.println("Meta file download successful");
            return;
        }
        Map<String, Object> fileInfo = ss.open(filename);
        ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");
        File csvOutputFile = new File("output" + File.separator + filename.replace(".db", ".csv"));
        try (FileWriter writer = new FileWriter(csvOutputFile)) {
            // Write CSV header
            writer.write("Key,Title,Genre\n");

            // Iterate through data blocks
            for (Object dataBlock : dataBlocksInfo) {
                List<?> commands = (List<?>) dataBlock; // Safe cast because we know the structure
                for (Object obj : commands) {
                    Command command = (Command) obj; // Cast to your Command object
                    writeCommandToCSV(writer, command);
                }
            }
            System.out.println("Data file download successful");
        }
    }

    public void downLoadAll(String tableName, File dataDirectory) throws IOException {
        SSTableList ss = new SSTableList();
        ss.loadConfig();
        ss.setDirectory(dataDirectory);
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            System.out.println("Database Does Not Exists");
            return;
        }
        File csvOutputFile = new File(outputDir, tableName + "_data.csv");

        // Start writing to CSV file
        try (FileWriter writer = new FileWriter(csvOutputFile)) {
            // Write CSV header once
            writer.write("Key,Title,Genre\n");

            // List all .db files in the data directory
            File[] dbFiles = dataDirectory.listFiles((dir, name) -> name.contains(tableName) && name.endsWith(".db"));
            if (dbFiles != null) {
                for (File dbFile : dbFiles) {
                    // Open each .db file and get data blocks info
                    Map<String, Object> fileInfo = ss.open(dbFile.getName());
                    ArrayList dataBlocksInfo = (ArrayList) fileInfo.get("DataBlocksInfo");

                    // Iterate through data blocks
                    for (Object dataBlock : dataBlocksInfo) {
                        List<?> commands = (List<?>) dataBlock; // Safe cast because we know the structure
                        for (Object obj : commands) {
                            Command command = (Command) obj; // Cast to your Command object
                            writeCommandToCSV(writer, command);
                        }
                    }
                }
            }
            System.out.println("Table "+ tableName +" data file download successful");
        }
    }

    private void writeCommandToCSV(FileWriter writer, Command command) throws IOException {
        String key = command.getKey(); // Directly accessing the key
        String value = command.getValue(); // Directly accessing the value

        // Regex to find the first comma that follows a pattern: (digits)
        Pattern pattern = DOWNLOAD_REGEX;
        Matcher matcher = pattern.matcher(value);

        if (matcher.find()) {
            String title = matcher.group(1); // The part before the first matching comma, including the year
            String genre = matcher.group(2).trim(); // The part after the comma, assumed to be the genre

            writer.write(String.format("%s,%s,%s\n", key, title, genre));
        } else {
            // Handle cases where the value doesn't match the expected format
            writer.write(String.format("%s,%s\n", key, value));
        }
    }


    private void writeIndexFileToText(Map<String, Object> fileInfo, String originalFilename, File directory) throws IOException {
        String textFilename = originalFilename.replace(".idx", ".txt");
        File textFile = new File(directory, textFilename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
            for (Map.Entry<String, Object> entry : fileInfo.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }

    private void writeMetaToText(Map<String, Object> metaData, String originalFilename, File directory) throws IOException {
        String textFilename = originalFilename.replace(".meta", ".txt");
        File textFile = new File(directory, textFilename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
            for (Map.Entry<String, Object> entry : metaData.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }


    public static void measureExecutionTime(Runnable task) {
        long startTime = System.nanoTime();  // Capture start time
        task.run();  // Execute the task
        long endTime = System.nanoTime();  // Capture end time

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;  // Convert duration to seconds
        System.out.println("Execution Time: " + durationInSeconds + " seconds");
    }

    // Method to delete all contents of a directory (including subdirectories and files)
    public static void deleteDirectoryWithContents(String directoryPath, Map<String,String> map, String dbName) {

        try {
            FileUtils.deleteDirectory(new File(directoryPath));
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (dbName.equals(entry.getValue())) {
                    iterator.remove(); // This removes the current entry from the map
                }
            }
            writeMapToIniFile(map);
            System.out.println(">>> Directory and all its contents have been successfully deleted.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(">>> An error occurred while trying to delete the directory.");
        }
    }

    public static void deleteFilesContainingTableName(String directoryPath, String tableName, String filename, Map<String, String> map) {
        Path path = Paths.get(directoryPath);

        // Use try-with-resources to automatically close the resources
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                // Check if it's a file and if the name contains the table name
                if (Files.isRegularFile(entry) && entry.getFileName().toString().contains(tableName)) {
                    try {
                        Files.delete(entry);
                        System.out.println("Deleted: " + entry);
                    } catch (IOException e) {
                        System.err.println("Error deleting file: " + entry + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directory: " + directoryPath + " - " + e.getMessage());
        }

        // Remove the entry from the map with the matching filename key
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            if (key.contains(filename)) { // Check if the key contains the filename
                iterator.remove(); // Safely remove the current entry from the map
                System.out.println("Removed file records from map with key: " + key);
            }
        }
        writeMapToIniFile(map);
    }

    public static void orderNoMatterInsertion(List<List<String>> data, File dataDirectory, String dbName, String tableName) throws IOException {
        SSTableList ss = new SSTableList();
        ss.setDirectory(dataDirectory);
        MeMEngine mm = new MeMEngine();
        mm.setDbName(dbName);
        int counter = 0;
        for (List<String> chunk : data){
            String[] Chunk = COMMA_OUTSIDE_QUOTES_PATTERN.split(chunk.get(0), -1);
            //System.out.println(firstOfEachChunk);

            for (String line : chunk){
                String[] columns = COMMA_OUTSIDE_QUOTES_PATTERN.split(line, -1);
                if (columns.length >= 3) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 1; i < columns.length; i++) {
                        joiner.add(columns[i]);
                    }
                    mm.put(new InsertCommand(columns[0], joiner.toString()),
                        dbName + "_" + tableName + "_small_spl_" + counter + ".db", dbName + "_" + tableName + "_id.idx");
                }

            }
            counter ++;
            mm.flush(dbName + "_" + tableName + "_small_" + counter + ".db",dbName + "_" + tableName + "_small" + "_id.idx");
            ss.writeMeta(dbName,mm.getFiles(),mm.getKVNums(),dbName + "_" + tableName + "_meta.meta", false);
        }
    }

    // after this, flush and write meta
    public static void compaction(String dbName, File dataDirectory, String tableName) throws IOException {
        SSTableList ss = new SSTableList();
        MeMEngine mm = new MeMEngine();

        ss.setDirectory(dataDirectory);
        mm.setDbName(dbName);

        // Construct index file names using dbName and tableName
        String smallIndexFileName = dbName + "_" + tableName + "_small_id.idx";
        String originalIndexFileName = dbName + "_" + tableName + "_id.idx";

        // Load index files
        Map<String, Object> smallIndex = ss.loadIndexFile(smallIndexFileName);
        Map<String, Object> originalIndex = ss.loadIndexFile(originalIndexFileName);

        // old files
        List<String> oldName = mergeNames(smallIndex,originalIndex);
        // Merge keys and names from the index files
        List<List<String>> mergedfileNames = findFiles(smallIndex, originalIndex);

        deleteFilesWithExtensions(dbName, tableName, ".idx",".meta");
        // filename blocks
        SkipList sl = new SkipList(0.5);
        for (List<String>  files : mergedfileNames){

            for (String filename : files){
                Map<String, Object> fileInfo = ss.open(filename);
                ArrayList dataBlocks = (ArrayList) fileInfo.get("DataBlocksInfo");
                for (Object block : dataBlocks) {
                    for (Object command : (List)block) {
                        sl.insert((Command) command);
                    }
                }
            }

        }
        for (Object node : sl){
            Command c =  ((Node) node).getCommand();
            String newFileName = dbName + "_" + tableName + "_" + getCurrentTimestamp();
            mm.put(c, newFileName, dbName + "_" + tableName + "_id.idx");
        }
        mm.flush(dbName + "_" + tableName + "_" + getCurrentTimestamp() + "_last.db",
            dbName + "_" + tableName + "_id.idx");
        ss.writeMeta(dbName,mm.getFiles(),mm.getKVNums(),dbName +"_" + tableName + "_meta.meta", false);

        deleteObsoleteFiles(oldName,dbName);
    }

    private static List<List<String>> findFiles(Map<String, Object> map1, Map<String, Object> map2) {
        List<String> smallNames = (List<String>) map1.get("fileName");
        List<String> smallKeys = (List<String>) map1.get("FirstKeys");
        List<String> orgNames = (List<String>) map2.get("fileName");
        List<String> orgKeys = (List<String>) map2.get("FirstKeys");

        Map<String, List<String>> associationMap = new HashMap<>();
        for (String orgName : orgNames) {
            associationMap.put(orgName, new ArrayList<>());
        }

        for (int i = 0; i < smallKeys.size(); i++) {
            int smallKey = Integer.parseInt(smallKeys.get(i));
            String smallName = smallNames.get(i);

            for (int j = 0; j < orgKeys.size(); j++) {
                int orgKey = Integer.parseInt(orgKeys.get(j));
                if (j == orgKeys.size() - 1 || smallKey < Integer.parseInt(orgKeys.get(j + 1))) {
                    associationMap.get(orgNames.get(j)).add(smallName);
                    break;
                }
            }
        }

        List<List<String>> resultList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : associationMap.entrySet()) {
            List<String> orgAndSmallFiles = new ArrayList<>();
            orgAndSmallFiles.add(entry.getKey()); // Add the org filename as the first element
            orgAndSmallFiles.addAll(entry.getValue()); // Add all the associated small filenames
            resultList.add(orgAndSmallFiles);
        }

        // Sort the resultList based on the numerical value before .db in the filenames
        Collections.sort(resultList, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                return extractNumber(o1.get(0)).compareTo(extractNumber(o2.get(0)));
            }

            private Integer extractNumber(String filename) {
                // Handling the case where the filename ends with "last"
                if (filename.contains("_last.db")) {
                    return Integer.MAX_VALUE; // Assign the maximum value to ensure it's sorted last
                }
                String[] parts = filename.split("_");
                String numberPart = parts[parts.length - 1];
                parts = numberPart.split("\\.");
                numberPart = parts[0];// The number is second last in the split array
                try {
                    return Integer.parseInt(numberPart);
                } catch (NumberFormatException e) {
                    return 0; // Default to 0 if parsing fails
                }
            }
        });

        return resultList;


    }

    public static void deleteObsoleteFiles(List<String> filenames, String directory) {
        for (String filename : filenames) {
            // Construct the full path for each file
            File file = new File(directory, filename);

            // Check if the file exists and is a file (not a directory)
            if (file.exists() && file.isFile()) {
                // Attempt to delete the file
                boolean deleted = file.delete();
                // Optionally, print a message indicating whether the deletion was successful
                if (deleted) {
                    System.out.println("Deleted: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to delete: " + file.getAbsolutePath());
                }
            } else {
                // If the file does not exist or is not a file, print a message
                System.out.println("File does not exist or is not a file: " + file.getAbsolutePath());
            }
        }
    }

    public static List<List<String>> readCSVAndDetectSequentialChunks(String inputFilePath, boolean header) {
        List<List<String>> sequentialChunks = new ArrayList<>();
        boolean flag = true;
        try (var br = Files.newBufferedReader(Paths.get(inputFilePath))) {
            if (header) br.readLine(); // skip the header
            String line;
            Integer previousKey = null; // Holds the last key to check for sequentiality
            List<String> currentChunk = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] columns = COMMA_OUTSIDE_QUOTES_PATTERN.split(line, -1); // Assuming the separator is a comma

                if (columns.length > 0) {
                    try {
                        Integer currentKey = Integer.parseInt(columns[0]); // Assuming the key is an integer

                        if (previousKey != null && currentKey != previousKey + 1) {
                            // If the keys are not sequential, start a new chunk
                            if (!currentChunk.isEmpty()) {
                                sequentialChunks.add(new ArrayList<>(currentChunk)); // Add the completed chunk to the list
                                currentChunk.clear(); // Start a new chunk
                            }
                        }

                        // Add the current line to the current chunk
                        currentChunk.add(line);
                        previousKey = currentKey; // Update the previousKey for the next iteration
                    } catch (NumberFormatException e) {
                        flag = false;
                        //System.out.println("There are empty values in the CSV file");
                    }
                }
            }
            if (!flag) {
                System.out.println(
                    "There are empty or Invalid value in the data, those lines are skipped");
            }
            // Add the last chunk if it's not empty
            if (!currentChunk.isEmpty()) {
                sequentialChunks.add(currentChunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortByFirstRecordFirstColumn(sequentialChunks);
        return sequentialChunks;
    }

    public static String getCurrentTimestamp() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        // Format the current date and time
        String formattedTimestamp = now.format(formatter);

        return formattedTimestamp;
    }

    public static void sortByFirstRecordFirstColumn(List<List<String>> data) {
        Collections.sort(data, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> list1, List<String> list2) {
                // Ensure both lists have at least one entry to avoid IndexOutOfBoundsException
                if (list1.isEmpty() && list2.isEmpty()) return 0;
                if (list1.isEmpty()) return -1;
                if (list2.isEmpty()) return 1;

                // Split the first record of each list to get the columns
                String[] columns1 = COMMA_OUTSIDE_QUOTES_PATTERN.split(list1.get(0), -1);
                String[] columns2 = COMMA_OUTSIDE_QUOTES_PATTERN.split(list2.get(0), -1);

                // Parse the first column of the first record from each list to integers, with error handling
                int key1 = 0, key2 = 0;
                try {
                    key1 = columns1.length > 0 ? Integer.parseInt(columns1[0]) : 0;
                } catch (NumberFormatException e) {
                    // Handle parsing error if necessary
                }
                try {
                    key2 = columns2.length > 0 ? Integer.parseInt(columns2[0]) : 0;
                } catch (NumberFormatException e) {
                    // Handle parsing error if necessary
                }

                // Compare the integer keys for ascending order
                return Integer.compare(key1, key2);
            }
        });

    }



    public static void deleteFilesWithExtensions(String folderPath, String small, String... extensions) {
        File folder = new File(folderPath);

        // Check if the folder exists and is indeed a directory
        if (folder.exists() && folder.isDirectory()) {
            // List all files and directories within the folder
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    // If it's a directory, recurse into it
                    if (file.isDirectory()) {
                        deleteFilesWithExtensions(file.getAbsolutePath(), small, extensions);
                    } else {
                        // Check if the file name contains the tableName and ends with any of the specified extensions
                        if (file.getName().toLowerCase().contains(small.toLowerCase())) {
                            for (String extension : extensions) {
                                if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                                    boolean deleted = file.delete();
                                    // Optionally, print a message if the file was successfully deleted
                                    if (deleted) {
                                        System.out.println("Deleted: " + file.getAbsolutePath());
                                    } else {
                                        System.out.println("Failed to delete: " + file.getAbsolutePath());
                                    }
                                    break; // No need to check other extensions if already deleted
                                }
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("The provided path is not a directory or doesn't exist: " + folderPath);
        }
    }


    public static List<String> mergeNames(Map<String, Object> map1, Map<String, Object> map2) {
        List<String> combinedNames = new ArrayList<>();
        List<String> names1 = (List<String>) map1.get("fileName");
        List<String> names2 = (List<String>) map2.get("fileName");
        combinedNames.addAll(names2);
        combinedNames.addAll(names1);
        // Return the combined names list
        return combinedNames;
    }

    public static void createConfigFile(String filePath) {
        File configFile = new File(filePath);

        // Check if the file already exists
        if (!configFile.exists()) {
            // Define the content of the INI file
            String[] settings = {
                "[Settings]",
                "version = 1",
                "MAX_FILE_SIZE = 1048576",
                "BLOCK_SIZE = 256",
                "HEAD_BLOCK_SIZE = 40000",
                "MULTIPLIER = 0.85",
                "META_BLOCK_SIZE = 128",
                "",
                "[Console]",
                "COMMA_OUTSIDE_QUOTES_PATTERN = (?=([^\"]*\"[^\"]*\")*[^\"]*$)",
                "DOWNLOAD_REGEX = (.*?\\(\\d+\\)),(.*)"
            };

            // Write to the file
            try (FileWriter writer = new FileWriter(configFile)) {
                for (String line : settings) {
                    writer.write(line + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> Using existing " + filePath);
        }
    }
}
