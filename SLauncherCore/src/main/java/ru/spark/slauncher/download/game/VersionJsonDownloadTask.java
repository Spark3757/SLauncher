package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.download.VersionList;
import ru.spark.slauncher.task.GetTask;
import ru.spark.slauncher.task.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author spark1337
 */
public final class VersionJsonDownloadTask extends Task<String> {
    private final String gameVersion;
    private final DefaultDependencyManager dependencyManager;
    private final List<Task<?>> dependents = new LinkedList<>();
    private final List<Task<?>> dependencies = new LinkedList<>();
    private final VersionList<?> gameVersionList;

    public VersionJsonDownloadTask(String gameVersion, DefaultDependencyManager dependencyManager) {
        this.gameVersion = gameVersion;
        this.dependencyManager = dependencyManager;
        this.gameVersionList = dependencyManager.getVersionList("game");

        dependents.add(gameVersionList.loadAsync());

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return dependents;
    }

    @Override
    public void execute() throws IOException {
        RemoteVersion remoteVersion = gameVersionList.getVersion(gameVersion, gameVersion)
                .orElseThrow(() -> new IOException("Cannot find specific version " + gameVersion + " in remote repository"));
        dependencies.add(new GetTask(dependencyManager.getDownloadProvider().injectURLsWithCandidates(remoteVersion.getUrls())).storeTo(this::setResult));
    }
}
