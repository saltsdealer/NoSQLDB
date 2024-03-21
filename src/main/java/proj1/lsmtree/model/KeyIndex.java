package proj1.lsmtree.model;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/3/10 18:45
 *@Title  :
 *  so the indexing : file name + first data id  
 */

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import proj1.lsmtree.CommandEnum;

public class KeyIndex {
    
    private String filename;
    private String key;

    public KeyIndex(String filename, String key) {
        this.filename = filename;
        this.key = key;
    }

    public byte[] toBytes() {
        byte[] filenameBytes = filename.getBytes();
        byte[] keyBytes = key.getBytes();
        // 4 bytes for the length of the filename, filename bytes, 4 bytes for the length of the key, and key bytes
        ByteBuffer buffer = ByteBuffer.allocate(4 + filenameBytes.length + 4 + keyBytes.length);
        buffer.putInt(filenameBytes.length); // Length of filename
        buffer.put(filenameBytes); // Filename bytes
        buffer.putInt(keyBytes.length); // Length of key
        buffer.put(keyBytes); // Key bytes
        return buffer.array();
    }
}



