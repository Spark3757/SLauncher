package ru.spark.slauncher.ui.construct;

import javafx.scene.Node;
import ru.spark.slauncher.ui.FXUtils;

public class IconedMenuItem extends IconedItem {

    public IconedMenuItem(Node node, String text, Runnable action) {
        super(node, text);

        getStyleClass().setAll("iconed-menu-item");
        setOnMouseClicked(e -> action.run());
    }

    public IconedMenuItem addTooltip(String tooltip) {
        FXUtils.installFastTooltip(this, tooltip);
        return this;
    }
}
