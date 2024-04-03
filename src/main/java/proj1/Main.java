package proj1;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/3/29 16:47
 *@Title  :
 */

import java.io.IOException;
import proj1.lsmtree.application.Console;

public class Main {

    public static void main(String[] args) throws IOException {
        try{
            Console c = new Console();
            c.menu();
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
