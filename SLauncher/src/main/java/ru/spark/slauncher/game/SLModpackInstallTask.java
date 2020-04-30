package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SLModpackInstallTask extends Task<Void> {
    public static final String MODPACK_TYPE = "SLauncher";
    private final File zipFile;
    private final String name;
    private final SLGameRepository repository;
    private final DefaultDependencyManager dependency;
    private final Modpack modpack;
    private final List<Task<?>> dependencies = new LinkedList<>();
    private final List<Task<?>> dependents = new LinkedList<>();

    public SLModpackInstallTask(Profile profile, File zipFile, Modpack modpack, String name) {
        dependency = profile.getDependency();
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
        dependents.add(new MinecraftInstanceTask<>(zipFile, modpack.getEncoding(), "/minecraft", modpack, MODPACK_TYPE, repository.getModpackConfiguration(name)).withStage("slauncher.modpack"));
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public List<Task<?>> getDependents() {
        return dependents;
    }

    @Override
    public void execute() throws Exception {
        String json = CompressingUtils.readTextZipEntry(zipFile, "minecraft/pack.json");
        Version originalVersion = JsonUtils.GSON.fromJson(json, Version.class).setId(name).setJar(null);
        LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(originalVersion);
        Task<Version> libraryTask = Task.supplyAsync(() -> originalVersion);
        // reinstall libraries
        // libraries of Forge and OptiFine should be obtained by installation.
        for (LibraryAnalyzer.LibraryMark mark : analyzer) {
            if (LibraryAnalyzer.LibraryType.MINECRAFT.getPatchId().equals(mark.getLibraryId()))
                continue;
            libraryTask = libraryTask.thenComposeAsync(version -> dependency.installLibraryAsync(modpack.getGameVersion(), version, mark.getLibraryId(), mark.getLibraryVersion()));
        }

        dependencies.add(libraryTask.thenComposeAsync(repository::saveAsync));
    }

    @Override
    public List<String> getStages() {
        return Stream.concat(
                dependents.stream().flatMap(task -> task.getStages().stream()),
                Stream.of("slauncher.modpack")
        ).collect(Collectors.toList());
    }
}
