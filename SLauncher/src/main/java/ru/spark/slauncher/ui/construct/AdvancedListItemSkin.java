package ru.spark.slauncher.ui.construct;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import ru.spark.slauncher.ui.FXUtils;

public class AdvancedListItemSkin extends SkinBase<AdvancedListItem> {

    public AdvancedListItemSkin(AdvancedListItem skinnable) {
        super(skinnable);

        StackPane stackPane = new StackPane();
        RipplerContainer container = new RipplerContainer(stackPane);

        BorderPane root = new BorderPane();
        root.setPickOnBounds(false);

        HBox left = new HBox();
        left.setAlignment(Pos.CENTER);
        left.setMouseTransparent(true);

        StackPane imageViewContainer = new StackPane();
        FXUtils.setLimitWidth(imageViewContainer, 32);
        FXUtils.setLimitHeight(imageViewContainer, 32);

        ImageView imageView = new ImageView();
        FXUtils.limitSize(imageView, 32, 32);
        imageView.setPreserveRatio(true);
        imageView.imageProperty().bind(skinnable.imageProperty());
        imageViewContainer.getChildren().setAll(imageView);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setPadding(new Insets(0, 0, 0, 10));

        Label title = new Label();
        title.textProperty().bind(skinnable.titleProperty());
        title.setMaxWidth(90);
        title.setStyle("-fx-font-size: 15;");
        title.setTextAlignment(TextAlignment.JUSTIFY);
        vbox.getChildren().add(title);

        Label subtitle = new Label();
        subtitle.textProperty().bind(skinnable.subtitleProperty());
        subtitle.setMaxWidth(90);
        subtitle.setStyle("-fx-font-size: 10;");
        subtitle.setTextAlignment(TextAlignment.JUSTIFY);
        vbox.getChildren().add(subtitle);

        FXUtils.onChangeAndOperate(skinnable.subtitleProperty(), subtitleString -> {
            if (subtitleString == null) vbox.getChildren().setAll(title);
            else vbox.getChildren().setAll(title, subtitle);
        });

        left.getChildren().setAll(imageViewContainer, vbox);
        root.setLeft(left);

        HBox right = new HBox();
        right.setAlignment(Pos.CENTER);
        right.setMouseTransparent(true);
        right.getStyleClass().add("toggle-icon4");
        FXUtils.setLimitWidth(right, 40);
        FXUtils.onChangeAndOperate(skinnable.rightGraphicProperty(),
                newGraphic -> {
                    if (newGraphic == null) {
                        right.getChildren().clear();
                    } else {
                        right.getChildren().setAll(newGraphic);
                    }
                });
        root.setRight(right);

        FXUtils.onChangeAndOperate(skinnable.actionButtonVisibleProperty(),
                visible -> root.setRight(visible ? right : null));

        stackPane.setStyle("-fx-padding: 10 16 10 16;");
        stackPane.getStyleClass().add("transparent");
        stackPane.setPickOnBounds(false);
        stackPane.getChildren().setAll(root);

        getChildren().setAll(container);
    }
}
