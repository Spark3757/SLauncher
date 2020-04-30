package ru.spark.slauncher.mod.server;

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
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerModpackLocalInstallTask extends Task<Void> {

    private final File zipFile;
    private final Modpack modpack;
    private final ServerModpackManifest manifest;
    private final String name;
    private final DefaultGameRepository repository;
    private final List<Task<?>> dependencies = new LinkedList<>();
    private final List<Task<?>> dependents = new LinkedList<>();

    public ServerModpackLocalInstallTask(DefaultDependencyManager dependencyManager, File zipFile, Modpack modpack, ServerModpackManifest manifest, String name) {
        this.zipFile = zipFile;
        this.modpack = modpack;
        this.manifest = manifest;
        this.name = name;
        this.repository = dependencyManager.getGameRepository();
        File run = repository.getRunDirectory(name);

        File json = repository.getModpackConfiguration(name);
        if (repository.hasVersion(name) && !json.exists())
            throw new IllegalArgumentException("Version " + name + " already exists.");

        GameBuilder builder = dependencyManager.gameBuilder().name(name);
        for (ServerModpackManifest.Addon addon : manifest.getAddons()) {
            builder.version(addon.getId(), addon.getVersion());
        }

        dependents.add(builder.buildAsync());
        onDone().register(event -> {
            if (event.isFailed())
                repository.removeVersionFromDisk(name);
        });

        ModpackConfiguration<ServerModpackManifest> config = null;
        try {
            if (json.exists()) {
                config = JsonUtils.GSON.fromJson(FileUtils.readText(json), new TypeToken<ModpackConfiguration<ServerModpackManifest>>() {
                }.getType());

                if (!MODPACK_TYPE.equals(config.getType()))
                    throw new IllegalArgumentException("Version " + name + " is not a Server modpack. Cannot update this version.");
            }
        } catch (JsonParseException | IOException ignore) {
        }
        dependents.add(new ModpackInstallTask<>(zipFile, run, modpack.getEncoding(), "/overrides", any -> true, config).withStage("slauncher.modpack"));
        dependents.add(new MinecraftInstanceTask<>(zipFile, modpack.getEncoding(), "/overrides", manifest, MODPACK_TYPE, repository.getModpackConfiguration(name)).withStage("slauncher.modpack"));
    }

    @Override
    public List<Task<?>> getDependents() {
        return dependents;
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() throws Exception {
    }

    @Override
    public List<String> getStages() {
        return Stream.concat(
                dependents.stream().flatMap(task -> task.getStages().stream()),
                Stream.of("slauncher.modpack")
        ).collect(Collectors.toList());
    }

    public static final String MODPACK_TYPE = "Server";
}
