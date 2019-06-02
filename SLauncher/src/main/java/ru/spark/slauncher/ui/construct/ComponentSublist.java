package ru.spark.slauncher.ui.construct;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

@DefaultProperty("content")
public final class ComponentSublist extends ComponentList {

    private final ObjectProperty<Node> headerLeft = new SimpleObjectProperty<>(this, "headerLeft");
    private final ObjectProperty<Node> headerRight = new SimpleObjectProperty<>(this, "headerRight");

    public ComponentSublist() {
        super();
    }

    public Node getHeaderLeft() {
        return headerLeft.get();
    }

    public void setHeaderLeft(Node headerLeft) {
        this.headerLeft.set(headerLeft);
    }

    public ObjectProperty<Node> headerLeftProperty() {
        return headerLeft;
    }

    public Node getHeaderRight() {
        return headerRight.get();
    }

    public void setHeaderRight(Node headerRight) {
        this.headerRight.set(headerRight);
    }

    public ObjectProperty<Node> headerRightProperty() {
        return headerRight;
    }
}
