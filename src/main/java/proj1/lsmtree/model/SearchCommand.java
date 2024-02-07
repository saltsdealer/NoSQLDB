package proj1.lsmtree.model;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:48
 *@Title  :
 */

import proj1.lsmtree.CommandEnum;
import proj1.lsmtree.impl.Command;

public class SearchCommand extends Command {

  public SearchCommand(String key) {
    this.key = key; // Pass null for value, as it's not used in search
  }

  @Override
  public CommandEnum getCommand() {
    return CommandEnum.SEARCH;
  }
}
