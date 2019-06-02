package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.GameBuilder;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;

import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class VanillaInstallWizardProvider implements WizardProvider {
    public static final String PROFILE = "PROFILE";
    private final Profile profile;

    public VanillaInstallWizardProvider(Profile profile) {
        this.profile = profile;
    }

    @Override
    public void start(Map<String, Object> settings) {
        settings.put(PROFILE, profile);
    }

    private Task finishVersionDownloadingAsync(Map<String, Object> settings) {
        GameBuilder builder = profile.getDependency().gameBuilder();

        String name = (String) settings.get("name");
        builder.name(name);
        builder.gameVersion(((RemoteVersion) settings.get("game")).getGameVersion());

        if (settings.containsKey("forge"))
            builder.version((RemoteVersion) settings.get("forge"));

        if (settings.containsKey("liteloader"))
            builder.version((RemoteVersion) settings.get("liteloader"));

        if (settings.containsKey("optifine"))
            builder.version((RemoteVersion) settings.get("optifine"));

        return builder.buildAsync().whenComplete((a, b) -> profile.getRepository().refreshVersions())
                .then(Task.of(Schedulers.javafx(), () -> profile.setSelectedVersion(name)));
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> InstallerWizardProvider.alertFailureMessage(exception, next));

        return finishVersionDownloadingAsync(settings);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        DownloadProvider provider = profile.getDependency().getDownloadProvider();
        switch (step) {
            case 0:
                return new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.game")), "", provider, "game", () -> controller.onNext(new InstallersPage(controller, profile.getRepository(), provider)));
            default:
                throw new IllegalStateException("error step " + step + ", settings: " + settings + ", pages: " + controller.getPages());
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
