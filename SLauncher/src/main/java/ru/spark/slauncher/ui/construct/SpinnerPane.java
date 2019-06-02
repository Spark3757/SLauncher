package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXSpinner;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.ui.animation.ContainerAnimations;
import ru.spark.slauncher.ui.animation.TransitionHandler;

@DefaultProperty("content")
public class SpinnerPane extends StackPane {
    private final TransitionHandler transitionHandler = new TransitionHandler(this);
    private final JFXSpinner spinner = new JFXSpinner();
    private final StackPane contentPane = new StackPane();
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading") {
        protected void invalidated() {
            if (get())
                transitionHandler.setContent(spinner, ContainerAnimations.FADE.getAnimationProducer());
            else
                transitionHandler.setContent(contentPane, ContainerAnimations.FADE.getAnimationProducer());
        }
    };

    public SpinnerPane() {
        getStyleClass().add("spinner-pane");

        getChildren().setAll(contentPane);

        content.addListener((a, b, newValue) -> contentPane.getChildren().setAll(newValue));
    }

    public void showSpinner() {
        setLoading(true);
    }

    public void hideSpinner() {
        setLoading(false);
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }
}
