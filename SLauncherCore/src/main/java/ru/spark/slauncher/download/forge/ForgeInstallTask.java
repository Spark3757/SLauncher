package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Spark1337
 */
public final class ForgeInstallTask extends TaskResult<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final ForgeRemoteVersion remote;
    private Path installer;
    private Task dependent;
    private TaskResult<Version> dependency;

    public ForgeInstallTask(DefaultDependencyManager dependencyManager, Version version, ForgeRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.remote = remoteVersion;
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void preExecute() throws Exception {
        installer = Files.createTempFile("forge-installer", ".jar");

        dependent = new FileDownloadTask(NetworkUtils.toURL(remote.getUrl()), installer.toFile())
                .setCaching(true);
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        Files.deleteIfExists(installer);
        setResult(dependency.getResult());
    }

    @Override
    public Collection<Task> getDependents() {
        return Collections.singleton(dependent);
    }

    @Override
    public Collection<Task> getDependencies() {
        return Collections.singleton(dependency);
    }

    @Override
    public void execute() {
        if (VersionNumber.VERSION_COMPARATOR.compare("1.13", remote.getGameVersion()) <= 0)
            dependency = new ForgeNewInstallTask(dependencyManager, version, installer);
        else
            dependency = new ForgeOldInstallTask(dependencyManager, version, installer);
    }
}
