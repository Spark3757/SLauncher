package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;

import java.util.List;

public class ForgeRemoteVersion extends RemoteVersion {
    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param url         the installer or universal jar original URL.
     */
    public ForgeRemoteVersion(String gameVersion, String selfVersion, List<String> url) {
        super(LibraryAnalyzer.LibraryType.FORGE.getPatchId(), gameVersion, selfVersion, url);
    }

    @Override
    public Task<Version> getInstallTask(DefaultDependencyManager dependencyManager, Version baseVersion) {
        return new ForgeInstallTask(dependencyManager, baseVersion, this);
    }
}
