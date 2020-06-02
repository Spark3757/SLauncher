package ru.spark.slauncher.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.InstallerItem;
import ru.spark.slauncher.ui.SVG;
import ru.spark.slauncher.ui.construct.Validator;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardPage;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class InstallersPage extends Control implements WizardPage {
    protected final WizardController controller;

    protected InstallerItem.InstallerItemGroup group = new InstallerItem.InstallerItemGroup();
    protected JFXTextField txtName = new JFXTextField();
    protected BooleanProperty installable = new SimpleBooleanProperty();

    public InstallersPage(WizardController controller, GameRepository repository, String gameVersion, InstallerWizardDownloadProvider downloadProvider) {
        this.controller = controller;

        Validator hasVersion = new Validator(s -> !repository.hasVersion(s) && StringUtils.isNotBlank(s));
        hasVersion.setMessage(i18n("install.new_game.already_exists"));
        Validator nameValidator = new Validator(OperatingSystem::isNameValid);
        nameValidator.setMessage(i18n("install.new_game.malformed"));
        txtName.getValidators().addAll(hasVersion, nameValidator);
        installable.bind(Bindings.createBooleanBinding(() -> txtName.validate(),
                txtName.textProperty()));
        txtName.setText(gameVersion);

        group.game.installable.setValue(false);

        for (InstallerItem library : group.getLibraries()) {
            String libraryId = library.getLibraryId();
            if (libraryId.equals("game")) continue;
            library.action.set(e -> {
                if (library.incompatibleLibraryName.get() == null)
                    controller.onNext(new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer." + libraryId)), gameVersion, downloadProvider, libraryId, () -> controller.onPrev(false)));
            });
            library.removeAction.set(e -> {
                controller.getSettings().remove(libraryId);
                reload();
            });
        }
    }

    @Override
    public String getTitle() {
        return i18n("install.new_game");
    }

    private String getVersion(String id) {
        return ((RemoteVersion) controller.getSettings().get(id)).getSelfVersion();
    }

    protected void reload() {
        for (InstallerItem library : group.getLibraries()) {
            String libraryId = library.getLibraryId();
            if (controller.getSettings().containsKey(libraryId)) {
                library.libraryVersion.set(getVersion(libraryId));
                library.removable.set(true);
            } else {
                library.libraryVersion.set(null);
                library.removable.set(false);
            }
        }
    }

    @Override
    public void onNavigate(Map<String, Object> settings) {
        reload();
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
    }

    @FXML
    protected void onInstall() {
        controller.getSettings().put("name", txtName.getText());
        controller.onFinish();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new InstallersPageSkin(this);
    }

    protected static class InstallersPageSkin extends SkinBase<InstallersPage> {

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        protected InstallersPageSkin(InstallersPage control) {
            super(control);

            BorderPane root = new BorderPane();
            root.setPadding(new Insets(16));

            VBox list = new VBox(8);
            root.setCenter(list);
            {
                HBox versionNamePane = new HBox(8);
                versionNamePane.setAlignment(Pos.CENTER_LEFT);
                versionNamePane.getStyleClass().add("card");
                versionNamePane.setStyle("-fx-padding: 20 8 20 16");

                versionNamePane.getChildren().add(new Label(i18n("archive.name")));

                control.txtName.setMaxWidth(300);
                versionNamePane.getChildren().add(control.txtName);
                list.getChildren().add(versionNamePane);
            }

            list.getChildren().addAll(control.group.getLibraries());

            {
                JFXButton installButton = new JFXButton(i18n("button.install"));
                installButton.disableProperty().bind(control.installable.not());
                installButton.getStyleClass().add("jfx-button-raised");
                installButton.setButtonType(JFXButton.ButtonType.RAISED);
                installButton.setPrefWidth(100);
                installButton.setPrefHeight(40);
                installButton.setOnMouseClicked(e -> control.onInstall());
                BorderPane.setAlignment(installButton, Pos.CENTER_RIGHT);
                root.setBottom(installButton);
            }

            getChildren().setAll(root);
        }
    }
}