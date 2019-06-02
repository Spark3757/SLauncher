package ru.spark.slauncher.ui.wizard;

import javafx.scene.Node;

import java.util.Map;

public interface WizardProvider {
    void start(Map<String, Object> settings);

    Object finish(Map<String, Object> settings);

    Node createPage(WizardController controller, int step, Map<String, Object> settings);

    boolean cancel();

    interface FailureCallback {
        void onFail(Map<String, Object> settings, Exception exception, Runnable next);
    }
}
