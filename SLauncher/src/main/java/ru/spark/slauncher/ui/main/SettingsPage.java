package ru.spark.slauncher.ui.main;

import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.effects.JFXDepthManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.setting.DownloadProviders;
import ru.spark.slauncher.setting.EnumBackgroundImage;
import ru.spark.slauncher.setting.EnumCommonDirectory;
import ru.spark.slauncher.setting.Settings;
import ru.spark.slauncher.setting.Theme;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.Validator;
import ru.spark.slauncher.ui.decorator.DecoratorPage;
import ru.spark.slauncher.upgrade.RemoteVersion;
import ru.spark.slauncher.upgrade.UpdateChannel;
import ru.spark.slauncher.upgrade.UpdateChecker;
import ru.spark.slauncher.upgrade.UpdateHandler;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.i18n.Locales;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.javafx.ExtendedProperties;
import ru.spark.slauncher.util.javafx.SafeStringConverter;

public final class SettingsPage extends SettingsView implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(I18n.i18n("settings.launcher")));

    private InvalidationListener updateListener;

    public SettingsPage() {
        FXUtils.smoothScrolling(scroll);

        // ==== Download sources ====
        cboDownloadSource.getItems().setAll(DownloadProviders.providersById.keySet());
        ExtendedProperties.selectedItemPropertyFor(cboDownloadSource).bindBidirectional(ConfigHolder.config().downloadTypeProperty());
        // ====

        // ==== Font ====
        cboFont.valueProperty().bindBidirectional(ConfigHolder.config().fontFamilyProperty());

        txtFontSize.textProperty().bindBidirectional(ConfigHolder.config().fontSizeProperty(),
                SafeStringConverter.fromFiniteDouble()
                        .restrict(it -> it > 0)
                        .fallbackTo(12.0)
                        .asPredicate(Validator.addTo(txtFontSize)));

        lblDisplay.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(ConfigHolder.config().getFontFamily(), ConfigHolder.config().getFontSize()),
                ConfigHolder.config().fontFamilyProperty(), ConfigHolder.config().fontSizeProperty()));
        // ====

        // ==== Languages ====
        cboLanguage.getItems().setAll(Locales.LOCALES);
        ExtendedProperties.selectedItemPropertyFor(cboLanguage).bindBidirectional(ConfigHolder.config().localizationProperty());
        // ====

        // ==== Proxy ====
        txtProxyHost.textProperty().bindBidirectional(ConfigHolder.config().proxyHostProperty());
        txtProxyPort.textProperty().bindBidirectional(ConfigHolder.config().proxyPortProperty(),
                SafeStringConverter.fromInteger()
                        .restrict(it -> it >= 0 && it <= 0xFFFF)
                        .fallbackTo(0)
                        .asPredicate(Validator.addTo(txtProxyPort)));
        txtProxyUsername.textProperty().bindBidirectional(ConfigHolder.config().proxyUserProperty());
        txtProxyPassword.textProperty().bindBidirectional(ConfigHolder.config().proxyPassProperty());

        proxyPane.disableProperty().bind(chkDisableProxy.selectedProperty());
        authPane.disableProperty().bind(chkProxyAuthentication.selectedProperty().not());

        ExtendedProperties.reversedSelectedPropertyFor(chkDisableProxy).bindBidirectional(ConfigHolder.config().hasProxyProperty());
        chkProxyAuthentication.selectedProperty().bindBidirectional(ConfigHolder.config().hasProxyAuthProperty());

        ToggleGroup proxyConfigurationGroup = new ToggleGroup();
        chkProxyHttp.setUserData(Proxy.Type.HTTP);
        chkProxyHttp.setToggleGroup(proxyConfigurationGroup);
        chkProxySocks.setUserData(Proxy.Type.SOCKS);
        chkProxySocks.setToggleGroup(proxyConfigurationGroup);
        ExtendedProperties.selectedItemPropertyFor(proxyConfigurationGroup, Proxy.Type.class).bindBidirectional(ConfigHolder.config().proxyTypeProperty());
        // ====

        fileCommonLocation.loadChildren(Collections.singletonList(
                fileCommonLocation.createChildren(I18n.i18n("launcher.cache_directory.default"), EnumCommonDirectory.DEFAULT)
        ), EnumCommonDirectory.CUSTOM);
        fileCommonLocation.selectedDataProperty().bindBidirectional(ConfigHolder.config().commonDirTypeProperty());
        fileCommonLocation.customTextProperty().bindBidirectional(ConfigHolder.config().commonDirectoryProperty());
        fileCommonLocation.subtitleProperty().bind(
                Bindings.createObjectBinding(() -> Optional.ofNullable(Settings.instance().getCommonDirectory())
                                .orElse(I18n.i18n("launcher.cache_directory.disabled")),
                        ConfigHolder.config().commonDirectoryProperty(), ConfigHolder.config().commonDirTypeProperty()));

        // ==== Update ====
        FXUtils.installFastTooltip(btnUpdate, I18n.i18n("update.tooltip"));
        updateListener = any -> {
            btnUpdate.setVisible(UpdateChecker.isOutdated());

            if (UpdateChecker.isOutdated()) {
                lblUpdateSub.setText(I18n.i18n("update.newest_version", UpdateChecker.getLatestVersion().getVersion()));
                lblUpdateSub.getStyleClass().setAll("update-label");

                lblUpdate.setText(I18n.i18n("update.found"));
                lblUpdate.getStyleClass().setAll("update-label");
            } else if (UpdateChecker.isCheckingUpdate()) {
                lblUpdateSub.setText(I18n.i18n("update.checking"));
                lblUpdateSub.getStyleClass().setAll("subtitle-label");

                lblUpdate.setText(I18n.i18n("update"));
                lblUpdate.getStyleClass().setAll();
            } else {
                lblUpdateSub.setText(I18n.i18n("update.latest"));
                lblUpdateSub.getStyleClass().setAll("subtitle-label");

                lblUpdate.setText(I18n.i18n("update"));
                lblUpdate.getStyleClass().setAll();
            }
        };
        UpdateChecker.latestVersionProperty().addListener(new WeakInvalidationListener(updateListener));
        UpdateChecker.outdatedProperty().addListener(new WeakInvalidationListener(updateListener));
        UpdateChecker.checkingUpdateProperty().addListener(new WeakInvalidationListener(updateListener));
        updateListener.invalidated(null);

        lblUpdateNote.setWrappingWidth(470);

        ToggleGroup updateChannelGroup = new ToggleGroup();
        chkUpdateDev.setToggleGroup(updateChannelGroup);
        chkUpdateDev.setUserData(UpdateChannel.DEVELOPMENT);
        chkUpdateStable.setToggleGroup(updateChannelGroup);
        chkUpdateStable.setUserData(UpdateChannel.STABLE);
        ExtendedProperties.selectedItemPropertyFor(updateChannelGroup, UpdateChannel.class).bindBidirectional(ConfigHolder.config().updateChannelProperty());
        // ====

        // ==== Background ====
        backgroundItem.loadChildren(Collections.singletonList(
                backgroundItem.createChildren(I18n.i18n("launcher.background.default"), EnumBackgroundImage.DEFAULT)
        ), EnumBackgroundImage.CUSTOM);
        backgroundItem.customTextProperty().bindBidirectional(ConfigHolder.config().backgroundImageProperty());
        backgroundItem.selectedDataProperty().bindBidirectional(ConfigHolder.config().backgroundImageTypeProperty());
        backgroundItem.subtitleProperty().bind(
                new When(backgroundItem.selectedDataProperty().isEqualTo(EnumBackgroundImage.DEFAULT))
                        .then(I18n.i18n("launcher.background.default"))
                        .otherwise(ConfigHolder.config().backgroundImageProperty()));
        // ====

        // ==== Theme ====
        JFXColorPicker picker = new JFXColorPicker(Color.web(ConfigHolder.config().getTheme().getColor()), null);
        picker.setCustomColorText(I18n.i18n("color.custom"));
        picker.setRecentColorsText(I18n.i18n("color.recent"));
        picker.getCustomColors().setAll(Theme.SUGGESTED_COLORS);
        picker.setOnAction(e -> {
            Theme theme = Theme.custom(Theme.getColorDisplayName(picker.getValue()));
            ConfigHolder.config().setTheme(theme);
            Controllers.getScene().getStylesheets().setAll(theme.getStylesheets());
        });
        themeColorPickerContainer.getChildren().setAll(picker);
        Platform.runLater(() -> JFXDepthManager.setDepth(picker, 0));
        // ====
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @Override
    protected void onUpdate() {
        RemoteVersion target = UpdateChecker.getLatestVersion();
        if (target == null) {
            return;
        }
        UpdateHandler.updateFrom(target);
    }

    @Override
    protected void onExportLogs() {
        // We cannot determine which file is JUL using.
        // So we write all the logs to a new file.
        Lang.thread(() -> {
            Path logFile = Paths.get("slauncher-exported-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".log").toAbsolutePath();

            Logging.LOG.info("Exporting logs to " + logFile);
            try {
                Files.write(logFile, Logging.getRawLogs());
            } catch (IOException e) {
                Platform.runLater(() -> Controllers.dialog(I18n.i18n("settings.launcher.launcher_log.export.failed") + "\n" + e, null, MessageDialogPane.MessageType.ERROR));
                Logging.LOG.log(Level.WARNING, "Failed to export logs", e);
                return;
            }

            Platform.runLater(() -> Controllers.dialog(I18n.i18n("settings.launcher.launcher_log.export.success", logFile)));
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(logFile.toFile());
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    protected void onHelp() {
        FXUtils.openLink(Metadata.HELP_URL);
    }

    @Override
    protected void onSponsor() {
        FXUtils.openLink("https://vk.com/slauncher");
    }

    @Override
    protected void clearCacheDirectory() {
        FileUtils.cleanDirectoryQuietly(new File(Settings.instance().getCommonDirectory(), "cache"));
    }
}
