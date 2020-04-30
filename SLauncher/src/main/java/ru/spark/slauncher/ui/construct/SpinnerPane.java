package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXSpinner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.animation.AnimationHandler;
import ru.spark.slauncher.ui.animation.AnimationProducer;
import ru.spark.slauncher.ui.animation.ContainerAnimations;

@DefaultProperty("content")
public class SpinnerPane extends Control {
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading");

    public void showSpinner() {
        setLoading(true);
    }

    public void hideSpinner() {
        setLoading(false);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    private static class Skin extends SkinBase<SpinnerPane> {
        private final JFXSpinner spinner = new JFXSpinner();
        private final StackPane contentPane = new StackPane();
        private final StackPane topPane = new StackPane();
        private final StackPane root = new StackPane();
        private Timeline animation;

        protected Skin(SpinnerPane control) {
            super(control);

            root.getStyleClass().add("spinner-pane");
            topPane.getChildren().setAll(spinner);
            root.getChildren().setAll(contentPane, topPane);
            FXUtils.onChangeAndOperate(getSkinnable().content, newValue -> contentPane.getChildren().setAll(newValue));
            getChildren().setAll(root);

            FXUtils.onChangeAndOperate(getSkinnable().loadingProperty(), newValue -> {
                Timeline prev = animation;
                if (prev != null) prev.stop();

                AnimationProducer transition;
                topPane.setMouseTransparent(true);
                topPane.setVisible(true);
                topPane.getStyleClass().add("gray-background");
                if (newValue)
                    transition = ContainerAnimations.FADE_IN.getAnimationProducer();
                else
                    transition = ContainerAnimations.FADE_OUT.getAnimationProducer();

                AnimationHandler handler = new AnimationHandler() {
                    @Override
                    public Duration getDuration() {
                        return Duration.millis(160);
                    }

                    @Override
                    public Pane getCurrentRoot() {
                        return root;
                    }

                    @Override
                    public Node getPreviousNode() {
                        return null;
                    }

                    @Override
                    public Node getCurrentNode() {
                        return topPane;
                    }
                };

                Timeline now = new Timeline();
                now.getKeyFrames().addAll(transition.animate(handler));
                now.getKeyFrames().add(new KeyFrame(handler.getDuration(), e -> {
                    topPane.setMouseTransparent(!newValue);
                    topPane.setVisible(newValue);
                }));
                now.play();
                animation = now;
            });
        }
    }
}
