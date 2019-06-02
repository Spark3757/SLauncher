package ru.spark.slauncher.ui.wizard;

import ru.spark.slauncher.ui.animation.ContainerAnimations;

public interface Navigation {

    void onStart();

    void onNext();

    void onPrev(boolean cleanUp);

    boolean canPrev();

    void onFinish();

    void onEnd();

    void onCancel();

    enum NavigationDirection {
        START(ContainerAnimations.NONE),
        PREVIOUS(ContainerAnimations.SWIPE_RIGHT),
        NEXT(ContainerAnimations.SWIPE_LEFT),
        FINISH(ContainerAnimations.SWIPE_LEFT),
        IN(ContainerAnimations.ZOOM_IN),
        OUT(ContainerAnimations.ZOOM_OUT);

        private final ContainerAnimations animation;

        NavigationDirection(ContainerAnimations animation) {
            this.animation = animation;
        }

        public ContainerAnimations getAnimation() {
            return animation;
        }
    }
}
