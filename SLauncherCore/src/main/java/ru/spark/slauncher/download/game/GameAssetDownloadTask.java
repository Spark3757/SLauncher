package ru.spark.slauncher.download.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.*;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author spark1337
 */
public final class GameAssetDownloadTask extends Task<Void> {

    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final AssetIndexInfo assetIndexInfo;
    private final File assetIndexFile;
    private final boolean integrityCheck;
    private final List<Task<?>> dependents = new LinkedList<>();
    private final List<Task<?>> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the game version
     */
    public GameAssetDownloadTask(AbstractDependencyManager dependencyManager, Version version, boolean forceDownloadingIndex, boolean integrityCheck) {
        this.dependencyManager = dependencyManager;
        this.version = version.resolve(dependencyManager.getGameRepository());
        this.assetIndexInfo = this.version.getAssetIndex();
        this.assetIndexFile = dependencyManager.getGameRepository().getIndexFile(version.getId(), assetIndexInfo.getId());
        this.integrityCheck = integrityCheck;

        dependents.add(new GameAssetIndexDownloadTask(dependencyManager, this.version, forceDownloadingIndex));
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return dependents;
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() throws Exception {
        AssetIndex index;
        try {
            index = JsonUtils.fromNonNullJson(FileUtils.readText(assetIndexFile), AssetIndex.class);
        } catch (IOException | JsonParseException e) {
            throw new GameAssetIndexDownloadTask.GameAssetIndexMalformedException();
        }

        int progress = 0;
        for (AssetObject assetObject : index.getObjects().values()) {
            if (isCancelled())
                throw new InterruptedException();

            File file = dependencyManager.getGameRepository().getAssetObject(version.getId(), assetIndexInfo.getId(), assetObject);
            boolean download = !file.isFile();
            try {
                if (!download && integrityCheck && !assetObject.validateChecksum(file.toPath(), true))
                    download = true;
            } catch (IOException e) {
                Logging.LOG.log(Level.WARNING, "Unable to calc hash value of file " + file.toPath(), e);
            }
            if (download) {
                List<URL> urls = dependencyManager.getDownloadProvider().getAssetObjectCandidates(assetObject.getLocation());

                FileDownloadTask task = new FileDownloadTask(urls, file, new FileDownloadTask.IntegrityCheck("SHA-1", assetObject.getHash()));
                task.setName(assetObject.getHash());
                task.setCandidate(dependencyManager.getCacheRepository().getCommonDirectory()
                        .resolve("assets").resolve("objects").resolve(assetObject.getLocation()));
                task.setCacheRepository(dependencyManager.getCacheRepository());
                task.setCaching(true);
                dependencies.add(task.withCounter());
            } else {
                dependencyManager.getCacheRepository().tryCacheFile(file.toPath(), CacheRepository.SHA1, assetObject.getHash());
            }

            updateProgress(++progress, index.getObjects().size());
        }

        if (!dependencies.isEmpty()) {
            getProperties().put("total", dependencies.size());
        }
    }

    public static final boolean DOWNLOAD_INDEX_FORCIBLY = true;
    public static final boolean DOWNLOAD_INDEX_IF_NECESSARY = false;
}
