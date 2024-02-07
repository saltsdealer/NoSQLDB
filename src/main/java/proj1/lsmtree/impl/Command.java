package proj1.lsmtree.impl;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:11
 *@Title  :
 */

import proj1.lsmtree.CommandEnum;

public abstract class Command {

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

  // Override toString for better readability and debugging
  @Override
  public String toString() {
    return getClass().getSimpleName() + "{key='" + key + "', value='" + value + "'}";
  }

}