package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalRunnable;

import java.util.concurrent.Future;

/**
 * Determines how a task is executed.
 *
 * @author Spark1337
 */
public abstract class Scheduler {

    /**
     * Schedules the given task.
     *
     * @return the future
     */
    public abstract Future<?> schedule(ExceptionalRunnable<?> block);

}
