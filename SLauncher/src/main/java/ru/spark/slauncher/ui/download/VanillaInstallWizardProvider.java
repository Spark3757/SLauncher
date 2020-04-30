package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.GameBuilder;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.setting.DownloadProviders;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;
import ru.spark.slauncher.util.i18n.I18n;

import java.util.Map;

public final class VanillaInstallWizardProvider implements WizardProvider {
    private final Profile profile;
    private final DefaultDependencyManager dependencyManager;
    private final InstallerWizardDownloadProvider downloadProvider;

    public VanillaInstallWizardProvider(Profile profile) {
        this.profile = profile;
        this.downloadProvider = new InstallerWizardDownloadProvider(DownloadProviders.getDownloadProvider());
        this.dependencyManager = profile.getDependency(downloadProvider);
    }

    @Override
    public void start(Map<String, Object> settings) {
        settings.put(PROFILE, profile);
    }

    private Task<Void> finishVersionDownloadingAsync(Map<String, Object> settings) {
        GameBuilder builder = dependencyManager.gameBuilder();

        String name = (String) settings.get("name");
        builder.name(name);
        builder.gameVersion(((RemoteVersion) settings.get("game")).getGameVersion());

        for (Map.Entry<String, Object> entry : settings.entrySet())
            if (!"game".equals(entry.getKey()) && entry.getValue() instanceof RemoteVersion)
                builder.version((RemoteVersion) entry.getValue());

        return builder.buildAsync().whenComplete(any -> profile.getRepository().refreshVersions())
                .thenRunAsync(Schedulers.javafx(), () -> profile.setSelectedVersion(name));
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("title", I18n.i18n("install.new_game"));
        settings.put("success_message", I18n.i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> UpdateInstallerWizardProvider.alertFailureMessage(exception, next));

        return finishVersionDownloadingAsync(settings);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new VersionsPage(controller, I18n.i18n("install.installer.choose", I18n.i18n("install.installer.game")), "", downloadProvider, "game",
                        () -> controller.onNext(new InstallersPage(controller, profile.getRepository(), ((RemoteVersion) controller.getSettings().get("game")).getGameVersion(), downloadProvider)));
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
