package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.*;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Spark1337
 */
public final class GameAssetDownloadTask extends Task {

    public static final boolean DOWNLOAD_INDEX_FORCIBLY = true;
    public static final boolean DOWNLOAD_INDEX_IF_NECESSARY = false;
    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final AssetIndexInfo assetIndexInfo;
    private final File assetIndexFile;
    private final List<Task> dependents = new LinkedList<>();
    private final List<Task> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the <b>resolved</b> version
     */
    public GameAssetDownloadTask(AbstractDependencyManager dependencyManager, Version version, boolean forceDownloadingIndex) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.assetIndexInfo = version.getAssetIndex();
        this.assetIndexFile = dependencyManager.getGameRepository().getIndexFile(version.getId(), assetIndexInfo.getId());

        if (!assetIndexFile.exists() || forceDownloadingIndex)
            dependents.add(new GameAssetIndexDownloadTask(dependencyManager, version));
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
    public void execute() throws Exception {
        AssetIndex index = JsonUtils.GSON.fromJson(FileUtils.readText(assetIndexFile), AssetIndex.class);
        int progress = 0;
        if (index != null)
            for (AssetObject assetObject : index.getObjects().values()) {
                if (Thread.interrupted())
                    throw new InterruptedException();

                File file = dependencyManager.getGameRepository().getAssetObject(version.getId(), assetIndexInfo.getId(), assetObject);
                if (file.isFile())
                    dependencyManager.getCacheRepository().tryCacheFile(file.toPath(), CacheRepository.SHA1, assetObject.getHash());
                else {
                    String url = dependencyManager.getDownloadProvider().getAssetBaseURL() + assetObject.getLocation();
                    FileDownloadTask task = new FileDownloadTask(NetworkUtils.toURL(url), file, new FileDownloadTask.IntegrityCheck("SHA-1", assetObject.getHash()));
                    task.setName(assetObject.getHash());
                    task.setCaching(true);
                    dependencies.add(task
                            .setCacheRepository(dependencyManager.getCacheRepository())
                            .setCaching(true)
                            .setCandidate(dependencyManager.getCacheRepository().getCommonDirectory()
                                    .resolve("assets").resolve("objects").resolve(assetObject.getLocation())));
                }

                updateProgress(++progress, index.getObjects().size());
            }
    }
}
