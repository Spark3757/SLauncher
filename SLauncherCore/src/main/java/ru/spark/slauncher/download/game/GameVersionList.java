package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * @author spark1337
 */
public final class GameVersionList extends VersionList<GameRemoteVersion> {
    private final DownloadProvider downloadProvider;

    public GameVersionList(DownloadProvider downloadProvider) {
        this.downloadProvider = downloadProvider;
    }

    @Override
    public boolean hasType() {
        return true;
    }

    @Override
    protected Collection<GameRemoteVersion> getVersionsImpl(String gameVersion) {
        return versions.values();
    }

    @Override
    public Task<?> refreshAsync() {
        GetTask task = new GetTask(NetworkUtils.toURL(downloadProvider.getVersionListURL()));
        return new Task<Void>() {
            @Override
            public Collection<Task<?>> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                lock.writeLock().lock();

                try {
                    versions.clear();

                    GameRemoteVersions root = JsonUtils.GSON.fromJson(task.getResult(), GameRemoteVersions.class);
                    for (GameRemoteVersionInfo remoteVersion : root.getVersions()) {
                        versions.put(remoteVersion.getGameVersion(), new GameRemoteVersion(
                                remoteVersion.getGameVersion(),
                                remoteVersion.getGameVersion(),
                                Collections.singletonList(remoteVersion.getUrl()),
                                remoteVersion.getType(), remoteVersion.getReleaseTime())
                        );
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }
        };
    }
}
