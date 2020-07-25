package ru.spark.slauncher.ui.wizard;

import javafx.scene.Node;
import ru.spark.slauncher.task.Task;

import java.util.Map;

public interface WizardDisplayer {
    default void onStart() {
    }

    default void onEnd() {
    }

    default void onCancel() {
    }

    void navigateTo(Node page, Navigation.NavigationDirection nav);

    void handleTask(Map<String, Object> settings, Task<?> task);
}