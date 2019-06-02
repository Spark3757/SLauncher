package ru.spark.slauncher.ui.export;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import ru.spark.slauncher.Launcher;
import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class ModpackInfoPage extends StackPane implements WizardPage {
    public static final String MODPACK_NAME = "modpack.name";
    public static final String MODPACK_VERSION = "modpack.version";
    public static final String MODPACK_AUTHOR = "archive.author";
    public static final String MODPACK_DESCRIPTION = "modpack.description";
    public static final String MODPACK_INCLUDE_LAUNCHER = "modpack.include_launcher";
    public static final String MODPACK_FILE = "modpack.file";
    private final WizardController controller;
    @FXML
    private Label lblVersionName;
    @FXML
    private JFXTextField txtModpackName;
    @FXML
    private JFXTextField txtModpackAuthor;
    @FXML
    private JFXTextField txtModpackVersion;
    @FXML
    private JFXTextArea txtModpackDescription;
    @FXML
    private JFXToggleButton chkIncludeLauncher;
    @FXML
    private JFXButton btnNext;
    @FXML
    private ScrollPane scroll;

    public ModpackInfoPage(WizardController controller, String version) {
        this.controller = controller;
        FXUtils.loadFXML(this, "/assets/fxml/modpack/info.fxml");
        FXUtils.smoothScrolling(scroll);
        txtModpackName.setText(version);
        txtModpackName.textProperty().addListener(e -> checkValidation());
        txtModpackAuthor.textProperty().addListener(e -> checkValidation());
        txtModpackVersion.textProperty().addListener(e -> checkValidation());
        txtModpackAuthor.setText(Optional.ofNullable(Accounts.getSelectedAccount()).map(Account::getUsername).orElse(""));
        lblVersionName.setText(version);

        List<File> launcherJar = Launcher.getCurrentJarFiles();
        if (launcherJar == null)
            chkIncludeLauncher.setDisable(true);
    }

    private void checkValidation() {
        btnNext.setDisable(!txtModpackName.validate() || !txtModpackVersion.validate() || !txtModpackAuthor.validate());
    }

    @FXML
    private void onNext() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n("modpack.wizard.step.initialization.save"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("modpack"), "*.zip"));
        File file = fileChooser.showSaveDialog(Controllers.getStage());
        if (file == null) {
            controller.onEnd();
            return;
        }
        controller.getSettings().put(MODPACK_NAME, txtModpackName.getText());
        controller.getSettings().put(MODPACK_VERSION, txtModpackVersion.getText());
        controller.getSettings().put(MODPACK_AUTHOR, txtModpackAuthor.getText());
        controller.getSettings().put(MODPACK_FILE, file);
        controller.getSettings().put(MODPACK_DESCRIPTION, txtModpackDescription.getText());
        controller.getSettings().put(MODPACK_INCLUDE_LAUNCHER, chkIncludeLauncher.isSelected());
        controller.onNext();
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
        controller.getSettings().remove(MODPACK_NAME);
        controller.getSettings().remove(MODPACK_VERSION);
        controller.getSettings().remove(MODPACK_AUTHOR);
        controller.getSettings().remove(MODPACK_DESCRIPTION);
        controller.getSettings().remove(MODPACK_INCLUDE_LAUNCHER);
        controller.getSettings().remove(MODPACK_FILE);
    }

    @Override
    public String getTitle() {
        return i18n("modpack.wizard.step.1.title");
    }
}
