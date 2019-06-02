package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.Lang;

import java.util.Map;
import java.util.Optional;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

class AdditionalInstallersPage extends StackPane implements WizardPage {
    public static final String INSTALLER_TYPE = "INSTALLER_TYPE";
    private final InstallerWizardProvider provider;
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
    private Label lblVersionName;
    @FXML
    private Label lblForge;
    @FXML
    private Label lblLiteLoader;
    @FXML
    private Label lblOptiFine;
    @FXML
    private JFXButton btnInstall;

    public AdditionalInstallersPage(InstallerWizardProvider provider, WizardController controller, GameRepository repository, DownloadProvider downloadProvider) {
        this.provider = provider;
        this.controller = controller;

        FXUtils.loadFXML(this, "/assets/fxml/download/additional-installers.fxml");

        lblGameVersion.setText(provider.getGameVersion());
        lblVersionName.setText(provider.getVersion().getId());

        btnForge.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 0);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.forge")), provider.getGameVersion(), downloadProvider, "forge", () -> {
                controller.onPrev(false);
            }));
        });

        btnLiteLoader.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 1);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.liteloader")), provider.getGameVersion(), downloadProvider, "liteloader", () -> {
                controller.onPrev(false);
            }));
        });

        btnOptiFine.setOnMouseClicked(e -> {
            controller.getSettings().put(INSTALLER_TYPE, 2);
            controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.optifine")), provider.getGameVersion(), downloadProvider, "optifine", () -> {
                controller.onPrev(false);
            }));
        });

        btnInstall.setOnMouseClicked(e -> onInstall());
    }

    private void onInstall() {
        controller.onFinish();
    }

    @Override
    public String getTitle() {
        return i18n("settings.tabs.installers");
    }

    private String getVersion(String id) {
        return Optional.ofNullable(controller.getSettings().get(id)).map(it -> (RemoteVersion) it).map(RemoteVersion::getSelfVersion).orElse(null);
    }

    @Override
    public void onNavigate(Map<String, Object> settings) {
        lblGameVersion.setText(i18n("install.new_game.current_game_version") + ": " + provider.getGameVersion());
        btnForge.setDisable(provider.getForge() != null);
        if (provider.getForge() != null || controller.getSettings().containsKey("forge"))
            lblForge.setText(i18n("install.installer.version", i18n("install.installer.forge")) + ": " + Lang.nonNull(provider.getForge(), getVersion("forge")));
        else
            lblForge.setText(i18n("install.installer.not_installed", i18n("install.installer.forge")));

        btnLiteLoader.setDisable(provider.getLiteLoader() != null);
        if (provider.getLiteLoader() != null || controller.getSettings().containsKey("liteloader"))
            lblLiteLoader.setText(i18n("install.installer.version", i18n("install.installer.liteloader")) + ": " + Lang.nonNull(provider.getLiteLoader(), getVersion("liteloader")));
        else
            lblLiteLoader.setText(i18n("install.installer.not_installed", i18n("install.installer.liteloader")));

        btnOptiFine.setDisable(provider.getOptiFine() != null);
        if (provider.getOptiFine() != null || controller.getSettings().containsKey("optifine"))
            lblOptiFine.setText(i18n("install.installer.version", i18n("install.installer.optifine")) + ": " + Lang.nonNull(provider.getOptiFine(), getVersion("optifine")));
        else
            lblOptiFine.setText(i18n("install.installer.not_installed", i18n("install.installer.optifine")));

    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        settings.remove(INSTALLER_TYPE);
    }
}
