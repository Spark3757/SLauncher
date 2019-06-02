package ru.spark.slauncher.task;

import java.util.Arrays;
import java.util.Collection;

/**
 * The tasks that provides a way to execute tasks parallelly.
 * Fails when some of {@link #tasks} failed.
 *
 * @author Spark1337
 */
public final class ParallelTask extends Task {

    private final Collection<Task> tasks;

    /**
     * Constructor.
     *
     * @param tasks the tasks that can be executed parallelly.
     */
    public ParallelTask(Task... tasks) {
        this.tasks = Arrays.asList(tasks);
        setSignificance(TaskSignificance.MINOR);
    }

    public ParallelTask(Collection<Task> tasks) {
        this.tasks = tasks;
        setSignificance(TaskSignificance.MINOR);
    }

    @Override
    public void execute() {
    }

    @Override
    public Collection<Task> getDependents() {
        return tasks;
    }

}
