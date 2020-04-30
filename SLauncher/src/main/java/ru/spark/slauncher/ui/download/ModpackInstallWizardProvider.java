package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.game.ModpackHelper;
import ru.spark.slauncher.mod.MismatchedModpackTypeException;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.UnsupportedModpackException;
import ru.spark.slauncher.mod.curse.CurseCompletionException;
import ru.spark.slauncher.mod.server.ServerModpackManifest;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.construct.MessageDialogPane;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.i18n.I18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class ModpackInstallWizardProvider implements WizardProvider {
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
            settings.put(LocalModpackPage.MODPACK_FILE, file);
        if (updateVersion != null)
            settings.put(LocalModpackPage.MODPACK_NAME, updateVersion);
        settings.put(PROFILE, profile);
    }

    private Task<Void> finishModpackInstallingAsync(Map<String, Object> settings) {
        File selected = Lang.tryCast(settings.get(LocalModpackPage.MODPACK_FILE), File.class).orElse(null);
        ServerModpackManifest serverModpackManifest = Lang.tryCast(settings.get(RemoteModpackPage.MODPACK_SERVER_MANIFEST), ServerModpackManifest.class).orElse(null);
        Modpack modpack = Lang.tryCast(settings.get(LocalModpackPage.MODPACK_MANIFEST), Modpack.class).orElse(null);
        String name = Lang.tryCast(settings.get(LocalModpackPage.MODPACK_NAME), String.class).orElse(null);
        if ((selected == null && serverModpackManifest == null) || modpack == null || name == null) return null;

        if (updateVersion != null) {
            if (selected == null) {
                Controllers.dialog(I18n.i18n("modpack.unsupported"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                return null;
            }
            try {
                return ModpackHelper.getUpdateTask(profile, selected, modpack.getEncoding(), name, ModpackHelper.readModpackConfiguration(profile.getRepository().getModpackConfiguration(name)));
            } catch (UnsupportedModpackException e) {
                Controllers.dialog(I18n.i18n("modpack.unsupported"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
            } catch (MismatchedModpackTypeException e) {
                Controllers.dialog(I18n.i18n("modpack.mismatched_type"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
            } catch (IOException e) {
                Controllers.dialog(I18n.i18n("modpack.invalid"), I18n.i18n("message.error"), MessageDialogPane.MessageType.ERROR);
            }
            return null;
        } else {
            if (serverModpackManifest != null) {
                return ModpackHelper.getInstallTask(profile, serverModpackManifest, name, modpack)
                        .thenRunAsync(Schedulers.javafx(), () -> profile.setSelectedVersion(name));
            } else {
                return ModpackHelper.getInstallTask(profile, selected, name, modpack)
                        .thenRunAsync(Schedulers.javafx(), () -> profile.setSelectedVersion(name));
            }
        }
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("title", I18n.i18n("install.modpack"));
        settings.put("success_message", I18n.i18n("install.success"));
        settings.put("failure_callback", new FailureCallback() {
            @Override
            public void onFail(Map<String, Object> settings, Exception exception, Runnable next) {
                if (exception instanceof CurseCompletionException) {
                    if (exception.getCause() instanceof FileNotFoundException) {
                        Controllers.dialog(I18n.i18n("modpack.type.curse.not_found"), I18n.i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
                    } else {
                        Controllers.dialog(I18n.i18n("install.success"), I18n.i18n("install.success"), MessageDialogPane.MessageType.INFORMATION, next);
                    }
                } else {
                    UpdateInstallerWizardProvider.alertFailureMessage(exception, next);
                }
            }
        });

        return finishModpackInstallingAsync(settings);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new ModpackSelectionPage(controller);
            case 1:
                if (controller.getSettings().containsKey(LocalModpackPage.MODPACK_FILE))
                    return new LocalModpackPage(controller);
                else if (controller.getSettings().containsKey(RemoteModpackPage.MODPACK_SERVER_MANIFEST))
                    return new RemoteModpackPage(controller);
                else
                    throw new IllegalArgumentException();
            default:
                throw new IllegalStateException("error step " + step + ", settings: " + settings + ", pages: " + controller.getPages());
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }

    public static final String PROFILE = "PROFILE";
}
