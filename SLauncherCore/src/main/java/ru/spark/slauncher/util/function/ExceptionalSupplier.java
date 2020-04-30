package ru.spark.slauncher.util.function;

import java.util.concurrent.Callable;

/**
 * @author spark1337
 */
public interface ExceptionalSupplier<R, E extends Exception> {
    R get() throws E;

    default Callable<R> toCallable() {
        return this::get;
    }
}
