package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.DependencyManager;
import ru.spark.slauncher.download.game.VersionJsonSaveTask;
import ru.spark.slauncher.mod.MinecraftInstanceTask;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.mod.ModpackInstallTask;
import ru.spark.slauncher.setting.Profile;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class SLauncherModpackInstallTask extends Task {
    public static final String MODPACK_TYPE = "SLauncher";
    private final File zipFile;
    private final String name;
    private final SLauncherGameRepository repository;
    private final Modpack modpack;
    private final List<Task> dependencies = new LinkedList<>();
    private final List<Task> dependents = new LinkedList<>();

    public SLauncherModpackInstallTask(Profile profile, File zipFile, Modpack modpack, String name) {
        DependencyManager dependency = profile.getDependency();
        repository = profile.getRepository();
        this.zipFile = zipFile;
        this.name = name;
        this.modpack = modpack;

        File run = repository.getRunDirectory(name);
        File json = repository.getModpackConfiguration(name);
        if (repository.hasVersion(name) && !json.exists())
            throw new IllegalArgumentException("Version " + name + " already exists");

        dependents.add(dependency.gameBuilder().name(name).gameVersion(modpack.getGameVersion()).buildAsync());

        onDone().register(event -> {
            if (event.isFailed()) repository.removeVersionFromDisk(name);
        });

        ModpackConfiguration<Modpack> config = null;
        try {
            if (json.exists()) {
                config = JsonUtils.GSON.fromJson(FileUtils.readText(json), new TypeToken<ModpackConfiguration<Modpack>>() {
                }.getType());

                if (!MODPACK_TYPE.equals(config.getType()))
                    throw new IllegalArgumentException("Version " + name + " is not a SLauncher modpack. Cannot update this version.");
            }
        } catch (JsonParseException | IOException ignore) {
        }
        dependents.add(new ModpackInstallTask<>(zipFile, run, modpack.getEncoding(), "/minecraft", it -> !"pack.json".equals(it), config));
    }

    @Override
    public List<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Task> getDependents() {
        return dependents;
    }

    @Override
    public void execute() throws Exception {
        String json = CompressingUtils.readTextZipEntry(zipFile, "minecraft/pack.json");
        Version version = JsonUtils.GSON.fromJson(json, Version.class).setId(name).setJar(null);
        dependencies.add(new VersionJsonSaveTask(repository, version));
        dependencies.add(new MinecraftInstanceTask<>(zipFile, modpack.getEncoding(), "/minecraft", modpack, MODPACK_TYPE, repository.getModpackConfiguration(name)));
    }
}
