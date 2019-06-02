package ru.spark.slauncher.ui.download;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.forge.ForgeRemoteVersion;
import ru.spark.slauncher.download.game.GameRemoteVersion;
import ru.spark.slauncher.download.liteloader.LiteLoaderRemoteVersion;
import ru.spark.slauncher.download.optifine.OptiFineRemoteVersion;
import ru.spark.slauncher.ui.FXUtils;

import java.util.Objects;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

/**
 * @author Spark1337
 */
public final class VersionsPageItem extends StackPane {
    private final RemoteVersion remoteVersion;
    @FXML
    private Label lblSelfVersion;
    @FXML
    private Label lblGameVersion;
    @FXML
    private ImageView imageView;
    @FXML
    private HBox leftPane;
    @FXML
    private StackPane imageViewContainer;

    public VersionsPageItem(RemoteVersion remoteVersion) {
        this.remoteVersion = Objects.requireNonNull(remoteVersion);

        FXUtils.loadFXML(this, "/assets/fxml/download/versions-list-item.fxml");
        lblSelfVersion.setText(remoteVersion.getSelfVersion());

        if (remoteVersion instanceof GameRemoteVersion) {
            switch (remoteVersion.getVersionType()) {
                case RELEASE:
                    lblGameVersion.setText(i18n("version.game.release"));
                    imageView.setImage(new Image("/assets/img/icon.png", 32, 32, false, true));
                    break;
                case SNAPSHOT:
                    lblGameVersion.setText(i18n("version.game.snapshot"));
                    imageView.setImage(new Image("/assets/img/command.png", 32, 32, false, true));
                    break;
                default:
                    lblGameVersion.setText(i18n("version.game.old"));
                    imageView.setImage(new Image("/assets/img/grass.png", 32, 32, false, true));
                    break;
            }
        } else if (remoteVersion instanceof LiteLoaderRemoteVersion) {
            imageView.setImage(new Image("/assets/img/chicken.png", 32, 32, false, true));
            lblGameVersion.setText(remoteVersion.getGameVersion());
        } else if (remoteVersion instanceof OptiFineRemoteVersion) {
            // optifine has no icon.
            lblGameVersion.setText(remoteVersion.getGameVersion());
        } else if (remoteVersion instanceof ForgeRemoteVersion) {
            imageView.setImage(new Image("/assets/img/forge.png", 32, 32, false, true));
            lblGameVersion.setText(remoteVersion.getGameVersion());
        }

        leftPane.getChildren().remove(imageViewContainer);
    }

    public RemoteVersion getRemoteVersion() {
        return remoteVersion;
    }
}
