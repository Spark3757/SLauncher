package ru.spark.slauncher.ui.wizard;

import javafx.scene.Node;
import ru.spark.slauncher.task.Task;

import java.util.Map;

public interface WizardDisplayer {
    void onStart();

    void onEnd();

    void onCancel();

    void navigateTo(Node page, Navigation.NavigationDirection nav);

    void handleTask(Map<String, Object> settings, Task<?> task);
}
