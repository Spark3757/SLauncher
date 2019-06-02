package ru.spark.slauncher.ui.versions;

import javafx.stage.FileChooser;
import ru.spark.slauncher.download.game.GameAssetDownloadTask;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.LauncherHelper;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.EnumGameDirectory;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.download.ModpackInstallWizardProvider;
import ru.spark.slauncher.ui.export.ExportWizardProvider;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class Versions {

    public static void deleteVersion(Profile profile, String version) {
        boolean isIndependent = profile.getVersionSetting(version).getGameDirType() == EnumGameDirectory.VERSION_FOLDER;
        boolean isMovingToTrashSupported = FileUtils.isMovingToTrashSupported();
        String message = isIndependent ? i18n("version.manage.remove.confirm.independent", version) :
                isMovingToTrashSupported ? i18n("version.manage.remove.confirm.trash", version, version + "_removed") :
                        i18n("version.manage.remove.confirm", version);
        Controllers.confirmDialog(message, i18n("message.confirm"), () -> {
            profile.getRepository().removeVersionFromDisk(version);
        }, null);
    }

    public static void renameVersion(Profile profile, String version) {
        Controllers.inputDialog(i18n("version.manage.rename.message"), (res, resolve, reject) -> {
            if (profile.getRepository().renameVersion(version, res)) {
                profile.getRepository().refreshVersionsAsync().start();
                resolve.run();
            } else {
                reject.accept(i18n("version.manage.rename.fail"));
            }
        }).setInitialText(version);
    }

    public static void exportVersion(Profile profile, String version) {
        Controllers.getDecorator().startWizard(new ExportWizardProvider(profile, version), i18n("modpack.wizard"));
    }

    public static void openFolder(Profile profile, String version) {
        FXUtils.openFolder(profile.getRepository().getRunDirectory(version));
    }

    public static void updateVersion(Profile profile, String version) {
        Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(profile, version));
    }

    public static void updateGameAssets(Profile profile, String version) {
        Version resolvedVersion = profile.getRepository().getResolvedVersion(version);
        TaskExecutor executor = new GameAssetDownloadTask(profile.getDependency(), resolvedVersion, GameAssetDownloadTask.DOWNLOAD_INDEX_FORCIBLY)
                .executor();
        Controllers.taskDialog(executor, i18n("version.manage.redownload_assets_index"));
        executor.start();
    }

    public static void cleanVersion(Profile profile, String id) {
        try {
            profile.getRepository().clean(id);
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Unable to clean game directory", e);
        }
    }

    public static void generateLaunchScript(Profile profile, String id) {
        if (checkForLaunching(profile, id)) {
            GameRepository repository = profile.getRepository();
            FileChooser chooser = new FileChooser();
            if (repository.getRunDirectory(id).isDirectory())
                chooser.setInitialDirectory(repository.getRunDirectory(id));
            chooser.setTitle(i18n("version.launch_script.save"));
            chooser.getExtensionFilters().add(OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS
                    ? new FileChooser.ExtensionFilter(i18n("extension.bat"), "*.bat")
                    : new FileChooser.ExtensionFilter(i18n("extension.sh"), "*.sh"));
            File file = chooser.showSaveDialog(Controllers.getStage());
            if (file != null)
                new LauncherHelper(profile, Accounts.getSelectedAccount(), id).makeLaunchScript(file);
        }
    }

    public static void launch(Profile profile, String id) {
        if (checkForLaunching(profile, id))
            new LauncherHelper(profile, Accounts.getSelectedAccount(), id).launch();
    }

    public static void testGame(Profile profile, String id) {
        if (checkForLaunching(profile, id)) {
            LauncherHelper helper = new LauncherHelper(profile, Accounts.getSelectedAccount(), id);
            helper.setTestMode();
            helper.launch();
        }
    }

    private static boolean checkForLaunching(Profile profile, String id) {
        if (Accounts.getSelectedAccount() == null)
            Controllers.getLeftPaneController().checkAccount();
        else if (id == null || !profile.getRepository().isLoaded() || !profile.getRepository().hasVersion(id))
            Controllers.dialog(i18n("version.empty.launch"));
        else
            return true;
        return false;
    }

    public static void modifyGlobalSettings(Profile profile) {
        VersionSettingsPage page = new VersionSettingsPage();
        page.loadVersion(profile, null);
        Controllers.navigate(page);
    }

    public static void modifyGameSettings(Profile profile, String version) {
        Controllers.getVersionPage().load(version, profile);
        Controllers.navigate(Controllers.getVersionPage());
    }
}
