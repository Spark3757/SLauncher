package ru.spark.slauncher.ui.construct;

import com.jfoenix.controls.JFXButton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;

/**
 * @author Spark1337
 */
class ComponentListCell extends StackPane {
    private final Node content;
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);
    private Animation expandAnimation;
    private Rectangle clipRect;

    ComponentListCell(Node content) {
        this.content = content;

        updateLayout();
    }

    private void updateClip(double newHeight) {
        if (clipRect != null)
            clipRect.setHeight(newHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        if (clipRect == null)
            clipRect = new Rectangle(0, 0, getWidth(), getHeight());
        else {
            clipRect.setX(0);
            clipRect.setY(0);
            clipRect.setHeight(getHeight());
            clipRect.setWidth(getWidth());
        }
    }

    private void updateLayout() {
        if (content instanceof ComponentList) {
            ComponentList list = (ComponentList) content;
            content.getStyleClass().remove("options-list");
            content.getStyleClass().add("options-sublist");

            BorderPane groupNode = new BorderPane();

            Node expandIcon = SVG.expand(Theme.blackFillBinding(), 10, 10);
            JFXButton expandButton = new JFXButton();
            expandButton.setGraphic(expandIcon);
            expandButton.getStyleClass().add("options-list-item-expand-button");

            VBox labelVBox = new VBox();
            labelVBox.setAlignment(Pos.CENTER_LEFT);

            boolean overrideHeaderLeft = false;
            if (list instanceof ComponentSublist) {
                Node leftNode = ((ComponentSublist) list).getHeaderLeft();
                if (leftNode != null) {
                    labelVBox.getChildren().setAll(leftNode);
                    overrideHeaderLeft = true;
                }
            }

            if (!overrideHeaderLeft) {
                Label label = new Label();
                label.textProperty().bind(list.titleProperty());
                labelVBox.getChildren().add(label);

                if (list.isHasSubtitle()) {
                    Label subtitleLabel = new Label();
                    subtitleLabel.textProperty().bind(list.subtitleProperty());
                    subtitleLabel.getStyleClass().add("subtitle-label");
                    labelVBox.getChildren().add(subtitleLabel);
                }
            }

            groupNode.setLeft(labelVBox);

            HBox right = new HBox();
            right.setSpacing(16);
            right.setAlignment(Pos.CENTER_RIGHT);
            if (list instanceof ComponentSublist) {
                Node rightNode = ((ComponentSublist) list).getHeaderRight();
                if (rightNode != null)
                    right.getChildren().add(rightNode);
            }
            right.getChildren().add(expandButton);
            groupNode.setRight(right);

            VBox container = new VBox();
            container.setPadding(new Insets(8, 0, 0, 0));
            FXUtils.setLimitHeight(container, 0);
            FXUtils.setOverflowHidden(container, true);
            container.getChildren().setAll(content);
            groupNode.setBottom(container);

            expandButton.setOnMouseClicked(e -> {
                if (expandAnimation != null && expandAnimation.getStatus() == Animation.Status.RUNNING) {
                    expandAnimation.stop();
                }

                setExpanded(!isExpanded());

                double newAnimatedHeight = content.prefHeight(-1) * (isExpanded() ? 1 : -1);
                double newHeight = isExpanded() ? getHeight() + newAnimatedHeight : prefHeight(-1);
                double contentHeight = isExpanded() ? newAnimatedHeight : 0;

                if (isExpanded()) {
                    updateClip(newHeight);
                }

                expandAnimation = new Timeline(new KeyFrame(new Duration(320.0),
                        new KeyValue(container.minHeightProperty(), contentHeight, FXUtils.SINE),
                        new KeyValue(container.maxHeightProperty(), contentHeight, FXUtils.SINE)
                ));

                if (!isExpanded()) {
                    expandAnimation.setOnFinished(e2 -> updateClip(newHeight));
                }

                expandAnimation.play();
            });

            expandedProperty().addListener((a, b, newValue) ->
                    expandIcon.setRotate(newValue ? 180 : 0));

            getChildren().setAll(groupNode);
        } else
            getChildren().setAll(content);
    }

    public boolean isExpanded() {
        return expanded.get();
    }

    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}
