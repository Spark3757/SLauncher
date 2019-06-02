package ru.spark.slauncher.ui.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public interface Refreshable {
    void refresh();

    default BooleanProperty canRefreshProperty() {
        return new SimpleBooleanProperty(false);
    }
}
