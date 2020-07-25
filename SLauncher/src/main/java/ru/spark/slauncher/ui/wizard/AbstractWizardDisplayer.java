package ru.spark.slauncher.ui.wizard;

import javafx.scene.control.Label;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.construct.TaskListPane;

import java.util.Map;
import java.util.Queue;

public abstract class AbstractWizardDisplayer implements WizardDisplayer {
    private final Queue<Object> cancelQueue;

    public AbstractWizardDisplayer(Queue<Object> cancelQueue) {
        this.cancelQueue = cancelQueue;
    }

    @Override
    public void handleTask(Map<String, Object> settings, Task<?> task) {
        TaskExecutor executor = task.withRunAsync(Schedulers.javafx(), this::navigateToSuccess).executor();
        TaskListPane pane = new TaskListPane();
        pane.setExecutor(executor);
        navigateTo(pane, Navigation.NavigationDirection.FINISH);
        cancelQueue.add(executor);
        executor.start();
    }

    @Override
    public void onCancel() {
        while (!cancelQueue.isEmpty()) {
            Object x = cancelQueue.poll();
            if (x instanceof TaskExecutor) ((TaskExecutor) x).cancel();
            else if (x instanceof Thread) ((Thread) x).interrupt();
            else throw new IllegalStateException("Unrecognized cancel queue element: " + x);
        }
    }

    void navigateToSuccess() {
        navigateTo(new Label("Successful"), Navigation.NavigationDirection.FINISH);
    }
}