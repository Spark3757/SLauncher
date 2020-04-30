package ru.spark.slauncher.download;

import ru.spark.slauncher.download.forge.ForgeInstallTask;
import ru.spark.slauncher.download.game.GameAssetDownloadTask;
import ru.spark.slauncher.download.game.GameDownloadTask;
import ru.spark.slauncher.download.game.GameLibrariesTask;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.spark.slauncher.download.LibraryAnalyzer.LibraryType.OPTIFINE;

/**
 * Note: This class has no state.
 *
 * @author spark1337
 */
public class DefaultDependencyManager extends AbstractDependencyManager {

    private final DefaultGameRepository repository;
    private final DownloadProvider downloadProvider;
    private final DefaultCacheRepository cacheRepository;

    public DefaultDependencyManager(DefaultGameRepository repository, DownloadProvider downloadProvider, DefaultCacheRepository cacheRepository) {
        this.repository = repository;
        this.downloadProvider = downloadProvider;
        this.cacheRepository = cacheRepository;
    }

    @Override
    public DefaultGameRepository getGameRepository() {
        return repository;
    }

    @Override
    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    @Override
    public DefaultCacheRepository getCacheRepository() {
        return cacheRepository;
    }

    @Override
    public GameBuilder gameBuilder() {
        return new DefaultGameBuilder(this);
    }

    @Override
    public Task<?> checkGameCompletionAsync(Version original, boolean integrityCheck) {
        Version version = original.resolve(repository);
        return Task.allOf(
                Task.composeAsync(() -> {
                    if (!repository.getVersionJar(version).exists())
                        return new GameDownloadTask(this, null, version);
                    else
                        return null;
                }),
                new GameAssetDownloadTask(this, version, GameAssetDownloadTask.DOWNLOAD_INDEX_IF_NECESSARY, integrityCheck),
                new GameLibrariesTask(this, version, integrityCheck)
        );
    }

    @Override
    public Task<?> checkLibraryCompletionAsync(Version version, boolean integrityCheck) {
        return new GameLibrariesTask(this, version, integrityCheck, version.getLibraries());
    }

    @Override
    public Task<?> checkPatchCompletionAsync(Version version, boolean integrityCheck) {
        return Task.composeAsync(() -> {
            List<Task<?>> tasks = new ArrayList<>();

            Optional<String> gameVersion = GameVersion.minecraftVersion(repository.getVersionJar(version));
            if (!gameVersion.isPresent()) return null;

            LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(version.resolvePreservingPatches(getGameRepository()));
            version.resolve(getGameRepository()).getLibraries().stream().filter(Library::appliesToCurrentEnvironment).forEach(library -> {
                Optional<String> libraryVersion = analyzer.getVersion(OPTIFINE);
                if (OPTIFINE.matchLibrary(library) && libraryVersion.isPresent()) {
                    if (GameLibrariesTask.shouldDownloadLibrary(repository, version, library, integrityCheck)) {
                        tasks.add(installLibraryAsync(gameVersion.get(), version, OPTIFINE.getPatchId(), libraryVersion.get()));
                    }
                }
            });

            return Task.allOf(tasks);
        });
    }

    @Override
    public Task<Version> installLibraryAsync(String gameVersion, Version baseVersion, String libraryId, String libraryVersion) {
        if (baseVersion.isResolved()) throw new IllegalArgumentException("Version should not be resolved");

        VersionList<?> versionList = getVersionList(libraryId);
        return versionList.loadAsync(gameVersion)
                .thenComposeAsync(() -> installLibraryAsync(baseVersion, versionList.getVersion(gameVersion, libraryVersion)
                        .orElseThrow(() -> new IOException("Remote library " + libraryId + " has no version " + libraryVersion))))
                .withStage(String.format("slauncher.install.%s:%s", libraryId, libraryVersion));
    }

    @Override
    public Task<Version> installLibraryAsync(Version baseVersion, RemoteVersion libraryVersion) {
        if (baseVersion.isResolved()) throw new IllegalArgumentException("Version should not be resolved");

        return removeLibraryAsync(baseVersion.resolvePreservingPatches(repository), libraryVersion.getLibraryId())
                .thenComposeAsync(version -> libraryVersion.getInstallTask(this, version))
                .thenApplyAsync(baseVersion::addPatch)
                .withStage(String.format("slauncher.install.%s:%s", libraryVersion.getLibraryId(), libraryVersion.getSelfVersion()));
    }

    public Task<Version> installLibraryAsync(Version oldVersion, Path installer) {
        if (oldVersion.isResolved()) throw new IllegalArgumentException("Version should not be resolved");

        return Task
                .composeAsync(() -> {
                    try {
                        return ForgeInstallTask.install(this, oldVersion, installer);
                    } catch (IOException ignore) {
                    }

                    try {
                        return OptiFineInstallTask.install(this, oldVersion, installer);
                    } catch (IOException ignore) {
                    }

                    throw new UnsupportedLibraryInstallerException();
                })
                .thenApplyAsync(oldVersion::addPatch);
    }

    public static class UnsupportedLibraryInstallerException extends Exception {
    }

    /**
     * Remove installed library.
     * Will try to remove libraries and patches.
     *
     * @param version   not resolved version
     * @param libraryId forge/liteloader/optifine/fabric
     * @return task to remove the specified library
     */
    public Task<Version> removeLibraryAsync(Version version, String libraryId) {
        // MaintainTask requires version that does not inherits from any version.
        // If we want to remove a library in dependent version, we should keep the dependents not changed
        // So resolving this game version to preserve all information in this version.json is necessary.
        if (version.isResolved())
            throw new IllegalArgumentException("removeLibraryWithoutSavingAsync requires non-resolved version");
        Version independentVersion = version.resolvePreservingPatches(repository);

        return Task.supplyAsync(() -> LibraryAnalyzer.analyze(independentVersion).removeLibrary(libraryId).build());
    }

}
