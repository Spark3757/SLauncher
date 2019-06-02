package ru.spark.slauncher.util.function;

/**
 * @author Spark1337
 */
public interface ExceptionalConsumer<T, E extends Exception> {
    static <T, E extends Exception> ExceptionalConsumer<T, E> fromRunnable(ExceptionalRunnable<E> runnable) {
        return new ExceptionalConsumer<T, E>() {
            @Override
            public void accept(T o) throws E {
                runnable.run();
            }

            @Override
            public String toString() {
                return runnable.toString();
            }
        };
    }

    static <T> ExceptionalConsumer<T, ?> empty() {
        return s -> {
        };
    }

    void accept(T t) throws E;
}
