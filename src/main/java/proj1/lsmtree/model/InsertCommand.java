package proj1.lsmtree.model;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:47
 *@Title  :
 */

import proj1.lsmtree.CommandEnum;
import proj1.lsmtree.impl.Command;

public class InsertCommand extends Command {

  String value;
  String key;

  public InsertCommand(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public CommandEnum getCommand() {
    return CommandEnum.INSERT;
  }

  // apparently this is only for proj demo
  @Override
  public String toString() {
    return key;
  }
}
