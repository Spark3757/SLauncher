package ru.spark.slauncher.util.function;

/**
 * @author spark1337
 */
public interface ExceptionalFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, E extends RuntimeException> ExceptionalFunction<T, T, E> identity() {
        return t -> t;
    }
}
