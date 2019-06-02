package ru.spark.slauncher.download.optifine;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.LibrariesDownloadInfo;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.LibraryDownloadInfo;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.Lang;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <b>Note</b>: OptiFine should be installed in the end.
 *
 * @author Spark1337
 */
public final class OptiFineInstallTask extends TaskResult<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final OptiFineRemoteVersion remote;
    private final List<Task> dependents = new LinkedList<>();
    private final List<Task> dependencies = new LinkedList<>();

    public OptiFineInstallTask(DefaultDependencyManager dependencyManager, Version version, OptiFineRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.remote = remoteVersion;
    }

    @Override
    public Collection<Task> getDependents() {
        return dependents;
    }

    @Override
    public Collection<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isRelyingOnDependencies() {
        return false;
    }

    @Override
    public void execute() {
        if ("cpw.mods.modlauncher.Launcher".equals(version.getMainClass()))
            throw new UnsupportedOptiFineInstallationException();

        String remoteVersion = remote.getGameVersion() + "_" + remote.getSelfVersion();

        Library library = new Library(
                "optifine", "OptiFine", remoteVersion, null, null,
                new LibrariesDownloadInfo(new LibraryDownloadInfo(
                        "optifine/OptiFine/" + remoteVersion + "/OptiFine-" + remoteVersion + ".jar",
                        remote.getUrl()))
        );

        List<Library> libraries = new LinkedList<>();
        libraries.add(library);

        if (version.getMainClass() == null || !version.getMainClass().startsWith("net.minecraft.launchwrapper."))
            libraries.add(0, new Library("net.minecraft", "launchwrapper", "1.12"));

        // --tweakClass will be added in MaintainTask
        setResult(version
                .setLibraries(Lang.merge(version.getLibraries(), libraries))
                .setMainClass("net.minecraft.launchwrapper.Launch")
        );

        dependencies.add(dependencyManager.checkLibraryCompletionAsync(version.setLibraries(libraries)));
    }

    public static class UnsupportedOptiFineInstallationException extends UnsupportedOperationException {
    }
}
