package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import ru.spark.slauncher.game.ModpackHelper;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.WebStage;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.SpinnerPane;
import ru.spark.slauncher.ui.construct.Validator;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.CompressingUtils;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public final class LocalModpackPage extends StackPane implements WizardPage {
    private final WizardController controller;

    private Modpack manifest = null;

    @FXML
    private Region borderPane;

    @FXML
    private Label lblName;

    @FXML
    private Label lblVersion;

    @FXML
    private Label lblAuthor;

    @FXML
    private Label lblModpackLocation;

    @FXML
    private JFXTextField txtModpackName;

    @FXML
    private JFXButton btnInstall;

    @FXML
    private SpinnerPane spinnerPane;

    public LocalModpackPage(WizardController controller) {
        this.controller = controller;

        FXUtils.loadFXML(this, "/assets/fxml/download/modpack.fxml");

        Profile profile = (Profile) controller.getSettings().get("PROFILE");

        File selectedFile;

        Optional<String> name = Lang.tryCast(controller.getSettings().get(MODPACK_NAME), String.class);
        if (name.isPresent()) {
            txtModpackName.setText(name.get());
            txtModpackName.setDisable(true);
        }

        Optional<File> filePath = Lang.tryCast(controller.getSettings().get(MODPACK_FILE), File.class);
        if (filePath.isPresent()) {
            selectedFile = filePath.get();
        } else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(I18n.i18n("modpack.choose"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.i18n("modpack"), "*.zip"));
            selectedFile = chooser.showOpenDialog(Controllers.getStage());
            if (selectedFile == null) {
                Platform.runLater(controller::onEnd);
                return;
            }

            controller.getSettings().put(MODPACK_FILE, selectedFile);
        }

        spinnerPane.showSpinner();
        Task.supplyAsync(() -> CompressingUtils.findSuitableEncoding(selectedFile.toPath()))
                .thenApplyAsync(encoding -> manifest = ModpackHelper.readModpackManifest(selectedFile.toPath(), encoding))
                .whenComplete(Schedulers.javafx(), manifest -> {
                    spinnerPane.hideSpinner();
                    controller.getSettings().put(MODPACK_MANIFEST, manifest);
                    lblName.setText(manifest.getName());
                    lblVersion.setText(manifest.getVersion());
                    lblAuthor.setText(manifest.getAuthor());

                    lblModpackLocation.setText(selectedFile.getAbsolutePath());

                    if (!name.isPresent()) {
                        txtModpackName.setText(manifest.getName() + (StringUtils.isBlank(manifest.getVersion()) ? "" : "-" + manifest.getVersion()));
                        txtModpackName.getValidators().addAll(
                                new Validator(I18n.i18n("install.new_game.already_exists"), str -> !profile.getRepository().hasVersion(str) && StringUtils.isNotBlank(str)),
                                new Validator(I18n.i18n("version.forbidden_name"), str -> !profile.getRepository().forbidsVersion(str))
                        );
                        txtModpackName.textProperty().addListener(e -> btnInstall.setDisable(!txtModpackName.validate()));
                    }
                }, e -> {
                    Controllers.dialog(I18n.i18n("modpack.task.install.error"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                    Platform.runLater(controller::onEnd);
                }).start();
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        settings.remove(MODPACK_FILE);
    }

    @FXML
    private void onInstall() {
        if (!txtModpackName.validate()) return;
        controller.getSettings().put(MODPACK_NAME, txtModpackName.getText());
        controller.onFinish();
    }

    @FXML
    private void onDescribe() {
        if (manifest != null) {
            WebStage stage = new WebStage();
            stage.getWebView().getEngine().loadContent(manifest.getDescription());
            stage.setTitle(I18n.i18n("modpack.description"));
            stage.showAndWait();
        }
    }

    @Override
    public String getTitle() {
        return I18n.i18n("modpack.task.install");
    }

    public static final String MODPACK_FILE = "MODPACK_FILE";
    public static final String MODPACK_NAME = "MODPACK_NAME";
    public static final String MODPACK_MANIFEST = "MODPACK_MANIFEST";
}
