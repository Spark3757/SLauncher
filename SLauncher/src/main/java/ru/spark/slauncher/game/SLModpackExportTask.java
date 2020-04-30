package ru.spark.slauncher.game;

import ru.spark.slauncher.mod.ModAdviser;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.Zipper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Export the game to a mod pack file.
 */
public class SLModpackExportTask extends Task<Void> {
    private final DefaultGameRepository repository;
    private final String version;
    private final List<String> whitelist;
    private final Modpack modpack;
    private final File output;

    /**
     * @param output  mod pack file.
     * @param version to locate version.json
     */
    public SLModpackExportTask(DefaultGameRepository repository, String version, List<String> whitelist, Modpack modpack, File output) {
        this.repository = repository;
        this.version = version;
        this.whitelist = whitelist;
        this.modpack = modpack;
        this.output = output;

        onDone().register(event -> {
            if (event.isFailed()) output.delete();
        });
    }

    @Override
    public void execute() throws Exception {
        ArrayList<String> blackList = new ArrayList<>(ModAdviser.MODPACK_BLACK_LIST);
        blackList.add(version + ".jar");
        blackList.add(version + ".json");
        Logging.LOG.info("Compressing game files without some files in blacklist, including files or directories: usernamecache.json, asm, logs, backups, versions, assets, usercache.json, libraries, crash-reports, launcher_profiles.json, NVIDIA, TCNodeTracker");
        try (Zipper zip = new Zipper(output.toPath())) {
            zip.putDirectory(repository.getRunDirectory(version).toPath(), "minecraft", path -> Modpack.acceptFile(path, blackList, whitelist));

            Version mv = repository.getResolvedPreservingPatchesVersion(version);
            String gameVersion = GameVersion.minecraftVersion(repository.getVersionJar(version))
                    .orElseThrow(() -> new IOException("Cannot parse the version of " + version));
            zip.putTextFile(JsonUtils.GSON.toJson(mv.setJar(gameVersion)), "minecraft/pack.json"); // Making "jar" to gameVersion is to be compatible with old SLauncher.
            zip.putTextFile(JsonUtils.GSON.toJson(modpack.setGameVersion(gameVersion)), "modpack.json"); // Newer SLauncher only reads 'gameVersion' field.
        }
    }
}
