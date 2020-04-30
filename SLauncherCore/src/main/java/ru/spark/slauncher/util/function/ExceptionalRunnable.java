package ru.spark.slauncher.util.function;

import java.util.concurrent.Callable;

/**
 * @author spark1337
 */
public interface ExceptionalRunnable<E extends Exception> {

    void run() throws E;

    default Callable<Void> toCallable() {
        return () -> {
            run();
            return null;
        };
    }

}
