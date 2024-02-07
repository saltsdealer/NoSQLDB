package proj1.lsmtree.model;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:47
 *@Title  :
 */

import proj1.lsmtree.CommandEnum;
import proj1.lsmtree.impl.Command;

public class SetCommand extends Command {

    public SetCommand(String key,String value){

        this.key = key;
        this.value = value;
    }
    @Override
    public CommandEnum getCommand() {
        return CommandEnum.SET;
    }
}