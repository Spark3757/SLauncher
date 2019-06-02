package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.VersionMismatchException;
import ru.spark.slauncher.download.game.LibraryDownloadException;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.DownloadException;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.construct.MessageDialogPane.MessageType;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.StringUtils;

import java.net.SocketTimeoutException;
import java.util.Map;

import static ru.spark.slauncher.download.LibraryAnalyzer.LibraryType.*;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class InstallerWizardProvider implements WizardProvider {
    private final Profile profile;
    private final String gameVersion;
    private final Version version;
    private final String forge;
    private final String liteLoader;
    private final String optiFine;

    public InstallerWizardProvider(Profile profile, String gameVersion, Version version) {
        this.profile = profile;
        this.gameVersion = gameVersion;
        this.version = version;

        LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(version);
        forge = analyzer.get(FORGE).map(Library::getVersion).orElse(null);
        liteLoader = analyzer.get(LITELOADER).map(Library::getVersion).orElse(null);
        optiFine = analyzer.get(OPTIFINE).map(Library::getVersion).orElse(null);
    }

    public static void alertFailureMessage(Exception exception, Runnable next) {
        if (exception instanceof LibraryDownloadException) {
            Controllers.dialog(i18n("launch.failed.download_library", ((LibraryDownloadException) exception).getLibrary().getName()) + "\n" + StringUtils.getStackTrace(exception.getCause()), i18n("install.failed.downloading"), MessageType.ERROR, next);
        } else if (exception instanceof DownloadException) {
            if (exception.getCause() instanceof SocketTimeoutException) {
                Controllers.dialog(i18n("install.failed.downloading.timeout", ((DownloadException) exception).getUrl()), i18n("install.failed.downloading"), MessageType.ERROR, next);
            } else {
                Controllers.dialog(i18n("install.failed.downloading.detail", ((DownloadException) exception).getUrl()) + "\n" +
                        StringUtils.getStackTrace(exception.getCause()), i18n("install.failed.downloading"), MessageType.ERROR, next);
            }
        } else if (exception instanceof OptiFineInstallTask.UnsupportedOptiFineInstallationException) {
            Controllers.dialog(i18n("install.failed.optifine_conflict"), i18n("install.failed"), MessageType.ERROR, next);
        } else if (exception instanceof UnsupportedOperationException) {
            Controllers.dialog(i18n("install.failed.install_online"), i18n("install.failed"), MessageType.ERROR, next);
        } else if (exception instanceof VersionMismatchException) {
            VersionMismatchException e = ((VersionMismatchException) exception);
            Controllers.dialog(i18n("install.failed.version_mismatch", e.getExpect(), e.getActual()), i18n("install.failed"), MessageType.ERROR, next);
        } else {
            Controllers.dialog(StringUtils.getStackTrace(exception), i18n("install.failed"), MessageType.ERROR, next);
        }
    }

    public Profile getProfile() {
        return profile;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public Version getVersion() {
        return version;
    }

    public String getForge() {
        return forge;
    }

    public String getLiteLoader() {
        return liteLoader;
    }

    public String getOptiFine() {
        return optiFine;
    }

    @Override
    public void start(Map<String, Object> settings) {
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> alertFailureMessage(exception, next));

        TaskResult<Version> ret = Task.ofResult(() -> version);

        if (settings.containsKey("forge"))
            ret = ret.thenCompose(profile.getDependency().installLibraryAsync((RemoteVersion) settings.get("forge")));

        if (settings.containsKey("liteloader"))
            ret = ret.thenCompose(profile.getDependency().installLibraryAsync((RemoteVersion) settings.get("liteloader")));

        if (settings.containsKey("optifine"))
            ret = ret.thenCompose(profile.getDependency().installLibraryAsync((RemoteVersion) settings.get("optifine")));

        return ret.then(profile.getRepository().refreshVersionsAsync());
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        DownloadProvider provider = profile.getDependency().getDownloadProvider();
        switch (step) {
            case 0:
                return new AdditionalInstallersPage(this, controller, profile.getRepository(), provider);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
