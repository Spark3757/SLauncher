package ru.spark.slauncher.ui.versions;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.stage.FileChooser;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskExecutor;
import ru.spark.slauncher.task.TaskListener;
import ru.spark.slauncher.ui.*;
import ru.spark.slauncher.ui.download.UpdateInstallerWizardProvider;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.i18n.I18n;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static ru.spark.slauncher.ui.FXUtils.runInFX;

public class InstallerListPage extends ListPageBase<InstallerItem> {
    private Profile profile;
    private String versionId;
    private Version version;
    private String gameVersion;

    {
        FXUtils.applyDragListener(this, it -> Arrays.asList("jar", "exe").contains(FileUtils.getExtension(it)), mods -> {
            if (!mods.isEmpty())
                doInstallOffline(mods.get(0));
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new InstallerListPageSkin();
    }

    public CompletableFuture<?> loadVersion(Profile profile, String versionId) {
        this.profile = profile;
        this.versionId = versionId;
        this.version = profile.getRepository().getVersion(versionId);
        this.gameVersion = null;

        return CompletableFuture.supplyAsync(() -> {
            gameVersion = GameVersion.minecraftVersion(profile.getRepository().getVersionJar(version)).orElse(null);

            return LibraryAnalyzer.analyze(profile.getRepository().getResolvedPreservingPatchesVersion(versionId));
        }).thenAcceptAsync(analyzer -> {
            Function<String, Consumer<InstallerItem>> removeAction = libraryId -> x -> {
                profile.getDependency().removeLibraryAsync(version, libraryId)
                        .thenComposeAsync(profile.getRepository()::saveAsync)
                        .withComposeAsync(profile.getRepository().refreshVersionsAsync())
                        .withRunAsync(Schedulers.javafx(), () -> loadVersion(this.profile, this.versionId))
                        .start();
            };

            itemsProperty().clear();

            for (LibraryAnalyzer.LibraryType type : LibraryAnalyzer.LibraryType.values()) {
                String libraryId = type.getPatchId();
                String libraryVersion = analyzer.getVersion(type).orElse(null);
                Consumer<InstallerItem> action = "game".equals(libraryId) || libraryVersion == null ? null : removeAction.apply(libraryId);
                itemsProperty().add(new InstallerItem(libraryId, libraryVersion, () -> {
                    Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, libraryId, libraryVersion));
                }, action));
            }

            for (LibraryAnalyzer.LibraryMark mark : analyzer) {
                String libraryId = mark.getLibraryId();
                String libraryVersion = mark.getLibraryVersion();

                // we have done this library above.
                if (LibraryAnalyzer.LibraryType.fromPatchId(libraryId) != null)
                    continue;

                Consumer<InstallerItem> action = removeAction.apply(libraryId);
                if (libraryVersion != null && Lang.test(() -> profile.getDependency().getVersionList(libraryId)))
                    itemsProperty().add(
                            new InstallerItem(libraryId, libraryVersion, () -> {
                                Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, libraryId, libraryVersion));
                            }, action));
                else
                    itemsProperty().add(new InstallerItem(libraryId, libraryVersion, null, action));
            }
        }, Platform::runLater);
    }

    public void installOffline() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.i18n("install.installer.install_offline.extension"), "*.jar", "*.exe"));
        File file = chooser.showOpenDialog(Controllers.getStage());
        if (file != null) doInstallOffline(file);
    }

    private void doInstallOffline(File file) {
        Task<?> task = profile.getDependency().installLibraryAsync(version, file.toPath())
                .thenComposeAsync(profile.getRepository()::saveAsync)
                .thenComposeAsync(profile.getRepository().refreshVersionsAsync());
        task.setName(I18n.i18n("install.installer.install_offline"));
        TaskExecutor executor = task.executor(new TaskListener() {
            @Override
            public void onStop(boolean success, TaskExecutor executor) {
                runInFX(() -> {
                    if (success) {
                        loadVersion(profile, versionId);
                        Controllers.dialog(I18n.i18n("install.success"));
                    } else {
                        if (executor.getException() == null)
                            return;
                        UpdateInstallerWizardProvider.alertFailureMessage(executor.getException(), null);
                    }
                });
            }
        });
        Controllers.taskDialog(executor, I18n.i18n("install.installer.install_offline"));
        executor.start();
    }

    private class InstallerListPageSkin extends ToolbarListPageSkin<InstallerListPage> {

        InstallerListPageSkin() {
            super(InstallerListPage.this);
        }

        @Override
        protected List<Node> initializeToolbar(InstallerListPage skinnable) {
            return Collections.singletonList(
                    createToolbarButton(I18n.i18n("install.installer.install_offline"), SVG::plus, skinnable::installOffline));
        }
    }
}
