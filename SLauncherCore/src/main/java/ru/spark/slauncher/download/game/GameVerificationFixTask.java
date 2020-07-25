package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Remove class digital verification file in game jar
 * @author spark1337
 */
public final class GameVerificationFixTask extends Task<Void> {
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final List<Task<?>> dependencies = new LinkedList<>();

    public GameVerificationFixTask(DefaultDependencyManager dependencyManager, String gameVersion, Version version) {
        this.dependencyManager = dependencyManager;
        this.gameVersion = gameVersion;
        this.version = version;

        if (!version.isResolved()) {
            throw new IllegalArgumentException("GameVerificationFixTask requires a resolved game version");
        }

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() throws IOException {
        File jar = dependencyManager.getGameRepository().getVersionJar(version);
        LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(version);

        if (jar.exists() && VersionNumber.VERSION_COMPARATOR.compare(gameVersion, "1.6") < 0 && analyzer.has(LibraryAnalyzer.LibraryType.FORGE)) {
            try (FileSystem fs = CompressingUtils.createWritableZipFileSystem(jar.toPath(), StandardCharsets.UTF_8)) {
                Files.deleteIfExists(fs.getPath("META-INF/MOJANG_C.DSA"));
                Files.deleteIfExists(fs.getPath("META-INF/MOJANG_C.SF"));
            }
        }
    }

}
