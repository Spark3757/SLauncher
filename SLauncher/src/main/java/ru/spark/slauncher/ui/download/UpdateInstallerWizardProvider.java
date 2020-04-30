package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.download.ArtifactMalformedException;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.VersionMismatchException;
import ru.spark.slauncher.download.fabric.FabricInstallTask;
import ru.spark.slauncher.download.game.GameAssetIndexDownloadTask;
import ru.spark.slauncher.download.game.LibraryDownloadException;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.DownloadProviders;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.DownloadException;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.ResponseCodeException;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

public final class UpdateInstallerWizardProvider implements WizardProvider {
    private final Profile profile;
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final String libraryId;
    private final String oldLibraryVersion;
    private final InstallerWizardDownloadProvider downloadProvider;

    public UpdateInstallerWizardProvider(@NotNull Profile profile, @NotNull String gameVersion, @NotNull Version version, @NotNull String libraryId, @Nullable String oldLibraryVersion) {
        this.profile = profile;
        this.gameVersion = gameVersion;
        this.version = version;
        this.libraryId = libraryId;
        this.oldLibraryVersion = oldLibraryVersion;
        this.downloadProvider = new InstallerWizardDownloadProvider(DownloadProviders.getDownloadProvider());
        this.dependencyManager = profile.getDependency(downloadProvider);
    }

    @Override
    public void start(Map<String, Object> settings) {
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("title", I18n.i18n("install.change_version"));
        settings.put("success_message", I18n.i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> alertFailureMessage(exception, next));

        // We remove library but not save it,
        // so if installation failed will not break down current version.
        Task<Version> ret = Task.supplyAsync(() -> version);
        List<String> stages = new ArrayList<>();
        for (Object value : settings.values()) {
            if (value instanceof RemoteVersion) {
                RemoteVersion remoteVersion = (RemoteVersion) value;
                ret = ret.thenComposeAsync(version -> dependencyManager.installLibraryAsync(version, remoteVersion));
                stages.add(String.format("slauncher.install.%s:%s", remoteVersion.getLibraryId(), remoteVersion.getSelfVersion()));
                if ("game".equals(remoteVersion.getLibraryId())) {
                    stages.add("slauncher.install.assets");
                }
            } else if (value instanceof RemoveVersionAction) {
                ret = ret.thenComposeAsync(version -> dependencyManager.removeLibraryAsync(version, ((RemoveVersionAction) value).libraryId));
            }
        }

        return ret.thenComposeAsync(profile.getRepository()::saveAsync).thenComposeAsync(profile.getRepository().refreshVersionsAsync()).withStagesHint(stages);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new VersionsPage(controller, I18n.i18n("install.installer.choose", I18n.i18n("install.installer." + libraryId)), gameVersion, downloadProvider, libraryId, () -> {
                    if (oldLibraryVersion == null) {
                        controller.onFinish();
                    } else if ("game".equals(libraryId)) {
                        String newGameVersion = ((RemoteVersion) settings.get(libraryId)).getSelfVersion();
                        controller.onNext(new AdditionalInstallersPage(newGameVersion, version, controller, profile.getRepository(), downloadProvider));
                    } else {
                        Controllers.confirm(I18n.i18n("install.change_version.confirm", I18n.i18n("install.installer." + libraryId), oldLibraryVersion, ((RemoteVersion) settings.get(libraryId)).getSelfVersion()),
                                I18n.i18n("install.change_version"), controller::onFinish, controller::onCancel);
                    }
                });
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public boolean cancelIfCannotGoBack() {
        // VersionsPage will call wizardController.onPrev(cleanUp = true) when list is empty.
        // So we cancel this wizard when VersionPage calls the method.
        return true;
    }

    public static void alertFailureMessage(Exception exception, Runnable next) {
        if (exception instanceof LibraryDownloadException) {
            String message = I18n.i18n("launch.failed.download_library", ((LibraryDownloadException) exception).getLibrary().getName()) + "\n";
            if (exception.getCause() instanceof ResponseCodeException) {
                ResponseCodeException rce = (ResponseCodeException) exception.getCause();
                int responseCode = rce.getResponseCode();
                URL url = rce.getUrl();
                if (responseCode == 404)
                    message += I18n.i18n("download.code.404", url);
                else
                    message += I18n.i18n("download.failed", url, responseCode);
            } else {
                message += StringUtils.getStackTrace(exception.getCause());
            }
            Controllers.dialog(message, I18n.i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof DownloadException) {
            URL url = ((DownloadException) exception).getUrl();
            if (exception.getCause() instanceof SocketTimeoutException) {
                Controllers.dialog(I18n.i18n("install.failed.downloading.timeout", url), I18n.i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
            } else if (exception.getCause() instanceof ResponseCodeException) {
                ResponseCodeException responseCodeException = (ResponseCodeException) exception.getCause();
                if (I18n.hasKey("download.code." + responseCodeException.getResponseCode())) {
                    Controllers.dialog(I18n.i18n("download.code." + responseCodeException.getResponseCode(), url), I18n.i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
                } else {
                    Controllers.dialog(I18n.i18n("install.failed.downloading.detail", url) + "\n" + StringUtils.getStackTrace(exception.getCause()), I18n.i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
                }
            } else {
                Controllers.dialog(I18n.i18n("install.failed.downloading.detail", url) + "\n" + StringUtils.getStackTrace(exception.getCause()), I18n.i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
            }
        } else if (exception instanceof OptiFineInstallTask.UnsupportedOptiFineInstallationException ||
                exception instanceof FabricInstallTask.UnsupportedFabricInstallationException) {
            Controllers.dialog(I18n.i18n("install.failed.optifine_conflict"), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof DefaultDependencyManager.UnsupportedLibraryInstallerException) {
            Controllers.dialog(I18n.i18n("install.failed.install_online"), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof ArtifactMalformedException || exception instanceof ZipException) {
            Controllers.dialog(I18n.i18n("install.failed.malformed"), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof GameAssetIndexDownloadTask.GameAssetIndexMalformedException) {
            Controllers.dialog(I18n.i18n("assets.index.malformed"), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof VersionMismatchException) {
            VersionMismatchException e = ((VersionMismatchException) exception);
            Controllers.dialog(I18n.i18n("install.failed.version_mismatch", e.getExpect(), e.getActual()), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else {
            Controllers.dialog(StringUtils.getStackTrace(exception), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        }
    }

    public static class RemoveVersionAction {
        private final String libraryId;

        public RemoveVersionAction(String libraryId) {
            this.libraryId = libraryId;
        }
    }
}
