package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Spark1337
 */
public final class GameVersionList extends VersionList<GameRemoteVersion> {

    public static final GameVersionList INSTANCE = new GameVersionList();

    private GameVersionList() {
    }

    @Override
    public boolean hasType() {
        return true;
    }

    @Override
    protected Collection<GameRemoteVersion> getVersionsImpl(String gameVersion) {
        lock.readLock().lock();
        try {
            return StringUtils.isBlank(gameVersion) ? versions.values() : versions.get(gameVersion);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Task refreshAsync(DownloadProvider downloadProvider) {
        GetTask task = new GetTask(NetworkUtils.toURL(downloadProvider.getVersionListURL()));
        return new Task() {
            @Override
            public Collection<Task> getDependents() {
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
                                remoteVersion.getUrl(),
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
