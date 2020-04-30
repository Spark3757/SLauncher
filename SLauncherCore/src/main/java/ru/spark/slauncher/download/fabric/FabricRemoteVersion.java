package ru.spark.slauncher.download.fabric;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;

import java.util.List;

public class FabricRemoteVersion extends RemoteVersion {
    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param urls        the installer or universal jar original URL.
     */
    FabricRemoteVersion(String gameVersion, String selfVersion, List<String> urls) {
        super(LibraryAnalyzer.LibraryType.FABRIC.getPatchId(), gameVersion, selfVersion, urls);
    }

    @Override
    public Task<Version> getInstallTask(DefaultDependencyManager dependencyManager, Version baseVersion) {
        return new FabricInstallTask(dependencyManager, baseVersion, this);
    }
}
