package ru.spark.slauncher.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface FutureCallback<T> {
    void call(T obj, Runnable resolve, Consumer<String> reject);
}
