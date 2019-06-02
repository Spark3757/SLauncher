package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.AbstractDependencyManager;
import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This task is to download game libraries.
 * This task should be executed last(especially after game downloading, Forge, LiteLoader and OptiFine install task).
 *
 * @author Spark1337
 */
public final class GameLibrariesTask extends Task {

    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final List<Library> libraries;
    private final List<Task> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the <b>resolved</b> version
     */
    public GameLibrariesTask(AbstractDependencyManager dependencyManager, Version version) {
        this(dependencyManager, version, version.getLibraries());
    }

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link GameRepository}
     * @param version           the <b>resolved</b> version
     */
    public GameLibrariesTask(AbstractDependencyManager dependencyManager, Version version, List<Library> libraries) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.libraries = libraries;

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        libraries.stream().filter(Library::appliesToCurrentEnvironment).forEach(library -> {
            File file = dependencyManager.getGameRepository().getLibraryFile(version, library);
            if (!file.exists())
                dependencies.add(new LibraryDownloadTask(dependencyManager, file, library));
            else
                dependencyManager.getCacheRepository().tryCacheLibrary(library, file.toPath());
        });
    }

}
