package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.download.fabric.FabricRemoteVersion;
import ru.spark.slauncher.download.forge.ForgeRemoteVersion;
import ru.spark.slauncher.download.game.GameRemoteVersion;
import ru.spark.slauncher.download.liteloader.LiteLoaderRemoteVersion;
import ru.spark.slauncher.download.optifine.OptiFineRemoteVersion;
import ru.spark.slauncher.setting.DownloadProviders;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.animation.ContainerAnimations;
import ru.spark.slauncher.ui.animation.TransitionPane;
import ru.spark.slauncher.ui.construct.FloatListCell;
import ru.spark.slauncher.ui.construct.TwoLineListItem;
import ru.spark.slauncher.ui.wizard.Refreshable;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ru.spark.slauncher.ui.FXUtils.stringConverter;

public final class VersionsPage extends BorderPane implements WizardPage, Refreshable {
    private final String gameVersion;
    private final String libraryId;
    private final String title;
    private final WizardController controller;

    @FXML
    private JFXListView<RemoteVersion> list;
    @FXML
    private JFXSpinner spinner;
    @FXML
    private StackPane failedPane;
    @FXML
    private StackPane emptyPane;
    @FXML
    private TransitionPane root;
    @FXML
    private JFXCheckBox chkRelease;
    @FXML
    private JFXCheckBox chkSnapshot;
    @FXML
    private JFXCheckBox chkOld;
    @FXML
    private HBox checkPane;
    @FXML
    private VBox centrePane;
    @FXML
    private JFXComboBox<String> downloadSourceComboBox;

    private VersionList<?> versionList;
    private TaskExecutor executor;

    public VersionsPage(WizardController controller, String title, String gameVersion, InstallerWizardDownloadProvider downloadProvider, String libraryId, Runnable callback) {
        this.title = title;
        this.gameVersion = gameVersion;
        this.libraryId = libraryId;
        this.controller = controller;

        FXUtils.loadFXML(this, "/assets/fxml/download/versions.fxml");

        downloadSourceComboBox.getItems().setAll(DownloadProviders.providersById.keySet());
        downloadSourceComboBox.setConverter(stringConverter(key -> I18n.i18n("download.provider." + key)));
        downloadSourceComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, newValue) -> {
            controller.getSettings().put("downloadProvider", newValue);
            downloadProvider.setDownloadProvider(DownloadProviders.getDownloadProviderByPrimaryId(newValue));
            versionList = downloadProvider.getVersionListById(libraryId);
            if (versionList.hasType()) {
                centrePane.getChildren().setAll(checkPane, list);
            } else {
                centrePane.getChildren().setAll(list);
            }
            refresh();
        });
        downloadSourceComboBox.getSelectionModel().select((String) controller.getSettings().getOrDefault("downloadProvider", DownloadProviders.getPrimaryDownloadProviderId()));

        InvalidationListener listener = o -> list.getItems().setAll(loadVersions());
        chkRelease.selectedProperty().addListener(listener);
        chkSnapshot.selectedProperty().addListener(listener);
        chkOld.selectedProperty().addListener(listener);

        list.setCellFactory(listView -> new FloatListCell<RemoteVersion>() {
            ImageView imageView = new ImageView();
            TwoLineListItem content = new TwoLineListItem();

            {
                HBox container = new HBox(12);
                container.setPadding(new Insets(0, 0, 0, 6));
                container.setAlignment(Pos.CENTER_LEFT);
                pane.getChildren().add(container);

                container.getChildren().setAll(imageView, content);
            }

            @Override
            protected void updateControl(RemoteVersion remoteVersion, boolean empty) {
                if (empty) return;
                content.setTitle(remoteVersion.getSelfVersion());
                content.setSubtitle(remoteVersion.getGameVersion());

                if (remoteVersion instanceof GameRemoteVersion) {
                    switch (remoteVersion.getVersionType()) {
                        case RELEASE:
                            content.setSubtitle(I18n.i18n("version.game.release"));
                            imageView.setImage(new Image("/assets/img/grass.png", 32, 32, false, true));
                            break;
                        case SNAPSHOT:
                            content.setSubtitle(I18n.i18n("version.game.snapshot"));
                            imageView.setImage(new Image("/assets/img/command.png", 32, 32, false, true));
                            break;
                        default:
                            content.setSubtitle(I18n.i18n("version.game.old"));
                            imageView.setImage(new Image("/assets/img/craft_table.png", 32, 32, false, true));
                            break;
                    }
                } else if (remoteVersion instanceof LiteLoaderRemoteVersion) {
                    imageView.setImage(new Image("/assets/img/chicken.png", 32, 32, false, true));
                    content.setSubtitle(remoteVersion.getGameVersion());
                } else if (remoteVersion instanceof OptiFineRemoteVersion) {
                    imageView.setImage(new Image("/assets/img/command.png", 32, 32, false, true));
                    content.setSubtitle(remoteVersion.getGameVersion());
                } else if (remoteVersion instanceof ForgeRemoteVersion) {
                    imageView.setImage(new Image("/assets/img/forge.png", 32, 32, false, true));
                    content.setSubtitle(remoteVersion.getGameVersion());
                } else if (remoteVersion instanceof FabricRemoteVersion) {
                    imageView.setImage(new Image("/assets/img/fabric.png", 32, 32, false, true));
                    content.setSubtitle(remoteVersion.getGameVersion());
                }
            }
        });

        list.setOnMouseClicked(e -> {
            if (list.getSelectionModel().getSelectedIndex() < 0)
                return;
            controller.getSettings().put(libraryId, list.getSelectionModel().getSelectedItem());
            callback.run();
        });
        refresh();
    }

    private List<RemoteVersion> loadVersions() {
        return versionList.getVersions(gameVersion).stream()
                .filter(it -> {
                    switch (it.getVersionType()) {
                        case RELEASE:
                            return chkRelease.isSelected();
                        case SNAPSHOT:
                            return chkSnapshot.isSelected();
                        case OLD:
                            return chkOld.isSelected();
                        default:
                            return true;
                    }
                })
                .sorted().collect(Collectors.toList());
    }

    @Override
    public void refresh() {
        VersionList<?> currentVersionList = versionList;
        root.setContent(spinner, ContainerAnimations.FADE.getAnimationProducer());
        executor = currentVersionList.refreshAsync(gameVersion).whenComplete(exception -> {
            if (exception == null) {
                List<RemoteVersion> items = loadVersions();

                Platform.runLater(() -> {
                    if (versionList != currentVersionList) return;
                    if (currentVersionList.getVersions(gameVersion).isEmpty()) {
                        root.setContent(emptyPane, ContainerAnimations.FADE.getAnimationProducer());
                    } else {
                        if (items.isEmpty()) {
                            chkRelease.setSelected(true);
                            chkSnapshot.setSelected(true);
                            chkOld.setSelected(true);
                        } else {
                            list.getItems().setAll(items);
                        }
                        root.setContent(centrePane, ContainerAnimations.FADE.getAnimationProducer());
                    }
                });
            } else {
                Logging.LOG.log(Level.WARNING, "Failed to fetch versions list", exception);
                Platform.runLater(() -> {
                    if (versionList != currentVersionList) return;
                    root.setContent(failedPane, ContainerAnimations.FADE.getAnimationProducer());
                });
            }
        }).executor().start();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        settings.remove(libraryId);
        if (executor != null)
            executor.cancel();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onBack() {
        controller.onPrev(true);
    }
}
