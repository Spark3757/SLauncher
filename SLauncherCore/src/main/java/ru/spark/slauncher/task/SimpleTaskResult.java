package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalSupplier;

import java.util.concurrent.Callable;

/**
 * @author Spark1337
 */
class SimpleTaskResult<V> extends TaskResult<V> {

    private final Callable<V> callable;

    public SimpleTaskResult(Callable<V> callable) {
        this.callable = callable;
    }

    public SimpleTaskResult(ExceptionalSupplier<V, ?> supplier) {
        this.callable = supplier.toCallable();
    }

    @Override
    public void execute() throws Exception {
        setResult(callable.call());
    }
}
