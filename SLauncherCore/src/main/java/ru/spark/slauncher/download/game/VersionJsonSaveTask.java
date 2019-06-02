package ru.spark.slauncher.download.game;

import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;

/**
 * This task is to save the version json.
 *
 * @author Spark1337
 */
public final class VersionJsonSaveTask extends TaskResult<Version> {

    private final DefaultGameRepository repository;
    private final Version version;

    /**
     * Constructor.
     *
     * @param repository the game repository
     * @param version    the game version
     */
    public VersionJsonSaveTask(DefaultGameRepository repository, Version version) {
        this.repository = repository;
        this.version = version;

        setSignificance(TaskSignificance.MODERATE);
        setResult(version);
    }

    @Override
    public void execute() throws Exception {
        File json = repository.getVersionJson(version.getId()).getAbsoluteFile();
        FileUtils.writeText(json, JsonUtils.GSON.toJson(version));
    }
}
