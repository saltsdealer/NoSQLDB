// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import java.nio.ByteBuffer;
import proj1.lsmtree.CommandEnum;

public abstract class Command implements Comparable<Command>{

  protected String key;
  protected String value;
  int byteLength;

  // Parameterized constructor to initialize key and value

  // Abstract method to get the command type


  public Command(String key, String value) {
    this.key = key;
    this.value = value;
    if (key == null || value == null){
      this.byteLength = 0;
    } else {
      this.byteLength = key.getBytes().length + value.getBytes().length + 10;
    }
  }

  public abstract CommandEnum getCommand();

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int compareTo(Command o) {

    return Integer.compare(Integer.parseInt(this.getKey()), Integer.parseInt(o.getKey()));
  }

  // Override toString for better readability and debugging

  public byte[] toBytes(){
    // ke(key-len(int) +y-value +cmd(int)+ value-len(int) + value+))+
    byte[] keyBytes = key.getBytes();
    byte[] valueBytes  = new byte[0];
    ByteBuffer byteBuffer  =null;

    if(getCommand().equals(CommandEnum.DELETE)){
      byteBuffer = ByteBuffer.allocate(2+keyBytes.length+4+4+0);
    }else{
      valueBytes = value.getBytes();
      byteBuffer = ByteBuffer.allocate(4+keyBytes.length+4+4+valueBytes.length);
    }
    //System.out.println(keyBytes.length);


    byteBuffer.putInt(keyBytes.length);
    byteBuffer.put(keyBytes);
    byteBuffer.putInt(getCommand().getFlag());

    if(getCommand().equals(CommandEnum.DELETE)){
      byteBuffer.putInt(0);
    }else{
      byteBuffer.putInt(valueBytes.length);
      byteBuffer.put(valueBytes);
    }

    return byteBuffer.array();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{key='" + key + "', value='" + value + "'}";
  }

}