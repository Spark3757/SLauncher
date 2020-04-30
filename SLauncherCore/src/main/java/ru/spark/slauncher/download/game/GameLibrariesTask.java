package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * This task is to download game libraries.
 * This task should be executed last(especially after game downloading, Forge, LiteLoader and OptiFine install task).
 *
 * @author spark1337
 */
public final class GameLibrariesTask extends Task<Void> {

    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final boolean integrityCheck;
    private final List<Library> libraries;
    private final List<Task<?>> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the game version
     */
    public GameLibrariesTask(AbstractDependencyManager dependencyManager, Version version, boolean integrityCheck) {
        this(dependencyManager, version, integrityCheck, version.resolve(dependencyManager.getGameRepository()).getLibraries());
    }

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the game version
     */
    public GameLibrariesTask(AbstractDependencyManager dependencyManager, Version version, boolean integrityCheck, List<Library> libraries) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.integrityCheck = integrityCheck;
        this.libraries = libraries;

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    public static boolean shouldDownloadLibrary(GameRepository gameRepository, Version version, Library library, boolean integrityCheck) {
        File file = gameRepository.getLibraryFile(version, library);
        Path jar = file.toPath();
        if (!file.isFile()) return true;
        try {
            if (integrityCheck && !library.getDownload().validateChecksum(jar, true)) return true;
            if (integrityCheck &&
                    library.getChecksums() != null && !library.getChecksums().isEmpty() &&
                    !LibraryDownloadTask.checksumValid(file, library.getChecksums())) return true;
            if (integrityCheck) {
                String ext = FileUtils.getExtension(file);
                if (ext.equals("jar")) {
                    try {
                        FileDownloadTask.ZIP_INTEGRITY_CHECK_HANDLER.checkIntegrity(jar, jar);
                    } catch (IOException ignored) {
                        // the Jar file is malformed, so re-download it.
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Unable to calc hash value of file " + jar, e);
        }

        return false;
    }

    @Override
    public void execute() {
        libraries.stream().filter(Library::appliesToCurrentEnvironment).forEach(library -> {
            File file = dependencyManager.getGameRepository().getLibraryFile(version, library);
            if (shouldDownloadLibrary(dependencyManager.getGameRepository(), version, library, integrityCheck)) {
                dependencies.add(new LibraryDownloadTask(dependencyManager, file, library));
            } else {
                dependencyManager.getCacheRepository().tryCacheLibrary(library, file.toPath());
            }
        });
    }

}
