package ru.spark.slauncher.download.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.AssetIndex;
import ru.spark.slauncher.game.AssetIndexInfo;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.DigestUtils;
import ru.spark.slauncher.util.Hex;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import static ru.spark.slauncher.util.Logging.LOG;

/**
 * This task is to download asset index file provided in minecraft.json.
 *
 * @author spark1337
 */
public final class GameAssetIndexDownloadTask extends Task<Void> {

    private final AbstractDependencyManager dependencyManager;
    private final boolean forceDownloading;
    private final Version version;
    private final List<Task<?>> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the <b>resolved</b> version
     */
    public GameAssetIndexDownloadTask(AbstractDependencyManager dependencyManager, Version version, boolean forceDownloading) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.forceDownloading = forceDownloading;
        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        AssetIndexInfo assetIndexInfo = version.getAssetIndex();
        File assetIndexFile = dependencyManager.getGameRepository().getIndexFile(version.getId(), assetIndexInfo.getId());
        boolean verifyHashCode = StringUtils.isNotBlank(assetIndexInfo.getSha1()) && assetIndexInfo.getUrl().contains(assetIndexInfo.getSha1());

        if (assetIndexFile.exists() && !forceDownloading) {
            // verify correctness of file content
            if (verifyHashCode) {
                try {
                    String actualSum = Hex.encodeHex(DigestUtils.digest("SHA-1", assetIndexFile.toPath()));
                    if (actualSum.equalsIgnoreCase(assetIndexInfo.getSha1()))
                        return;
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to calculate sha1sum of file " + assetIndexInfo, e);
                    // continue downloading
                }
            } else {
                try {
                    JsonUtils.fromNonNullJson(FileUtils.readText(assetIndexFile), AssetIndex.class);
                    return;
                } catch (IOException | JsonParseException ignore) {
                }
            }
        }

        // We should not check the hash code of asset index file since this file is not consistent
        // And Mojang will modify this file anytime. So assetIndex.hash might be outdated.
        FileDownloadTask task = new FileDownloadTask(
                dependencyManager.getDownloadProvider().injectURLWithCandidates(assetIndexInfo.getUrl()),
                assetIndexFile,
                verifyHashCode ? new FileDownloadTask.IntegrityCheck("SHA-1", assetIndexInfo.getSha1()) : null
        );
        task.setCacheRepository(dependencyManager.getCacheRepository());
        dependencies.add(task);
    }


    public static class GameAssetIndexMalformedException extends IOException {
    }
}
