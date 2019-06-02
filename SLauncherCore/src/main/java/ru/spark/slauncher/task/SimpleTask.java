package ru.spark.slauncher.task;

import ru.spark.slauncher.util.function.ExceptionalRunnable;

/**
 * @author Spark1337
 */
class SimpleTask extends Task {

    private final ExceptionalRunnable<?> closure;
    private final Scheduler scheduler;

    public SimpleTask(String name, ExceptionalRunnable<?> closure, Scheduler scheduler) {
        this.closure = closure;
        this.scheduler = scheduler;

        if (name == null) {
            setSignificance(TaskSignificance.MINOR);
            setName(closure.toString());
        } else {
            setName(name);
        }
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void execute() throws Exception {
        closure.run();
    }
}
