package ru.spark.slauncher.ui.animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ru.spark.slauncher.ui.FXUtils;

public class TransitionPane extends StackPane implements AnimationHandler {
    private Duration duration;
    private Node previousNode, currentNode;

    {
        currentNode = getChildren().stream().findFirst().orElse(null);
        FXUtils.setOverflowHidden(this);
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
        return this;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public void setContent(Node newView, AnimationProducer transition) {
        setContent(newView, transition, Duration.millis(160));
    }

    public void setContent(Node newView, AnimationProducer transition, Duration duration) {
        this.duration = duration;

        updateContent(newView);

        transition.init(this);

        // runLater or "init" will not work
        Platform.runLater(() -> {
            Timeline newAnimation = new Timeline();
            newAnimation.getKeyFrames().addAll(transition.animate(this));
            newAnimation.getKeyFrames().add(new KeyFrame(duration, e -> {
                setMouseTransparent(false);
                getChildren().remove(previousNode);
            }));
            FXUtils.playAnimation(this, "transition_pane", newAnimation);
        });
    }

    private void updateContent(Node newView) {
        if (getWidth() > 0 && getHeight() > 0) {
            previousNode = currentNode;
            if (previousNode == null)
                previousNode = EMPTY_PANE;
        } else
            previousNode = EMPTY_PANE;

        if (previousNode == newView)
            previousNode = EMPTY_PANE;

        setMouseTransparent(true);

        currentNode = newView;

        getChildren().setAll(previousNode, currentNode);
    }

    private final StackPane EMPTY_PANE = new StackPane();
}
