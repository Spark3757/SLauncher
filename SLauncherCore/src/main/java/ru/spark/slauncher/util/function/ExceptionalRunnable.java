package ru.spark.slauncher.util.function;

import java.util.concurrent.Callable;

/**
 * @author Spark1337
 */
public interface ExceptionalRunnable<E extends Exception> {

    static ExceptionalRunnable<?> fromRunnable(Runnable r) {
        return r::run;
    }

    void run() throws E;

    default Callable<?> toCallable() {
        return () -> {
            run();
            return null;
        };
    }

}
