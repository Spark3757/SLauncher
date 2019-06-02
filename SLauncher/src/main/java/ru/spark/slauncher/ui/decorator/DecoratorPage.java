package ru.spark.slauncher.ui.decorator;

import javafx.beans.property.ReadOnlyStringProperty;

public interface DecoratorPage {
    ReadOnlyStringProperty titleProperty();

    default boolean canForceToClose() {
        return false;
    }

    default boolean onClose() {
        return true;
    }

    default void onForceToClose() {
    }
}
