package ru.spark.slauncher.ui.construct;

import ru.spark.slauncher.ui.Controllers;

/**
 * @author spark1337
 * @see Controllers#dialog(javafx.scene.layout.Region)
 */
public interface DialogAware {

    default void onDialogShown() {
    }

}
