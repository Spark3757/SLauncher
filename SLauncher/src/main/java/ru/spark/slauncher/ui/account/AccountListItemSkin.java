package ru.spark.slauncher.ui.account;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorAccount;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.javafx.BindingMapping;

import static ru.spark.slauncher.ui.FXUtils.runInFX;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class AccountListItemSkin extends SkinBase<AccountListItem> {

    public AccountListItemSkin(AccountListItem skinnable) {
        super(skinnable);

        BorderPane root = new BorderPane();

        JFXRadioButton chkSelected = new JFXRadioButton() {
            @Override
            public void fire() {
                skinnable.fire();
            }
        };
        BorderPane.setAlignment(chkSelected, Pos.CENTER);
        chkSelected.selectedProperty().bind(skinnable.selectedProperty());
        root.setLeft(chkSelected);

        HBox center = new HBox();
        center.setSpacing(8);
        center.setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = new ImageView();
        FXUtils.limitSize(imageView, 32, 32);
        imageView.imageProperty().bind(skinnable.imageProperty());

        Label title = new Label();
        title.getStyleClass().add("title");
        title.textProperty().bind(skinnable.titleProperty());
        Label subtitle = new Label();
        subtitle.getStyleClass().add("subtitle");
        subtitle.textProperty().bind(skinnable.subtitleProperty());
        if (skinnable.getAccount() instanceof AuthlibInjectorAccount) {
            Tooltip tooltip = new Tooltip();
            AuthlibInjectorServer server = ((AuthlibInjectorAccount) skinnable.getAccount()).getServer();
            tooltip.textProperty().bind(BindingMapping.of(server, AuthlibInjectorServer::toString));
            FXUtils.installSlowTooltip(subtitle, tooltip);
        }
        VBox item = new VBox(title, subtitle);
        item.getStyleClass().add("two-line-list-item");
        BorderPane.setAlignment(item, Pos.CENTER);

        center.getChildren().setAll(imageView, item);
        root.setCenter(center);

        HBox right = new HBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        JFXButton btnRefresh = new JFXButton();
        btnRefresh.setOnMouseClicked(e -> skinnable.refresh());
        btnRefresh.getStyleClass().add("toggle-icon4");
        btnRefresh.setGraphic(SVG.refresh(Theme.blackFillBinding(), -1, -1));
        runInFX(() -> FXUtils.installFastTooltip(btnRefresh, i18n("button.refresh")));
        right.getChildren().add(btnRefresh);

        JFXButton btnUpload = new JFXButton();
        btnUpload.setOnMouseClicked(e -> skinnable.uploadSkin());
        btnUpload.getStyleClass().add("toggle-icon4");
        btnUpload.setGraphic(SVG.hanger(Theme.blackFillBinding(), -1, -1));
        runInFX(() -> FXUtils.installFastTooltip(btnUpload, i18n("account.skin.upload")));
        btnUpload.visibleProperty().bind(skinnable.canUploadSkin());
        right.getChildren().add(btnUpload);

        JFXButton btnRemove = new JFXButton();
        btnRemove.setOnMouseClicked(e -> skinnable.remove());
        btnRemove.getStyleClass().add("toggle-icon4");
        BorderPane.setAlignment(btnRemove, Pos.CENTER);
        btnRemove.setGraphic(SVG.delete(Theme.blackFillBinding(), -1, -1));
        runInFX(() -> FXUtils.installFastTooltip(btnRemove, i18n("button.delete")));
        right.getChildren().add(btnRemove);
        root.setRight(right);

        root.getStyleClass().add("card");
        root.setStyle("-fx-padding: 8 8 8 0;");
        JFXDepthManager.setDepth(root, 1);

        getChildren().setAll(root);
    }
}
