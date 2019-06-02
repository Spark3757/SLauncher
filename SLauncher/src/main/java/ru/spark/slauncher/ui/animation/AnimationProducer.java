package ru.spark.slauncher.ui.animation;

import javafx.animation.KeyFrame;

import java.util.List;

public interface AnimationProducer {
    void init(AnimationHandler handler);

    List<KeyFrame> animate(AnimationHandler handler);
}
