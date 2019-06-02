package ru.spark.slauncher.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.construct.TwoLineListItem;

import java.util.function.Consumer;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

/**
 * @author Spark1337
 */
public class InstallerItem extends BorderPane {

    public InstallerItem(String artifact, String version, @Nullable Runnable upgrade, @Nullable Consumer<InstallerItem> deleteCallback) {
        getStyleClass().add("two-line-list-item");
        setStyle("-fx-background-radius: 2; -fx-background-color: white; -fx-padding: 8;");
        JFXDepthManager.setDepth(this, 1);

        {
            TwoLineListItem item = new TwoLineListItem();
            item.setTitle(artifact);
            item.setSubtitle(i18n("archive.version") + ": " + version);
            setCenter(item);
        }

        {
            HBox hBox = new HBox();

            if (upgrade != null) {
                JFXButton upgradeButton = new JFXButton();
                upgradeButton.setGraphic(SVG.update(Theme.blackFillBinding(), -1, -1));
                upgradeButton.getStyleClass().add("toggle-icon4");
                FXUtils.installFastTooltip(upgradeButton, i18n("install.change_version"));
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
