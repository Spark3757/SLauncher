package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.Validator;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.StringUtils;

import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class InstallersPage extends StackPane implements WizardPage {
    public static final String INSTALLER_TYPE = "INSTALLER_TYPE";
    private final WizardController controller;
    @FXML
    private VBox list;
    @FXML
    private JFXButton btnForge;
    @FXML
    private JFXButton btnLiteLoader;
    @FXML
    private JFXButton btnOptiFine;
    @FXML
    private Label lblGameVersion;
    @FXML
    private Label lblForge;
    @FXML
    private Label lblLiteLoader;
    @FXML
    private Label lblOptiFine;
    @FXML
    private JFXTextField txtName;
    @FXML
    private JFXButton btnInstall;

    public InstallersPage(WizardController controller, GameRepository repository, DownloadProvider downloadProvider) {
        this.controller = controller;

        FXUtils.loadFXML(this, "/assets/fxml/download/installers.fxml");

        String gameVersion = ((RemoteVersion) controller.getSettings().get("game")).getGameVersion();
        Validator hasVersion = new Validator(s -> !repository.hasVersion(s) && StringUtils.isNotBlank(s));
        hasVersion.setMessage(i18n("install.new_game.already_exists"));
        txtName.getValidators().add(hasVersion);
        txtName.textProperty().addListener(e -> btnInstall.setDisable(!txtName.validate()));
        txtName.setText(gameVersion);

        btnForge.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 0);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.forge")), gameVersion, downloadProvider, "forge", () -> controller.onPrev(false)));
        });

        btnLiteLoader.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 1);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.liteloader")), gameVersion, downloadProvider, "liteloader", () -> controller.onPrev(false)));
        });

        btnOptiFine.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 2);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.optifine")), gameVersion, downloadProvider, "optifine", () -> controller.onPrev(false)));
        });
    }

    @Override
    public String getTitle() {
        return i18n("install.new_game");
    }

    private String getVersion(String id) {
        return ((RemoteVersion) controller.getSettings().get(id)).getSelfVersion();
    }

    @Override
    public void onNavigate(Map<String, Object> settings) {
        lblGameVersion.setText(i18n("install.new_game.current_game_version") + ": " + getVersion("game"));
        if (controller.getSettings().containsKey("forge"))
            lblForge.setText(i18n("install.installer.version", i18n("install.installer.forge")) + ": " + getVersion("forge"));
        else
            lblForge.setText(i18n("install.installer.not_installed", i18n("install.installer.forge")));

        if (controller.getSettings().containsKey("liteloader"))
            lblLiteLoader.setText(i18n("install.installer.version", i18n("install.installer.liteloader")) + ": " + getVersion("liteloader"));
        else
            lblLiteLoader.setText(i18n("install.installer.not_installed", i18n("install.installer.liteloader")));

        if (controller.getSettings().containsKey("optifine"))
            lblOptiFine.setText(i18n("install.installer.version", i18n("install.installer.optifine")) + ": " + getVersion("optifine"));
        else
            lblOptiFine.setText(i18n("install.installer.not_installed", i18n("install.installer.optifine")));
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        settings.remove(INSTALLER_TYPE);
    }

    @FXML
    private void onInstall() {
        controller.getSettings().put("name", txtName.getText());
        controller.onFinish();
    }
}
