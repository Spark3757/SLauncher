package ru.spark.slauncher.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.function.Consumer;

/**
 * @author spark1337
 */
public class InstallerItem extends BorderPane {

    public InstallerItem(String libraryId, String libraryVersion, @Nullable Runnable upgrade, @Nullable Consumer<InstallerItem> deleteCallback) {
        getStyleClass().addAll("two-line-list-item", "card");
        JFXDepthManager.setDepth(this, 1);

        String[] urls = new String[]{"/assets/img/grass.png", "/assets/img/fabric.png", "/assets/img/forge.png", "/assets/img/chicken.png", "/assets/img/command.png"};
        String[] libraryIds = new String[]{"game", "fabric", "forge", "liteloader", "optifine"};

        boolean regularLibrary = false;
        for (int i = 0; i < 5; ++i) {
            if (libraryIds[i].equals(libraryId)) {
                setLeft(FXUtils.limitingSize(new ImageView(new Image(urls[i], 32, 32, true, true)), 32, 32));
                Label label = new Label();
                BorderPane.setAlignment(label, Pos.CENTER_LEFT);
                BorderPane.setMargin(label, new Insets(0, 0, 0, 8));
                if (libraryVersion == null) {
                    label.setText(I18n.i18n("install.installer.not_installed", I18n.i18n("install.installer." + libraryId)));
                } else {
                    label.setText(I18n.i18n("install.installer.version", I18n.i18n("install.installer." + libraryId), libraryVersion));
                }
                setCenter(label);
                regularLibrary = true;
                break;
            }
        }

        if (!regularLibrary) {
            String title = I18n.hasKey("install.installer." + libraryId) ? I18n.i18n("install.installer." + libraryId) : libraryId;
            if (libraryVersion != null) {
                TwoLineListItem item = new TwoLineListItem();
                item.setTitle(title);
                item.setSubtitle(I18n.i18n("archive.version") + ": " + libraryVersion);
                setCenter(item);
            } else {
                Label label = new Label();
                label.setStyle("-fx-font-size: 15px;");
                BorderPane.setAlignment(label, Pos.CENTER_LEFT);
                setCenter(label);
            }
        }

        {
            HBox hBox = new HBox();

            if (upgrade != null) {
                JFXButton upgradeButton = new JFXButton();
                if (libraryVersion == null) {
                    upgradeButton.setGraphic(SVG.arrowRight(Theme.blackFillBinding(), -1, -1));
                } else {
                    upgradeButton.setGraphic(SVG.update(Theme.blackFillBinding(), -1, -1));
                }
                upgradeButton.getStyleClass().add("toggle-icon4");
                FXUtils.installFastTooltip(upgradeButton, I18n.i18n("install.change_version"));
                upgradeButton.setOnMouseClicked(e -> upgrade.run());
                hBox.getChildren().add(upgradeButton);
            }

            if (deleteCallback != null) {
                JFXButton deleteButton = new JFXButton();
                deleteButton.setGraphic(SVG.close(Theme.blackFillBinding(), -1, -1));
                deleteButton.getStyleClass().add("toggle-icon4");
                deleteButton.setOnMouseClicked(e -> deleteCallback.accept(this));
                hBox.getChildren().add(deleteButton);
            }

            hBox.setAlignment(Pos.CENTER_RIGHT);
            setRight(hBox);
        }
    }

}
