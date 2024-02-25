// Group 8:
// Tairan Ren 002772875
// Quan Yuan 002703792

package proj1.lsmtree.impl;

import proj1.lsmtree.CommandEnum;

public abstract class Command implements Comparable<Command>{

  protected String key;
  protected String value;

  // Parameterized constructor to initialize key and value

  // Abstract method to get the command type
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
  @Override
  public String toString() {
    return getClass().getSimpleName() + "{key='" + key + "', value='" + value + "'}";
  }

}