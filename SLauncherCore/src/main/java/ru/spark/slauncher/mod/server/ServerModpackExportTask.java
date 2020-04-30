package ru.spark.slauncher.mod.server;

import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.mod.ModAdviser;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.DigestUtils;
import ru.spark.slauncher.util.Hex;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.Zipper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerModpackExportTask extends Task<Void> {
    private final DefaultGameRepository repository;
    private final String versionId;
    private final List<String> whitelist;
    private final File output;
    private final String modpackName;
    private final String modpackAuthor;
    private final String modpackVersion;
    private final String modpackDescription;
    private final String modpackFileApi;

    public ServerModpackExportTask(DefaultGameRepository repository, String versionId, List<String> whitelist, String modpackName, String modpackAuthor, String modpackVersion, String modpackDescription, String modpackFileApi, File output) {
        this.repository = repository;
        this.versionId = versionId;
        this.whitelist = whitelist;
        this.output = output;
        this.modpackName = modpackName;
        this.modpackAuthor = modpackAuthor;
        this.modpackVersion = modpackVersion;
        this.modpackDescription = modpackDescription;
        this.modpackFileApi = modpackFileApi;

        onDone().register(event -> {
            if (event.isFailed()) output.delete();
        });
    }

    @Override
    public void execute() throws Exception {
        ArrayList<String> blackList = new ArrayList<>(ModAdviser.MODPACK_BLACK_LIST);
        blackList.add(versionId + ".jar");
        blackList.add(versionId + ".json");
        Logging.LOG.info("Compressing game files without some files in blacklist, including files or directories: usernamecache.json, asm, logs, backups, versions, assets, usercache.json, libraries, crash-reports, launcher_profiles.json, NVIDIA, TCNodeTracker");
        try (Zipper zip = new Zipper(output.toPath())) {
            Path runDirectory = repository.getRunDirectory(versionId).toPath();
            List<ModpackConfiguration.FileInformation> files = new ArrayList<>();
            zip.putDirectory(runDirectory, "overrides", path -> {
                if (Modpack.acceptFile(path, blackList, whitelist)) {
                    Path file = runDirectory.resolve(path);
                    if (Files.isRegularFile(file)) {
                        String relativePath = runDirectory.relativize(file).normalize().toString().replace(File.separatorChar, '/');
                        files.add(new ModpackConfiguration.FileInformation(relativePath, Hex.encodeHex(DigestUtils.digest("SHA-1", file))));
                    }
                    return true;
                } else {
                    return false;
                }
            });

            LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(repository.getResolvedPreservingPatchesVersion(versionId));
            String gameVersion = GameVersion.minecraftVersion(repository.getVersionJar(versionId))
                    .orElseThrow(() -> new IOException("Cannot parse the version of " + versionId));
            List<ServerModpackManifest.Addon> addons = new ArrayList<>();
            addons.add(new ServerModpackManifest.Addon(LibraryAnalyzer.LibraryType.MINECRAFT.getPatchId(), gameVersion));
            analyzer.getVersion(LibraryAnalyzer.LibraryType.FORGE).ifPresent(forgeVersion ->
                    addons.add(new ServerModpackManifest.Addon(LibraryAnalyzer.LibraryType.FORGE.getPatchId(), forgeVersion)));
            analyzer.getVersion(LibraryAnalyzer.LibraryType.LITELOADER).ifPresent(liteLoaderVersion ->
                    addons.add(new ServerModpackManifest.Addon(LibraryAnalyzer.LibraryType.LITELOADER.getPatchId(), liteLoaderVersion)));
            analyzer.getVersion(LibraryAnalyzer.LibraryType.OPTIFINE).ifPresent(optifineVersion ->
                    addons.add(new ServerModpackManifest.Addon(LibraryAnalyzer.LibraryType.OPTIFINE.getPatchId(), optifineVersion)));
            analyzer.getVersion(LibraryAnalyzer.LibraryType.FABRIC).ifPresent(fabricVersion ->
                    addons.add(new ServerModpackManifest.Addon(LibraryAnalyzer.LibraryType.FABRIC.getPatchId(), fabricVersion)));
            ServerModpackManifest manifest = new ServerModpackManifest(modpackName, modpackAuthor, modpackVersion, modpackDescription, StringUtils.removeSuffix(modpackFileApi, "/"), files, addons);
            zip.putTextFile(JsonUtils.GSON.toJson(manifest), "server-manifest.json");
        }
    }
}
