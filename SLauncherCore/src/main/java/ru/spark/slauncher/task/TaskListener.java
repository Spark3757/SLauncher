package ru.spark.slauncher.task;

import java.util.EventListener;

/**
 * @author Spark1337
 */
public abstract class TaskListener implements EventListener {

    public void onStart() {
    }

    public void onReady(Task task) {
    }

    public void onRunning(Task task) {
    }

    public void onFinished(Task task) {
    }

    public void onFailed(Task task, Throwable throwable) {
        onFinished(task);
    }

    public void onStop(boolean success, TaskExecutor executor) {
    }

    public static class DefaultTaskListener extends TaskListener {
        public static final DefaultTaskListener INSTANCE = new DefaultTaskListener();

        private DefaultTaskListener() {
        }
    }
}
