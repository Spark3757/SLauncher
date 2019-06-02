package ru.spark.slauncher.ui.versions;

import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.MaintainTask;
import ru.spark.slauncher.download.game.VersionJsonSaveTask;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.ui.Controllers;
import ru.spark.slauncher.ui.InstallerItem;
import ru.spark.slauncher.ui.ListPage;
import ru.spark.slauncher.ui.download.InstallerWizardProvider;
import ru.spark.slauncher.ui.download.UpdateInstallerWizardProvider;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import static ru.spark.slauncher.download.LibraryAnalyzer.LibraryType.*;
import static ru.spark.slauncher.util.i18n.I18n.i18n;

public class InstallerListPage extends ListPage<InstallerItem> {
    private Profile profile;
    private String versionId;
    private Version version;
    private String gameVersion;

    public void loadVersion(Profile profile, String versionId) {
        this.profile = profile;
        this.versionId = versionId;
        this.version = profile.getRepository().getResolvedVersion(versionId);
        this.gameVersion = null;

        Task.ofResult(() -> {
            gameVersion = GameVersion.minecraftVersion(profile.getRepository().getVersionJar(version)).orElse(null);

            return LibraryAnalyzer.analyze(version);
        }).thenAccept(Schedulers.javafx(), analyzer -> {
            Function<Library, Consumer<InstallerItem>> removeAction = library -> x -> {
                LinkedList<Library> newList = new LinkedList<>(version.getLibraries());
                newList.remove(library);
                new MaintainTask(version.setLibraries(newList))
                        .then(maintainedVersion -> new VersionJsonSaveTask(profile.getRepository(), maintainedVersion))
                        .with(profile.getRepository().refreshVersionsAsync())
                        .with(Task.of(Schedulers.javafx(), () -> loadVersion(this.profile, this.versionId)))
                        .start();
            };

            itemsProperty().clear();
            analyzer.get(FORGE).ifPresent(library -> itemsProperty().add(
                    new InstallerItem("Forge", library.getVersion(), () -> {
                        Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, "forge", library));
                    }, removeAction.apply(library))));
            analyzer.get(LITELOADER).ifPresent(library -> itemsProperty().add(
                    new InstallerItem("LiteLoader", library.getVersion(), () -> {
                        Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, "liteloader", library));
                    }, removeAction.apply(library))));
            analyzer.get(OPTIFINE).ifPresent(library -> itemsProperty().add(
                    new InstallerItem("OptiFine", library.getVersion(), () -> {
                        Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, "optifine", library));
                    }, removeAction.apply(library))));
        }).start();
    }

    @Override
    public void add() {
        if (gameVersion == null)
            Controllers.dialog(i18n("version.cannot_read"));
        else
            Controllers.getDecorator().startWizard(new InstallerWizardProvider(profile, gameVersion, version));
    }
}
