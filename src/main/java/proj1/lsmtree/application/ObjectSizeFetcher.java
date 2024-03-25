package proj1.lsmtree.application;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/3/24 21:19
 *@Title  :
 */
import java.lang.instrument.Instrumentation;

public class ObjectSizeFetcher {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long getObjectSize(Object object) {
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation is not initialized.");
        }
        return instrumentation.getObjectSize(object);
    }
}
