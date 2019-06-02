package ru.spark.slauncher.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.construct.SpinnerPane;

public class ListPageSkin extends SkinBase<ListPage<?>> {

    public ListPageSkin(ListPage<?> skinnable) {
        super(skinnable);

        SpinnerPane spinnerPane = new SpinnerPane();
        spinnerPane.getStyleClass().add("large-spinner-pane");
        Pane placeholder = new Pane();
        VBox list = new VBox();

        StackPane contentPane = new StackPane();
        {
            ScrollPane scrollPane = new ScrollPane();
            {
                scrollPane.setFitToWidth(true);

                list.maxWidthProperty().bind(scrollPane.widthProperty());
                list.setSpacing(10);

                VBox content = new VBox();
                content.getChildren().setAll(list, placeholder);

                Bindings.bindContent(list.getChildren(), skinnable.itemsProperty());

                scrollPane.setContent(content);
                JFXScrollPane.smoothScrolling(scrollPane);
            }

            VBox vBox = new VBox();
            {
                vBox.setAlignment(Pos.BOTTOM_RIGHT);
                vBox.setPickOnBounds(false);
                vBox.setPadding(new Insets(15));
                vBox.setSpacing(15);

                JFXButton btnAdd = new JFXButton();
                FXUtils.setLimitWidth(btnAdd, 40);
                FXUtils.setLimitHeight(btnAdd, 40);
                btnAdd.getStyleClass().add("jfx-button-raised-round");
                btnAdd.setButtonType(JFXButton.ButtonType.RAISED);
                btnAdd.setGraphic(SVG.plus(Theme.whiteFillBinding(), -1, -1));
                btnAdd.setOnMouseClicked(e -> skinnable.add());

                JFXButton btnRefresh = new JFXButton();
                FXUtils.setLimitWidth(btnRefresh, 40);
                FXUtils.setLimitHeight(btnRefresh, 40);
                btnRefresh.getStyleClass().add("jfx-button-raised-round");
                btnRefresh.setButtonType(JFXButton.ButtonType.RAISED);
                btnRefresh.setGraphic(SVG.refresh(Theme.whiteFillBinding(), -1, -1));
                btnRefresh.setOnMouseClicked(e -> skinnable.refresh());

                vBox.getChildren().setAll(btnAdd);

                FXUtils.onChangeAndOperate(skinnable.refreshableProperty(),
                        refreshable -> {
                            if (refreshable) {
                                list.setPadding(new Insets(10, 10, 15 + 40 + 15 + 40 + 15, 10));
                                vBox.getChildren().setAll(btnRefresh, btnAdd);
                            } else {
                                list.setPadding(new Insets(10, 10, 15 + 40 + 15, 10));
                                vBox.getChildren().setAll(btnAdd);
                            }
                        });
            }

            // Keep a blank space to prevent buttons from blocking up mod items.
            BorderPane group = new BorderPane();
            group.setPickOnBounds(false);
            group.setBottom(vBox);
            placeholder.minHeightProperty().bind(vBox.heightProperty());

            contentPane.getChildren().setAll(scrollPane, group);
        }

        spinnerPane.loadingProperty().bind(skinnable.loadingProperty());
        spinnerPane.setContent(contentPane);

        getChildren().setAll(spinnerPane);
    }
}
