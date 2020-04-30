package ru.spark.slauncher.ui.decorator;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import ru.spark.slauncher.ui.construct.Navigator;
import ru.spark.slauncher.ui.wizard.Refreshable;

public interface DecoratorPage extends Refreshable {
    ReadOnlyObjectProperty<State> stateProperty();

    default boolean isPageCloseable() {
        return false;
    }

    default boolean back() {
        return true;
    }

    @Override
    default void refresh() {
    }

    default void closePage() {
    }

    default void onDecoratorPageNavigating(Navigator.NavigationEvent event) {
        ((Node) this).getStyleClass().add("content-background");
    }

    class State {
        private final String title;
        private final Node titleNode;
        private final boolean backable;
        private final boolean refreshable;
        private final boolean animate;
        private final boolean titleBarTransparent;
        private final double leftPaneWidth;

        public State(String title, Node titleNode, boolean backable, boolean refreshable, boolean animate) {
            this(title, titleNode, backable, refreshable, animate, false, 0);
        }

        public State(String title, Node titleNode, boolean backable, boolean refreshable, boolean animate, boolean titleBarTransparent, double leftPaneWidth) {
            this.title = title;
            this.titleNode = titleNode;
            this.backable = backable;
            this.refreshable = refreshable;
            this.animate = animate;
            this.titleBarTransparent = titleBarTransparent;
            this.leftPaneWidth = leftPaneWidth;
        }

        public static State fromTitle(String title) {
            return new State(title, null, true, false, true);
        }

        public static State fromTitleNode(Node titleNode) {
            return new State(null, titleNode, true, false, true);
        }

        public String getTitle() {
            return title;
        }

        public Node getTitleNode() {
            return titleNode;
        }

        public boolean isBackable() {
            return backable;
        }

        public boolean isRefreshable() {
            return refreshable;
        }

        public boolean isAnimate() {
            return animate;
        }

        public boolean isTitleBarTransparent() {
            return titleBarTransparent;
        }

        public double getLeftPaneWidth() {
            return leftPaneWidth;
        }
    }
}
