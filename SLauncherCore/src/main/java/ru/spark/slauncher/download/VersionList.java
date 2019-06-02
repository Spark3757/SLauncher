package ru.spark.slauncher.download;

import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.SimpleMultimap;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The remote version list.
 *
 * @param <T> The subclass of {@code RemoteVersion}, the type of RemoteVersion.
 * @author Spark1337
 */
public abstract class VersionList<T extends RemoteVersion> {

    /**
     * the remote version list.
     * key: game version.
     * values: corresponding remote versions.
     */
    protected final SimpleMultimap<String, T> versions = new SimpleMultimap<String, T>(HashMap::new, TreeSet::new);
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * True if the version list has been loaded.
     */
    public boolean isLoaded() {
        return !versions.isEmpty();
    }

    /**
     * True if the version list that contains the remote versions which depends on the specific game version has been loaded.
     *
     * @param gameVersion the remote version depends on
     */
    public boolean isLoaded(String gameVersion) {
        return !versions.get(gameVersion).isEmpty();
    }

    public abstract boolean hasType();

    /**
     * @param downloadProvider DownloadProvider
     * @return the task to reload the remote version list.
     */
    public abstract Task refreshAsync(DownloadProvider downloadProvider);

    /**
     * @param gameVersion      the remote version depends on
     * @param downloadProvider DownloadProvider
     * @return the task to reload the remote version list.
     */
    public Task refreshAsync(String gameVersion, DownloadProvider downloadProvider) {
        return refreshAsync(downloadProvider);
    }

    public Task loadAsync(DownloadProvider downloadProvider) {
        return Task.ofThen(() -> {
            lock.readLock().lock();
            boolean loaded;

            try {
                loaded = isLoaded();
            } finally {
                lock.readLock().unlock();
            }
            return loaded ? null : refreshAsync(downloadProvider);
        });
    }

    public Task loadAsync(String gameVersion, DownloadProvider downloadProvider) {
        return Task.ofThen(() -> {
            lock.readLock().lock();
            boolean loaded;

            try {
                loaded = isLoaded(gameVersion);
            } finally {
                lock.readLock().unlock();
            }
            return loaded ? null : refreshAsync(gameVersion, downloadProvider);
        });
    }

    protected Collection<T> getVersionsImpl(String gameVersion) {
        lock.readLock().lock();
        try {
            return versions.get(gameVersion);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the remote versions that specifics Minecraft version.
     *
     * @param gameVersion the Minecraft version that remote versions belong to
     * @return the collection of specific remote versions
     */
    public final Collection<T> getVersions(String gameVersion) {
        return Collections.unmodifiableCollection(getVersionsImpl(gameVersion));
    }

    /**
     * Get the specific remote version.
     *
     * @param gameVersion   the Minecraft version that remote versions belong to
     * @param remoteVersion the version of the remote version.
     * @return the specific remote version, null if it is not found.
     */
    public final Optional<T> getVersion(String gameVersion, String remoteVersion) {
        lock.readLock().lock();
        try {
            T result = null;
            for (T it : versions.get(gameVersion))
                if (remoteVersion.equals(it.getSelfVersion()))
                    result = it;
            return Optional.ofNullable(result);
        } finally {
            lock.readLock().unlock();
        }
    }
}
