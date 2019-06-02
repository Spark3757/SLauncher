package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.game.ModpackHelper;
import ru.spark.slauncher.mod.CurseCompletionException;
import ru.spark.slauncher.mod.MismatchedModpackTypeException;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.UnsupportedModpackException;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.construct.MessageDialogPane.MessageType;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.Lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class ModpackInstallWizardProvider implements WizardProvider {
    public static final String PROFILE = "PROFILE";
    private final Profile profile;
    private final File file;
    private final String updateVersion;

    public ModpackInstallWizardProvider(Profile profile) {
        this(profile, null, null);
    }

    public ModpackInstallWizardProvider(Profile profile, File modpackFile) {
        this(profile, modpackFile, null);
    }

    public ModpackInstallWizardProvider(Profile profile, String updateVersion) {
        this(profile, null, updateVersion);
    }

    public ModpackInstallWizardProvider(Profile profile, File modpackFile, String updateVersion) {
        this.profile = profile;
        this.file = modpackFile;
        this.updateVersion = updateVersion;
    }

    @Override
    public void start(Map<String, Object> settings) {
        if (file != null)
            settings.put(ModpackPage.MODPACK_FILE, file);
        if (updateVersion != null)
            settings.put(ModpackPage.MODPACK_NAME, updateVersion);
        settings.put(PROFILE, profile);
    }

    private Task finishModpackInstallingAsync(Map<String, Object> settings) {
        if (!settings.containsKey(ModpackPage.MODPACK_FILE))
            return null;

        File selected = Lang.tryCast(settings.get(ModpackPage.MODPACK_FILE), File.class).orElse(null);
        Modpack modpack = Lang.tryCast(settings.get(ModpackPage.MODPACK_MANIFEST), Modpack.class).orElse(null);
        String name = Lang.tryCast(settings.get(ModpackPage.MODPACK_NAME), String.class).orElse(null);
        if (selected == null || modpack == null || name == null) return null;

        if (updateVersion != null) {
            try {
                return ModpackHelper.getUpdateTask(profile, selected, modpack.getEncoding(), name, ModpackHelper.readModpackConfiguration(profile.getRepository().getModpackConfiguration(name)));
            } catch (UnsupportedModpackException e) {
                Controllers.dialog(i18n("modpack.unsupported"), i18n("message.error"), MessageType.ERROR);
            } catch (MismatchedModpackTypeException e) {
                Controllers.dialog(i18n("modpack.mismatched_type"), i18n("message.error"), MessageType.ERROR);
            } catch (IOException e) {
                Controllers.dialog(i18n("modpack.invalid"), i18n("message.error"), MessageType.ERROR);
            }
            return null;
        } else {
            return ModpackHelper.getInstallTask(profile, selected, name, modpack)
                    .then(Task.of(Schedulers.javafx(), () -> profile.setSelectedVersion(name)));
        }
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", new FailureCallback() {
            @Override
            public void onFail(Map<String, Object> settings, Exception exception, Runnable next) {
                if (exception instanceof CurseCompletionException) {
                    if (exception.getCause() instanceof FileNotFoundException) {
                        Controllers.dialog(i18n("modpack.type.curse.not_found"), i18n("install.failed"), MessageType.ERROR, next);
                    } else {
                        Controllers.dialog(i18n("modpack.type.curse.tolerable_error"), i18n("install.success"), MessageType.INFORMATION, next);
                    }
                } else {
                    InstallerWizardProvider.alertFailureMessage(exception, next);
                }
            }
        });

        return finishModpackInstallingAsync(settings);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new ModpackPage(controller);
            default:
                throw new IllegalStateException("error step " + step + ", settings: " + settings + ", pages: " + controller.getPages());
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
