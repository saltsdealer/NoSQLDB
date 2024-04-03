package proj1.btree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/4/3 0:39
 *@Title  :
 */

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import proj1.lsmtree.IMTable;
import proj1.lsmtree.impl.Command;
import proj1.lsmtree.model.DelCommand;
import proj1.lsmtree.model.SetCommand;

public class PackedBTree implements IMTable {
    private List<Command> commands;
    private BTree tree;

    public PackedBTree(BTree tree) {
        this.commands = tree.getSortedCommands();
        this.tree = tree;
    }

    @Override
    public void set(SetCommand setCommand) {
        tree.set(setCommand);
    }

    @Override
    public void del(DelCommand deleteCommand) {
        tree.del(deleteCommand);
    }

    @Override
    public boolean insert(Command entry) {
        return tree.insert(entry);
    }

    @Override
    public Command get() {
        return tree.get();
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public Object getRawData() {
        return null;
    }

    @Override
    public int getSize() {
        return tree.getSize();
    }

    @Override
    public Command search(String key) {
        return tree.search(key);
    }

    // Here's the modified iterator method
    @Override
    public Iterator<Command> iterator() {
        return commands.iterator(); // Returns an iterator over the commands list
    }

    @Override
    public void forEach(Consumer action) {
        if (commands != null) {
            commands.forEach(action);
        }
    }

    @Override
    public Spliterator<Command> spliterator() {
        return null;
    }

}
