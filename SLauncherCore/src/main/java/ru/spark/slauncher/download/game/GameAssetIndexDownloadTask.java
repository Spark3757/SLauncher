package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.AssetIndexInfo;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This task is to download asset index file provided in minecraft.json.
 *
 * @author Spark1337
 */
public final class GameAssetIndexDownloadTask extends Task {

    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final List<Task> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the <b>resolved</b> version
     */
    public GameAssetIndexDownloadTask(AbstractDependencyManager dependencyManager, Version version) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        AssetIndexInfo assetIndexInfo = version.getAssetIndex();
        File assetIndexFile = dependencyManager.getGameRepository().getIndexFile(version.getId(), assetIndexInfo.getId());

        // We should not check the hash code of asset index file since this file is not consistent
        // And Mojang will modify this file anytime. So assetIndex.hash might be outdated.
        dependencies.add(new FileDownloadTask(
                NetworkUtils.toURL(dependencyManager.getDownloadProvider().injectURL(assetIndexInfo.getUrl())),
                assetIndexFile
        ).setCacheRepository(dependencyManager.getCacheRepository()));
    }

}
