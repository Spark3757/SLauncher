package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Spark1337
 */
public final class GameDownloadTask extends Task {
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final List<Task> dependencies = new LinkedList<>();

    public GameDownloadTask(DefaultDependencyManager dependencyManager, String gameVersion, Version version) {
        this.dependencyManager = dependencyManager;
        this.gameVersion = gameVersion;
        this.version = version;

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        File jar = dependencyManager.getGameRepository().getVersionJar(version);

        FileDownloadTask task = new FileDownloadTask(
                NetworkUtils.toURL(dependencyManager.getDownloadProvider().injectURL(version.getDownloadInfo().getUrl())),
                jar,
                FileDownloadTask.IntegrityCheck.of(CacheRepository.SHA1, version.getDownloadInfo().getSha1()))
                .setCaching(true)
                .setCacheRepository(dependencyManager.getCacheRepository());

        if (gameVersion != null)
            task.setCandidate(dependencyManager.getCacheRepository().getCommonDirectory().resolve("jars").resolve(gameVersion + ".jar"));

        dependencies.add(task);
    }

}
