package ru.spark.slauncher.upgrade;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.setting.ConfigHolder;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.Pair;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.IOException;
import java.util.logging.Level;

public final class UpdateChecker {
    private static ObjectProperty<RemoteVersion> latestVersion = new SimpleObjectProperty<>();
    private static BooleanBinding outdated = Bindings.createBooleanBinding(
            () -> {
                RemoteVersion latest = latestVersion.get();
                if (latest == null || isDevelopmentVersion(Metadata.VERSION)) {
                    return false;
                } else {
                    return VersionNumber.asVersion(latest.getVersion()).compareTo(VersionNumber.asVersion(Metadata.VERSION)) > 0;
                }
            },
            latestVersion);
    private static ReadOnlyBooleanWrapper checkingUpdate = new ReadOnlyBooleanWrapper(false);

    private UpdateChecker() {
    }

    public static void init() {
        ConfigHolder.config().updateChannelProperty().addListener(FXUtils.onInvalidating(UpdateChecker::requestCheckUpdate));
        requestCheckUpdate();
    }

    public static RemoteVersion getLatestVersion() {
        return latestVersion.get();
    }

    public static ReadOnlyObjectProperty<RemoteVersion> latestVersionProperty() {
        return latestVersion;
    }

    public static boolean isOutdated() {
        return outdated.get();
    }

    public static ObservableBooleanValue outdatedProperty() {
        return outdated;
    }

    public static boolean isCheckingUpdate() {
        return checkingUpdate.get();
    }

    public static ReadOnlyBooleanProperty checkingUpdateProperty() {
        return checkingUpdate.getReadOnlyProperty();
    }

    private static RemoteVersion checkUpdate(UpdateChannel channel) throws IOException {
        if (!IntegrityChecker.isSelfVerified() && !"true".equals(System.getProperty("slauncher.self_integrity_check.disable"))) {
            throw new IOException("Self verification failed");
        }

        String url = NetworkUtils.withQuery(Metadata.UPDATE_URL, Lang.mapOf(
                Pair.pair("version", Metadata.VERSION),
                Pair.pair("channel", channel.channelName)));

        return RemoteVersion.fetch(url);
    }

    private static boolean isDevelopmentVersion(String version) {
        return version.contains("@") || // eg. @develop@
                version.contains("SNAPSHOT"); // eg. 3.1.SNAPSHOT
    }

    public static void requestCheckUpdate() {
        Platform.runLater(() -> {
            if (isCheckingUpdate())
                return;
            checkingUpdate.set(true);
            UpdateChannel channel = ConfigHolder.config().getUpdateChannel();

            Lang.thread(() -> {
                RemoteVersion result = null;
                try {
                    result = checkUpdate(channel);
                    Logging.LOG.info("Latest version (" + channel + ") is " + result);
                } catch (IOException e) {
                    Logging.LOG.log(Level.WARNING, "Failed to check for update", e);
                }

                RemoteVersion finalResult = result;
                Platform.runLater(() -> {
                    checkingUpdate.set(false);
                    if (finalResult != null) {
                        if (channel.equals(ConfigHolder.config().getUpdateChannel())) {
                            latestVersion.set(finalResult);
                        } else {
                            // the channel has been changed during the period
                            // check update again
                            requestCheckUpdate();
                        }
                    }
                });
            }, "Update Checker", true);
        });
    }
}
