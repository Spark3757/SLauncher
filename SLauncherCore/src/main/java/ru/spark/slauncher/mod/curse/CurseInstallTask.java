package ru.spark.slauncher.mod.curse;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.GameBuilder;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.mod.MinecraftInstanceTask;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.mod.ModpackInstallTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Install a downloaded CurseForge modpack.
 *
 * @author spark1337
 */
public final class CurseInstallTask extends Task<Void> {

    private final DefaultDependencyManager dependencyManager;
    private final DefaultGameRepository repository;
    private final File zipFile;
    private final Modpack modpack;
    private final CurseManifest manifest;
    private final String name;
    private final File run;
    private final ModpackConfiguration<CurseManifest> config;
    private final List<Task<?>> dependents = new LinkedList<>();
    private final List<Task<?>> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager.
     * @param zipFile           the CurseForge modpack file.
     * @param manifest          The manifest content of given CurseForge modpack.
     * @param name              the new version name
     * @see CurseManifest#readCurseForgeModpackManifest
     */
    public CurseInstallTask(DefaultDependencyManager dependencyManager, File zipFile, Modpack modpack, CurseManifest manifest, String name) {
        this.dependencyManager = dependencyManager;
        this.zipFile = zipFile;
        this.modpack = modpack;
        this.manifest = manifest;
        this.name = name;
        this.repository = dependencyManager.getGameRepository();
        this.run = repository.getRunDirectory(name);

        File json = repository.getModpackConfiguration(name);
        if (repository.hasVersion(name) && !json.exists())
            throw new IllegalArgumentException("Version " + name + " already exists.");

        GameBuilder builder = dependencyManager.gameBuilder().name(name).gameVersion(manifest.getMinecraft().getGameVersion());
        for (CurseManifestModLoader modLoader : manifest.getMinecraft().getModLoaders())
            if (modLoader.getId().startsWith("forge-"))
                builder.version("forge", modLoader.getId().substring("forge-".length()));
        dependents.add(builder.buildAsync());

        onDone().register(event -> {
            Exception ex = event.getTask().getException();
            if (event.isFailed()) {
                if (!(ex instanceof CurseCompletionException)) {
                    repository.removeVersionFromDisk(name);
                }
            }
        });

        ModpackConfiguration<CurseManifest> config = null;
        try {
            if (json.exists()) {
                config = JsonUtils.GSON.fromJson(FileUtils.readText(json), new TypeToken<ModpackConfiguration<CurseManifest>>() {
                }.getType());

                if (!MODPACK_TYPE.equals(config.getType()))
                    throw new IllegalArgumentException("Version " + name + " is not a Curse modpack. Cannot update this version.");
            }
        } catch (JsonParseException | IOException ignore) {
        }
        this.config = config;
        dependents.add(new ModpackInstallTask<>(zipFile, run, modpack.getEncoding(), manifest.getOverrides(), any -> true, config).withStage("slauncher.modpack"));
        dependents.add(new MinecraftInstanceTask<>(zipFile, modpack.getEncoding(), manifest.getOverrides(), manifest, MODPACK_TYPE, repository.getModpackConfiguration(name)).withStage("slauncher.modpack"));

        dependencies.add(new CurseCompletionTask(dependencyManager, name, manifest).withStage("slauncher.modpack.download"));
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
        if (config != null) {
            // For update, remove mods not listed in new manifest
            for (CurseManifestFile oldCurseManifestFile : config.getManifest().getFiles()) {
                if (StringUtils.isBlank(oldCurseManifestFile.getFileName())) continue;
                File oldFile = new File(run, "mods/" + oldCurseManifestFile.getFileName());
                if (!oldFile.exists()) continue;
                if (manifest.getFiles().stream().noneMatch(oldCurseManifestFile::equals))
                    if (!oldFile.delete())
                        throw new IOException("Unable to delete mod file " + oldFile);
            }
        }

        File root = repository.getVersionRoot(name);
        FileUtils.writeText(new File(root, "manifest.json"), JsonUtils.GSON.toJson(manifest));
    }

    @Override
    public List<String> getStages() {
        return Stream.concat(
                dependents.stream().flatMap(task -> task.getStages().stream()),
                Stream.of("slauncher.modpack", "slauncher.modpack.download")
        ).collect(Collectors.toList());
    }

    public static final String MODPACK_TYPE = "Curse";
}
