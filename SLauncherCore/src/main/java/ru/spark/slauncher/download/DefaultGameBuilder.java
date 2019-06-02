package ru.spark.slauncher.download;

import ru.spark.slauncher.download.game.*;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.ParallelTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.function.ExceptionalFunction;
import ru.spark.slauncher.util.gson.JsonUtils;

/**
 * @author Spark1337
 */
public class DefaultGameBuilder extends GameBuilder {

    private final DefaultDependencyManager dependencyManager;
    private final DownloadProvider downloadProvider;

    public DefaultGameBuilder(DefaultDependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
        this.downloadProvider = dependencyManager.getDownloadProvider();
    }

    public DefaultDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    @Override
    public Task buildAsync() {
        return new VersionJsonDownloadTask(gameVersion, dependencyManager).thenCompose(rawJson -> {
            Version original = JsonUtils.GSON.fromJson(rawJson, Version.class);
            Version version = original.setId(name).setJar(null);
            Task vanillaTask = downloadGameAsync(gameVersion, version).then(new ParallelTask(
                    new GameAssetDownloadTask(dependencyManager, version, GameAssetDownloadTask.DOWNLOAD_INDEX_FORCIBLY),
                    new GameLibrariesTask(dependencyManager, version) // Game libraries will be downloaded for multiple times partly, this time is for vanilla libraries.
            ).with(new VersionJsonSaveTask(dependencyManager.getGameRepository(), version))); // using [with] because download failure here are tolerant.

            TaskResult<Version> libraryTask = vanillaTask.thenSupply(() -> version);

            if (toolVersions.containsKey("forge"))
                libraryTask = libraryTask.thenCompose(libraryTaskHelper(gameVersion, "forge"));
            if (toolVersions.containsKey("liteloader"))
                libraryTask = libraryTask.thenCompose(libraryTaskHelper(gameVersion, "liteloader"));
            if (toolVersions.containsKey("optifine"))
                libraryTask = libraryTask.thenCompose(libraryTaskHelper(gameVersion, "optifine"));

            for (RemoteVersion remoteVersion : remoteVersions)
                libraryTask = libraryTask.thenCompose(dependencyManager.installLibraryAsync(remoteVersion));

            return libraryTask;
        }).whenComplete((isDependentSucceeded, exception) -> {
            if (!isDependentSucceeded)
                dependencyManager.getGameRepository().removeVersionFromDisk(name);
        });
    }

    private ExceptionalFunction<Version, TaskResult<Version>, ?> libraryTaskHelper(String gameVersion, String libraryId) {
        return version -> dependencyManager.installLibraryAsync(gameVersion, version, libraryId, toolVersions.get(libraryId));
    }

    protected Task downloadGameAsync(String gameVersion, Version version) {
        return new GameDownloadTask(dependencyManager, gameVersion, version);
    }

}
