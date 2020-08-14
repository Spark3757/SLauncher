package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ru.spark.slauncher.mod.server.ServerModpackManifest;
import ru.spark.slauncher.setting.Profile;
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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class RemoteModpackPage extends StackPane implements WizardPage {
    private final WizardController controller;

    private final ServerModpackManifest manifest;

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

    public RemoteModpackPage(WizardController controller) {
        this.controller = controller;

        FXUtils.loadFXML(this, "/assets/fxml/download/modpack.fxml");

        Profile profile = (Profile) controller.getSettings().get("PROFILE");

        Optional<String> name = Lang.tryCast(controller.getSettings().get(MODPACK_NAME), String.class);
        if (name.isPresent()) {
            txtModpackName.setText(name.get());
            txtModpackName.setDisable(true);
        }

        manifest = Lang.tryCast(controller.getSettings().get(MODPACK_SERVER_MANIFEST), ServerModpackManifest.class)
                .orElseThrow(() -> new IllegalStateException("MODPACK_SERVER_MANIFEST should exist"));
        lblModpackLocation.setText(manifest.getFileApi());

        try {
            controller.getSettings().put(MODPACK_MANIFEST, manifest.toModpack(null));
        } catch (IOException e) {
            Controllers.dialog(I18n.i18n("modpack.type.server.malformed"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
            Platform.runLater(controller::onEnd);
            return;
        }

        lblName.setText(manifest.getName());
        lblVersion.setText(manifest.getVersion());
        lblAuthor.setText(manifest.getAuthor());

        if (!name.isPresent()) {
            txtModpackName.setText(manifest.getName());
            txtModpackName.getValidators().addAll(
                    new Validator(I18n.i18n("install.new_game.already_exists"), str -> !profile.getRepository().hasVersion(str) && StringUtils.isNotBlank(str)),
                    new Validator(I18n.i18n("version.forbidden_name"), str -> !profile.getRepository().forbidsVersion(str))
            );
            txtModpackName.textProperty().addListener(e -> btnInstall.setDisable(!txtModpackName.validate()));
        }
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        settings.remove(MODPACK_SERVER_MANIFEST);
    }

    @FXML
    private void onInstall() {
        if (!txtModpackName.validate()) return;
        controller.getSettings().put(MODPACK_NAME, txtModpackName.getText());
        controller.onFinish();
    }

    @FXML
    private void onDescribe() {
        WebStage stage = new WebStage();
        stage.getWebView().getEngine().loadContent(manifest.getDescription());
        stage.setTitle(I18n.i18n("modpack.description"));
        stage.showAndWait();
    }

    @Override
    public String getTitle() {
        return I18n.i18n("modpack.task.install");
    }

    public static final String MODPACK_SERVER_MANIFEST = "MODPACK_SERVER_MANIFEST";
    public static final String MODPACK_NAME = "MODPACK_NAME";
    public static final String MODPACK_MANIFEST = "MODPACK_MANIFEST";
}
