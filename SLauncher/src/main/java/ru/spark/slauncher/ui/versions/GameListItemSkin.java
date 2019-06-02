package ru.spark.slauncher.ui.versions;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.IconedMenuItem;
import ru.spark.slauncher.ui.construct.MenuSeparator;
import ru.spark.slauncher.ui.construct.PopupMenu;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class GameListItemSkin extends SkinBase<GameListItem> {

    public GameListItemSkin(GameListItem skinnable) {
        super(skinnable);

        BorderPane root = new BorderPane();

        JFXRadioButton chkSelected = new JFXRadioButton();
        BorderPane.setAlignment(chkSelected, Pos.CENTER);
        chkSelected.setUserData(skinnable);
        chkSelected.selectedProperty().bindBidirectional(skinnable.selectedProperty());
        chkSelected.setToggleGroup(skinnable.getToggleGroup());
        root.setLeft(chkSelected);

        root.setCenter(new GameItem(skinnable.getProfile(), skinnable.getVersion()));

        PopupMenu menu = new PopupMenu();
        JFXPopup popup = new JFXPopup(menu);

        menu.getContent().setAll(
                new IconedMenuItem(FXUtils.limitingSize(SVG.gear(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("version.manage.manage"), FXUtils.withJFXPopupClosing(skinnable::modifyGameSettings, popup)),
                new MenuSeparator(),
                new IconedMenuItem(FXUtils.limitingSize(SVG.pencil(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("version.manage.rename"), FXUtils.withJFXPopupClosing(skinnable::rename, popup)),
                new IconedMenuItem(FXUtils.limitingSize(SVG.delete(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("version.manage.remove"), FXUtils.withJFXPopupClosing(skinnable::remove, popup)),
                new IconedMenuItem(FXUtils.limitingSize(SVG.export(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("modpack.export"), FXUtils.withJFXPopupClosing(skinnable::export, popup)),
                new MenuSeparator(),
                new IconedMenuItem(FXUtils.limitingSize(SVG.folderOpen(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("folder.game"), FXUtils.withJFXPopupClosing(skinnable::browse, popup)),
                new MenuSeparator(),
                new IconedMenuItem(FXUtils.limitingSize(SVG.launch(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("version.launch.test"), FXUtils.withJFXPopupClosing(skinnable::launch, popup)),
                new IconedMenuItem(FXUtils.limitingSize(SVG.script(Theme.blackFillBinding(), 14, 14), 14, 14), i18n("version.launch_script"), FXUtils.withJFXPopupClosing(skinnable::generateLaunchScript, popup)));

        HBox right = new HBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        if (skinnable.canUpdate()) {
            JFXButton btnUpgrade = new JFXButton();
            btnUpgrade.setOnMouseClicked(e -> skinnable.update());
            btnUpgrade.getStyleClass().add("toggle-icon4");
            btnUpgrade.setGraphic(SVG.update(Theme.blackFillBinding(), -1, -1));
            FXUtils.runInFX(() -> FXUtils.installFastTooltip(btnUpgrade, i18n("version.update")));
            right.getChildren().add(btnUpgrade);
        }

        JFXButton btnManage = new JFXButton();
        btnManage.setOnMouseClicked(e -> {
            popup.show(root, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, 0, root.getHeight());
        });
        btnManage.getStyleClass().add("toggle-icon4");
        BorderPane.setAlignment(btnManage, Pos.CENTER);
        btnManage.setGraphic(SVG.dotsVertical(Theme.blackFillBinding(), -1, -1));
        right.getChildren().add(btnManage);
        root.setRight(right);

        root.setStyle("-fx-background-color: white; -fx-padding: 8 8 8 0;");
        JFXDepthManager.setDepth(root, 1);

        getChildren().setAll(root);
    }
}
