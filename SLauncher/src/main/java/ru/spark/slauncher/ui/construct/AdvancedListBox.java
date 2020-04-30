package ru.spark.slauncher.ui.construct;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.ui.FXUtils;

public class AdvancedListBox extends ScrollPane {
    private final VBox container = new VBox();

    {
        setContent(container);

        FXUtils.smoothScrolling(this);

        setFitToHeight(true);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);

        container.setSpacing(5);
        container.getStyleClass().add("advanced-list-box-content");
    }

    public AdvancedListBox add(Node child) {
        if (child instanceof Pane || child instanceof AdvancedListItem)
            container.getChildren().add(child);
        else {
            StackPane pane = new StackPane();
            pane.getStyleClass().add("advanced-list-box-item");
            pane.getChildren().setAll(child);
            container.getChildren().add(pane);
        }
        return this;
    }

    public AdvancedListBox add(int index, Node child) {
        if (child instanceof Pane || child instanceof AdvancedListItem)
            container.getChildren().add(index, child);
        else {
            StackPane pane = new StackPane();
            pane.getStyleClass().add("advanced-list-box-item");
            pane.getChildren().setAll(child);
            container.getChildren().add(index, pane);
        }
        return this;
    }

    public AdvancedListBox remove(Node child) {
        container.getChildren().remove(indexOf(child));
        return this;
    }

    public int indexOf(Node child) {
        if (child instanceof Pane) {
            return container.getChildren().indexOf(child);
        } else {
            for (int i = 0; i < container.getChildren().size(); ++i) {
                Node node = container.getChildren().get(i);
                if (node instanceof StackPane) {
                    ObservableList<Node> list = ((StackPane) node).getChildren();
                    if (list.size() == 1 && list.get(0) == child)
                        return i;
                }
            }
            return -1;
        }
    }

    public AdvancedListBox startCategory(String category) {
        return add(new ClassTitle(category));
    }

    public void setSpacing(double spacing) {
        container.setSpacing(spacing);
    }

    public void clear() {
        container.getChildren().clear();
    }
}
