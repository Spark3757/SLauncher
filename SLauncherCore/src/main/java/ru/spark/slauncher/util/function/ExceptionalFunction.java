package ru.spark.slauncher.util.function;

/**
 * @author Spark1337
 */
public interface ExceptionalFunction<T, R, E extends Exception> {
    static <T, E extends RuntimeException> ExceptionalFunction<T, T, E> identity() {
        return t -> t;
    }

    R apply(T t) throws E;
}
