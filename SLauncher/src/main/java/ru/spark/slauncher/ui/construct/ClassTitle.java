package ru.spark.slauncher.ui.construct;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import ru.spark.slauncher.util.Lang;

/**
 * @author Spark1337
 */
public class ClassTitle extends StackPane {
    private final Node content;

    public ClassTitle(String text) {
        this(new Text(text));
    }

    public ClassTitle(Node content) {
        this.content = content;

        VBox vbox = new VBox();
        vbox.getChildren().addAll(content);
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(vbox.widthProperty());
        rectangle.setHeight(1.0);
        rectangle.setFill(Color.GRAY);
        vbox.getChildren().add(rectangle);
        getChildren().setAll(vbox);
        getStyleClass().add("class-title");
    }

    public ClassTitle(String text, Node rightNode) {
        this(Lang.apply(new BorderPane(), borderPane -> {
            borderPane.setLeft(Lang.apply(new VBox(), vBox -> vBox.getChildren().setAll(new Text(text))));
            borderPane.setRight(rightNode);
        }));
    }

    public Node getContent() {
        return content;
    }
}
