package proj1.lsmtree.model;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:47
 *@Title  :
 */

import proj1.lsmtree.CommandEnum;
import proj1.lsmtree.impl.Command;

public class DelCommand extends Command {

    public DelCommand(String key){
        this.key  = key;

    }
    @Override
    public CommandEnum getCommand() {
        return CommandEnum.DELETE;
    }
}
