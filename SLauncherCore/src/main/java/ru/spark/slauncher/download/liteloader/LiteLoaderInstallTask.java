package ru.spark.slauncher.download.liteloader;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.LibrariesDownloadInfo;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.LibraryDownloadInfo;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.Lang;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Note: LiteLoader must be installed after Forge.
 *
 * @author Spark1337
 */
public final class LiteLoaderInstallTask extends TaskResult<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final LiteLoaderRemoteVersion remote;
    private final List<Task> dependents = new LinkedList<>();
    private final List<Task> dependencies = new LinkedList<>();

    public LiteLoaderInstallTask(DefaultDependencyManager dependencyManager, Version version, LiteLoaderRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.remote = remoteVersion;
    }

    @Override
    public Collection<Task> getDependents() {
        return dependents;
    }

    @Override
    public Collection<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        Library library = new Library(
                "com.mumfrey", "liteloader", remote.getSelfVersion(), null,
                "http://dl.liteloader.com/versions/",
                new LibrariesDownloadInfo(new LibraryDownloadInfo(null, remote.getUrl()))
        );

        Version tempVersion = version.setLibraries(Lang.merge(remote.getLibraries(), Collections.singleton(library)));

        // --tweakClass will be added in MaintainTask
        setResult(version
                .setMainClass("net.minecraft.launchwrapper.Launch")
                .setLibraries(Lang.merge(tempVersion.getLibraries(), version.getLibraries()))
                .setLogging(Collections.emptyMap()) // Mods may log in malformed format, causing XML parser to crash. So we suppress using official log4j configuration
        );

        dependencies.add(dependencyManager.checkLibraryCompletionAsync(tempVersion));
    }

}
