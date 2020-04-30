package ru.spark.slauncher.util.function;

public interface ExceptionalPredicate<T, E extends Exception> {
    boolean test(T t) throws E;
}
