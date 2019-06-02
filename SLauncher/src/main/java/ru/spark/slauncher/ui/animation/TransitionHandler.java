package ru.spark.slauncher.ui.animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public final class TransitionHandler implements AnimationHandler {
    private final StackPane view;
    private final StackPane EMPTY_PANE = new StackPane();
    private Timeline animation;
    private Duration duration;
    private Node previousNode, currentNode;

    /**
     * @param view A stack pane that contains another control that is {@link Parent}
     */
    public TransitionHandler(StackPane view) {
        this.view = view;
        currentNode = view.getChildren().stream().findFirst().orElse(null);

        // prevent content overflow
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);
    }

    @Override
    public Node getPreviousNode() {
        return previousNode;
    }

    @Override
    public Node getCurrentNode() {
        return currentNode;
    }

    @Override
    public StackPane getCurrentRoot() {
        return view;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public void setContent(Node newView, AnimationProducer transition) {
        setContent(newView, transition, Duration.millis(320));
    }

    public void setContent(Node newView, AnimationProducer transition, Duration duration) {
        this.duration = duration;

        Timeline prev = animation;
        if (prev != null)
            prev.stop();

        updateContent(newView);

        transition.init(this);

        // runLater or "init" will not work
        Platform.runLater(() -> {
            Timeline nowAnimation = new Timeline();
            nowAnimation.getKeyFrames().addAll(transition.animate(this));
            nowAnimation.getKeyFrames().add(new KeyFrame(duration, e -> {
                view.setMouseTransparent(false);
                view.getChildren().remove(previousNode);
            }));
            nowAnimation.play();
            animation = nowAnimation;
        });
    }

    private void updateContent(Node newView) {
        if (view.getWidth() > 0 && view.getHeight() > 0) {
            previousNode = currentNode;
            if (previousNode == null)
                previousNode = EMPTY_PANE;
        } else
            previousNode = EMPTY_PANE;

        if (previousNode == newView)
            previousNode = EMPTY_PANE;

        view.setMouseTransparent(true);

        currentNode = newView;

        view.getChildren().setAll(previousNode, currentNode);
    }
}
