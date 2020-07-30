package ru.spark.slauncher.mod.server;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.GameBuilder;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerModpackRemoteInstallTask extends Task<Void> {

    private final String name;
    private final DefaultDependencyManager dependency;
    private final DefaultGameRepository repository;
    private final List<Task<?>> dependencies = new LinkedList<>();
    private final List<Task<?>> dependents = new LinkedList<>();
    private final ServerModpackManifest manifest;

    public ServerModpackRemoteInstallTask(DefaultDependencyManager dependencyManager, ServerModpackManifest manifest, String name) {
        this.name = name;
        this.dependency = dependencyManager;
        this.repository = dependencyManager.getGameRepository();
        this.manifest = manifest;

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
        dependencies.add(new ServerModpackCompletionTask(dependency, name, new ModpackConfiguration<>(manifest, MODPACK_TYPE, manifest.getName(), manifest.getVersion(), Collections.emptyList())));
    }

    public static final String MODPACK_TYPE = "Server";
}
