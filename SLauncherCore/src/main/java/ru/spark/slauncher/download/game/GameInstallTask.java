package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static ru.spark.slauncher.download.LibraryAnalyzer.LibraryType.MINECRAFT;

public class GameInstallTask extends Task<Version> {

    private final DefaultGameRepository gameRepository;
    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final GameRemoteVersion remote;
    private final VersionJsonDownloadTask downloadTask;
    private final List<Task<?>> dependencies = new LinkedList<>();

    public GameInstallTask(DefaultDependencyManager dependencyManager, Version version, GameRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.gameRepository = dependencyManager.getGameRepository();
        this.version = version;
        this.remote = remoteVersion;
        this.downloadTask = new VersionJsonDownloadTask(remoteVersion.getGameVersion(), dependencyManager);
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return Collections.singleton(downloadTask);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isRelyingOnDependencies() {
        return false;
    }

    @Override
    public void execute() throws Exception {
        Version patch = JsonUtils.fromNonNullJson(downloadTask.getResult(), Version.class)
                .setId(MINECRAFT.getPatchId()).setVersion(remote.getGameVersion()).setJar(null).setPriority(0);
        setResult(patch);

        Version version = new Version(this.version.getId()).addPatch(patch);
        dependencies.add(Task.allOf(
                new GameDownloadTask(dependencyManager, remote.getGameVersion(), version),
                Task.allOf(
                        new GameAssetDownloadTask(dependencyManager, version, GameAssetDownloadTask.DOWNLOAD_INDEX_FORCIBLY, true),
                        new GameLibrariesTask(dependencyManager, version, true)
                ).withStage("slauncher.install.assets").withRunAsync(() -> {
                    // ignore failure
                })
        ).thenComposeAsync(gameRepository.saveAsync(version)));
    }

}
