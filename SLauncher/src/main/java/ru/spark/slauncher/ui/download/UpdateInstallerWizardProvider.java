package ru.spark.slauncher.ui.download;

import javafx.scene.Node;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.MaintainTask;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.wizard.WizardController;
import ru.spark.slauncher.ui.wizard.WizardProvider;

import java.util.LinkedList;
import java.util.Map;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public final class UpdateInstallerWizardProvider implements WizardProvider {
    private final Profile profile;
    private final String gameVersion;
    private final Version version;
    private final String libraryId;
    private final Library oldLibrary;

    public UpdateInstallerWizardProvider(Profile profile, String gameVersion, Version version, String libraryId, Library oldLibrary) {
        this.profile = profile;
        this.gameVersion = gameVersion;
        this.version = version;
        this.libraryId = libraryId;
        this.oldLibrary = oldLibrary;
    }

    @Override
    public void start(Map<String, Object> settings) {
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> InstallerWizardProvider.alertFailureMessage(exception, next));

        // We remove library but not save it,
        // so if installation failed will not break down current version.
        LinkedList<Library> newList = new LinkedList<>(version.getLibraries());
        newList.remove(oldLibrary);
        return new MaintainTask(version.setLibraries(newList))
                .thenCompose(profile.getDependency().installLibraryAsync((RemoteVersion) settings.get(libraryId)))
                .then(profile.getRepository().refreshVersionsAsync());
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        DownloadProvider provider = profile.getDependency().getDownloadProvider();
        switch (step) {
            case 0:
                return new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer." + libraryId)), gameVersion, provider, libraryId, () -> {
                    Controllers.confirmDialog(i18n("install.change_version.confirm", i18n("install.installer." + libraryId), oldLibrary.getVersion(), ((RemoteVersion) settings.get(libraryId)).getSelfVersion()),
                            i18n("install.change_version"), controller::onFinish, controller::onCancel);
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
}
