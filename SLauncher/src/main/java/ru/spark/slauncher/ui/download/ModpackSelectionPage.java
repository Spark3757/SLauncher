package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import ru.spark.slauncher.mod.server.ServerModpackManifest;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static ru.spark.slauncher.ui.download.RemoteModpackPage.MODPACK_SERVER_MANIFEST;

public final class ModpackSelectionPage extends StackPane implements WizardPage {
    private final WizardController controller;

    @FXML
    private JFXButton btnLocal;
    @FXML
    private JFXButton btnRemote;

    public ModpackSelectionPage(WizardController controller) {
        this.controller = controller;
        FXUtils.loadFXML(this, "/assets/fxml/download/modpack-source.fxml");

        JFXDepthManager.setDepth(btnLocal, 1);
        JFXDepthManager.setDepth(btnRemote, 1);

        Optional<File> filePath = Lang.tryCast(controller.getSettings().get(LocalModpackPage.MODPACK_FILE), File.class);
        if (filePath.isPresent()) {
            controller.getSettings().put(LocalModpackPage.MODPACK_FILE, filePath.get());
            Platform.runLater(controller::onNext);
        }

        FXUtils.applyDragListener(this, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
            File modpack = modpacks.get(0);
            controller.getSettings().put(LocalModpackPage.MODPACK_FILE, modpack);
            controller.onNext();
        });
    }

    @FXML
    private void onChooseLocalFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.i18n("modpack.choose"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.i18n("modpack"), "*.zip"));
        File selectedFile = chooser.showOpenDialog(Controllers.getStage());
        if (selectedFile == null) {
            Platform.runLater(controller::onEnd);
            return;
        }

        controller.getSettings().put(LocalModpackPage.MODPACK_FILE, selectedFile);
        controller.onNext();
    }

    @FXML
    private void onChooseRemoteFile() {
        Controllers.prompt(I18n.i18n("modpack.choose.remote.tooltip"), (urlString, resolve, reject) -> {
            try {
                URL url = new URL(urlString);
                if (urlString.endsWith("server-manifest.json")) {
                    // if urlString ends with .json, we assume that the url is server-manifest.json
                    Controllers.taskDialog(new GetTask(url).whenComplete(Schedulers.javafx(), (result, e) -> {
                        ServerModpackManifest manifest = JsonUtils.fromMaybeMalformedJson(result, ServerModpackManifest.class);
                        if (manifest == null) {
                            reject.accept(I18n.i18n("modpack.type.server.malformed"));
                        } else if (e == null) {
                            resolve.run();
                            controller.getSettings().put(MODPACK_SERVER_MANIFEST, manifest);
                            controller.onNext();
                        } else {
                            reject.accept(e.getMessage());
                        }
                    }).executor(true), I18n.i18n("message.downloading"));
                } else {
                    // otherwise we still consider the file as modpack zip file
                    // since casually the url may not ends with ".zip"
                    Path modpack = Files.createTempFile("modpack", ".zip");
                    resolve.run();

                    Controllers.taskDialog(
                            new FileDownloadTask(url, modpack.toFile(), null)
                                    .whenComplete(Schedulers.javafx(), e -> {
                                        if (e == null) {
                                            resolve.run();
                                            controller.getSettings().put(LocalModpackPage.MODPACK_FILE, modpack.toFile());
                                            controller.onNext();
                                        } else {
                                            reject.accept(e.getMessage());
                                        }
                                    }).executor(true),
                            I18n.i18n("message.downloading")
                    );
                }
            } catch (IOException e) {
                reject.accept(e.getMessage());
            }
        });
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
    }

    @Override
    public String getTitle() {
        return I18n.i18n("modpack.task.install");
    }
}
