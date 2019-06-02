package ru.spark.slauncher.util.javafx;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * @author Spark1337
 */
public class AutomatedToggleGroup extends ToggleGroup {

    private final ObservableList<? extends Toggle> toggles;
    private final ListChangeListener<Toggle> listListener;

    public AutomatedToggleGroup(ObservableList<? extends Toggle> toggles) {
        this.toggles = toggles;

        listListener = change -> {
            while (change.next()) {
                change.getRemoved().forEach(it -> it.setToggleGroup(null));
                change.getAddedSubList().forEach(it -> it.setToggleGroup(this));
            }
        };
        toggles.addListener(listListener);

        toggles.forEach(it -> it.setToggleGroup(this));
    }

    public void disconnect() {
        toggles.removeListener(listListener);
        toggles.forEach(it -> it.setToggleGroup(null));
    }
}
