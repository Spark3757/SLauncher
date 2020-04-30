package ru.spark.slauncher.ui.versions;

import javafx.stage.FileChooser;
import ru.spark.slauncher.download.game.GameAssetDownloadTask;
import ru.spark.slauncher.game.GameDirectoryType;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.LauncherHelper;
import ru.spark.slauncher.setting.Accounts;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.construct.PromptDialogPane;
import ru.spark.slauncher.ui.construct.Validator;
import ru.spark.slauncher.ui.download.ModpackInstallWizardProvider;
import ru.spark.slauncher.ui.export.ExportWizardProvider;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Versions {

    public static void deleteVersion(Profile profile, String version) {
        boolean isIndependent = profile.getVersionSetting(version).getGameDirType() == GameDirectoryType.VERSION_FOLDER;
        boolean isMovingToTrashSupported = FileUtils.isMovingToTrashSupported();
        String message = isIndependent ? I18n.i18n("version.manage.remove.confirm.independent", version) :
                isMovingToTrashSupported ? I18n.i18n("version.manage.remove.confirm.trash", version, version + "_removed") :
                        I18n.i18n("version.manage.remove.confirm", version);
        Controllers.confirm(message, I18n.i18n("message.confirm"), () -> {
            profile.getRepository().removeVersionFromDisk(version);
        }, null);
    }

    public static CompletableFuture<String> renameVersion(Profile profile, String version) {
        return Controllers.prompt(I18n.i18n("version.manage.rename.message"), (newName, resolve, reject) -> {
            if (!OperatingSystem.isNameValid(newName)) {
                reject.accept(I18n.i18n("install.new_game.malformed"));
                return;
            }
            if (profile.getRepository().renameVersion(version, newName)) {
                profile.getRepository().refreshVersionsAsync().start();
                resolve.run();
            } else {
                reject.accept(I18n.i18n("version.manage.rename.fail"));
            }
        }, version);
    }

    public static void exportVersion(Profile profile, String version) {
        Controllers.getDecorator().startWizard(new ExportWizardProvider(profile, version), I18n.i18n("modpack.wizard"));
    }

    public static void openFolder(Profile profile, String version) {
        FXUtils.openFolder(profile.getRepository().getRunDirectory(version));
    }

    public static void duplicateVersion(Profile profile, String version) {
        Controllers.prompt(
                new PromptDialogPane.Builder(I18n.i18n("version.manage.duplicate.prompt"), (res, resolve, reject) -> {
                    String newVersionName = ((PromptDialogPane.Builder.StringQuestion) res.get(0)).getValue();
                    boolean copySaves = ((PromptDialogPane.Builder.BooleanQuestion) res.get(1)).getValue();
                    Task.runAsync(() -> profile.getRepository().duplicateVersion(version, newVersionName, copySaves))
                            .thenComposeAsync(profile.getRepository().refreshVersionsAsync())
                            .whenComplete(Schedulers.javafx(), (result, exception) -> {
                                if (exception == null) {
                                    resolve.run();
                                } else {
                                    reject.accept(StringUtils.getStackTrace(exception));
                                    profile.getRepository().removeVersionFromDisk(newVersionName);
                                }
                            }).start();
                })
                        .addQuestion(new PromptDialogPane.Builder.StringQuestion(I18n.i18n("version.manage.duplicate.confirm"), version,
                                new Validator(I18n.i18n("install.new_game.already_exists"), newVersionName -> !profile.getRepository().hasVersion(newVersionName))))
                        .addQuestion(new PromptDialogPane.Builder.BooleanQuestion(I18n.i18n("version.manage.duplicate.duplicate_save"), false)));
    }

    public static void updateVersion(Profile profile, String version) {
        Controllers.getDecorator().startWizard(new ModpackInstallWizardProvider(profile, version));
    }

    public static void updateGameAssets(Profile profile, String version) {
        TaskExecutor executor = new GameAssetDownloadTask(profile.getDependency(), profile.getRepository().getVersion(version), GameAssetDownloadTask.DOWNLOAD_INDEX_FORCIBLY, true)
                .executor();
        Controllers.taskDialog(executor, I18n.i18n("version.manage.redownload_assets_index"));
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
            chooser.setTitle(I18n.i18n("version.launch_script.save"));
            chooser.getExtensionFilters().add(OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS
                    ? new FileChooser.ExtensionFilter(I18n.i18n("extension.bat"), "*.bat")
                    : new FileChooser.ExtensionFilter(I18n.i18n("extension.sh"), "*.sh"));
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
            Controllers.getRootPage().checkAccount();
        else if (id == null || !profile.getRepository().isLoaded() || !profile.getRepository().hasVersion(id))
            Controllers.dialog(I18n.i18n("version.empty.launch"), I18n.i18n("launch.failed"), MessageDialogPane.MessageType.ERROR, () -> {
                Controllers.getRootPage().getSelectionModel().select(Controllers.getRootPage().getGameTab());
            });
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
        Controllers.getVersionPage().setVersion(version, profile);
        // VersionPage.loadVersion will be invoked after navigation
        Controllers.navigate(Controllers.getVersionPage());
    }
}
