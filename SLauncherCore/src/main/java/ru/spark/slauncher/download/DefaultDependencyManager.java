package ru.spark.slauncher.download;

import ru.spark.slauncher.download.forge.ForgeInstallTask;
import ru.spark.slauncher.download.forge.ForgeRemoteVersion;
import ru.spark.slauncher.download.game.*;
import ru.spark.slauncher.download.liteloader.LiteLoaderInstallTask;
import ru.spark.slauncher.download.liteloader.LiteLoaderRemoteVersion;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.download.optifine.OptiFineRemoteVersion;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.ParallelTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.function.ExceptionalFunction;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Note: This class has no state.
 *
 * @author Spark1337
 */
public class DefaultDependencyManager extends AbstractDependencyManager {

    private final DefaultGameRepository repository;
    private final DownloadProvider downloadProvider;
    private final DefaultCacheRepository cacheRepository;

    public DefaultDependencyManager(DefaultGameRepository repository, DownloadProvider downloadProvider, DefaultCacheRepository cacheRepository) {
        this.repository = repository;
        this.downloadProvider = downloadProvider;
        this.cacheRepository = cacheRepository;
    }

    @Override
    public DefaultGameRepository getGameRepository() {
        return repository;
    }

    @Override
    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    @Override
    public DefaultCacheRepository getCacheRepository() {
        return cacheRepository;
    }

    @Override
    public GameBuilder gameBuilder() {
        return new DefaultGameBuilder(this);
    }

    @Override
    public Task checkGameCompletionAsync(Version version) {
        return new ParallelTask(
                Task.ofThen(() -> {
                    if (!repository.getVersionJar(version).exists())
                        return new GameDownloadTask(this, null, version);
                    else
                        return null;
                }),
                new GameAssetDownloadTask(this, version, GameAssetDownloadTask.DOWNLOAD_INDEX_IF_NECESSARY),
                new GameLibrariesTask(this, version)
        );
    }

    @Override
    public Task checkLibraryCompletionAsync(Version version) {
        return new GameLibrariesTask(this, version);
    }

    @Override
    public TaskResult<Version> installLibraryAsync(String gameVersion, Version version, String libraryId, String libraryVersion) {
        VersionList<?> versionList = getVersionList(libraryId);
        return versionList.loadAsync(gameVersion, getDownloadProvider())
                .thenCompose(() -> installLibraryAsync(version, versionList.getVersion(gameVersion, libraryVersion)
                        .orElseThrow(() -> new IllegalStateException("Remote library " + libraryId + " has no version " + libraryVersion))));
    }

    @Override
    public TaskResult<Version> installLibraryAsync(Version oldVersion, RemoteVersion libraryVersion) {
        TaskResult<Version> task;
        if (libraryVersion instanceof ForgeRemoteVersion)
            task = new ForgeInstallTask(this, oldVersion, (ForgeRemoteVersion) libraryVersion);
        else if (libraryVersion instanceof LiteLoaderRemoteVersion)
            task = new LiteLoaderInstallTask(this, oldVersion, (LiteLoaderRemoteVersion) libraryVersion);
        else if (libraryVersion instanceof OptiFineRemoteVersion)
            task = new OptiFineInstallTask(this, oldVersion, (OptiFineRemoteVersion) libraryVersion);
        else
            throw new IllegalArgumentException("Remote library " + libraryVersion + " is unrecognized.");
        return task
                .thenCompose(LibrariesUniqueTask::new)
                .thenCompose(MaintainTask::new)
                .thenCompose(newVersion -> new VersionJsonSaveTask(repository, newVersion));
    }


    public ExceptionalFunction<Version, TaskResult<Version>, ?> installLibraryAsync(RemoteVersion libraryVersion) {
        return version -> installLibraryAsync(version, libraryVersion);
    }

    public Task installLibraryAsync(Version oldVersion, Path installer) {
        return Task
                .of(() -> {
                })
                .thenCompose(() -> {
                    try {
                        return ForgeInstallTask.install(this, oldVersion, installer);
                    } catch (IOException ignore) {
                    }

                    try {
                        return OptiFineInstallTask.install(this, oldVersion, installer);
                    } catch (IOException ignore) {
                    }

                    throw new UnsupportedOperationException("Library cannot be recognized");
                })
                .thenCompose(LibrariesUniqueTask::new)
                .thenCompose(MaintainTask::new)
                .thenCompose(newVersion -> new VersionJsonSaveTask(repository, newVersion));
    }
}
