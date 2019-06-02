package ru.spark.slauncher.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.construct.SpinnerPane;

import java.util.List;

public abstract class ToolbarListPageSkin<T extends ListPageBase<? extends Node>> extends SkinBase<T> {

    public ToolbarListPageSkin(T skinnable) {
        super(skinnable);

        SpinnerPane spinnerPane = new SpinnerPane();
        spinnerPane.getStyleClass().add("large-spinner-pane");

        BorderPane root = new BorderPane();

        {
            HBox toolbar = new HBox();
            toolbar.getStyleClass().add("jfx-tool-bar-second");
            JFXDepthManager.setDepth(toolbar, 1);
            toolbar.setPickOnBounds(false);
            toolbar.getChildren().setAll(initializeToolbar(skinnable));
            root.setTop(toolbar);
        }

        {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);

            VBox content = new VBox();
            content.setSpacing(10);
            content.setPadding(new Insets(10));

            Bindings.bindContent(content.getChildren(), skinnable.itemsProperty());

            scrollPane.setContent(content);
            JFXScrollPane.smoothScrolling(scrollPane);

            root.setCenter(scrollPane);
        }

        spinnerPane.loadingProperty().bind(skinnable.loadingProperty());
        spinnerPane.setContent(root);

        getChildren().setAll(spinnerPane);
    }

    public static Node wrap(Node node) {
        StackPane stackPane = new StackPane();
        stackPane.setPadding(new Insets(0, 5, 0, 2));
        stackPane.getChildren().setAll(node);
        return stackPane;
    }

    public static JFXButton createToolbarButton(String text, SVG.SVGIcon creator, Runnable onClick) {
        JFXButton ret = new JFXButton();
        ret.getStyleClass().add("jfx-tool-bar-button");
        ret.textFillProperty().bind(Theme.foregroundFillBinding());
        ret.setGraphic(wrap(creator.createIcon(Theme.foregroundFillBinding(), -1, -1)));
        ret.setText(text);
        ret.setOnMouseClicked(e -> onClick.run());
        return ret;
    }

    protected abstract List<Node> initializeToolbar(T skinnable);
}
