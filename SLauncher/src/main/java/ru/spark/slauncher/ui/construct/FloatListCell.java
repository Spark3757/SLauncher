package ru.spark.slauncher.ui.construct;

import com.jfoenix.effects.JFXDepthManager;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.FXUtils;

public abstract class FloatListCell<T> extends ListCell<T> {
    private final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    protected final StackPane pane = new StackPane();

    {
        setText(null);
        setGraphic(null);

        pane.getStyleClass().add("card");
        pane.setCursor(Cursor.HAND);
        setPadding(new Insets(9, 9, 0, 9));
        JFXDepthManager.setDepth(pane, 1);

        FXUtils.onChangeAndOperate(selectedProperty(), selected -> {
            pane.pseudoClassStateChanged(SELECTED, selected);
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateControl(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(pane);
        }
    }

    protected abstract void updateControl(T dataItem, boolean empty);
}
