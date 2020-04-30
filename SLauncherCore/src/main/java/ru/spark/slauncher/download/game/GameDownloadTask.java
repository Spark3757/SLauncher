package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.FileDownloadTask.IntegrityCheck;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.CacheRepository;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Task to download Minecraft jar
 *
 * @author spark1337
 */
public final class GameDownloadTask extends Task<Void> {
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final List<Task<?>> dependencies = new LinkedList<>();

    public GameDownloadTask(DefaultDependencyManager dependencyManager, String gameVersion, Version version) {
        this.dependencyManager = dependencyManager;
        this.gameVersion = gameVersion;
        this.version = version.resolve(dependencyManager.getGameRepository());

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        File jar = dependencyManager.getGameRepository().getVersionJar(version);

        FileDownloadTask task = new FileDownloadTask(
                dependencyManager.getDownloadProvider().injectURLWithCandidates(version.getDownloadInfo().getUrl()),
                jar,
                IntegrityCheck.of(CacheRepository.SHA1, version.getDownloadInfo().getSha1()));
        task.setCaching(true);
        task.setCacheRepository(dependencyManager.getCacheRepository());

        if (gameVersion != null)
            task.setCandidate(dependencyManager.getCacheRepository().getCommonDirectory().resolve("jars").resolve(gameVersion + ".jar"));

        dependencies.add(task);
    }

}
