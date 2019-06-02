package ru.spark.slauncher.ui.wizard;

import java.util.Map;

public interface WizardPage {
    default void onNavigate(Map<String, Object> settings) {
    }

    void cleanup(Map<String, Object> settings);

    String getTitle();
}
