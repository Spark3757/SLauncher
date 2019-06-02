package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Spark1337
 */
public final class VersionJsonDownloadTask extends TaskResult<String> {
    private final String gameVersion;
    private final DefaultDependencyManager dependencyManager;
    private final List<Task> dependents = new LinkedList<>();
    private final List<Task> dependencies = new LinkedList<>();
    private final VersionList<?> gameVersionList;

    public VersionJsonDownloadTask(String gameVersion, DefaultDependencyManager dependencyManager) {
        this.gameVersion = gameVersion;
        this.dependencyManager = dependencyManager;
        this.gameVersionList = dependencyManager.getVersionList("game");

        if (!gameVersionList.isLoaded())
            dependents.add(gameVersionList.refreshAsync(dependencyManager.getDownloadProvider()));

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public Collection<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public Collection<Task> getDependents() {
        return dependents;
    }

    @Override
    public void execute() {
        RemoteVersion remoteVersion = gameVersionList.getVersions(gameVersion).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find specific version " + gameVersion + " in remote repository"));
        String jsonURL = dependencyManager.getDownloadProvider().injectURL(remoteVersion.getUrl());
        dependencies.add(new GetTask(NetworkUtils.toURL(jsonURL)).storeTo(this::setResult));
    }
}
