package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.VersionMismatchException;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Spark1337
 */
public final class ForgeInstallTask extends TaskResult<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final ForgeRemoteVersion remote;
    private Path installer;
    private Task dependent;
    private TaskResult<Version> dependency;

    public ForgeInstallTask(DefaultDependencyManager dependencyManager, Version version, ForgeRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.remote = remoteVersion;
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void preExecute() throws Exception {
        installer = Files.createTempFile("forge-installer", ".jar");

        dependent = new FileDownloadTask(NetworkUtils.toURL(remote.getUrl()), installer.toFile())
                .setCaching(true);
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        Files.deleteIfExists(installer);
        setResult(dependency.getResult());
    }

    @Override
    public Collection<Task> getDependents() {
        return Collections.singleton(dependent);
    }

    @Override
    public Collection<Task> getDependencies() {
        return Collections.singleton(dependency);
    }

    @Override
    public void execute() {
        if (VersionNumber.VERSION_COMPARATOR.compare("1.13", remote.getGameVersion()) <= 0)
            dependency = new ForgeNewInstallTask(dependencyManager, version, installer);
        else
            dependency = new ForgeOldInstallTask(dependencyManager, version, installer);
    }

    /**
     * Install Forge library from existing local file.
     *
     * @param dependencyManager game repository
     * @param version           version.json
     * @param installer         the Forge installer, either the new or old one.
     * @return the task to install library
     * @throws IOException              if unable to read compressed content of installer file, or installer file is corrupted, or the installer is not the one we want.
     * @throws VersionMismatchException if required game version of installer does not match the actual one.
     */
    public static TaskResult<Version> install(DefaultDependencyManager dependencyManager, Version version, Path installer) throws IOException, VersionMismatchException {
        Optional<String> gameVersion = GameVersion.minecraftVersion(dependencyManager.getGameRepository().getVersionJar(version));
        if (!gameVersion.isPresent())
            throw new IOException("Unable to read compressed content of installer file, or installer file is corrupted, or the installer is not the one we want.");
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(installer)) {
            String installProfileText = FileUtils.readText(fs.getPath("install_profile.json"));
            Map installProfile = JsonUtils.fromNonNullJson(installProfileText, Map.class);
            if (installProfile.containsKey("spec")) {
                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.get().equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion.get());
                return new ForgeNewInstallTask(dependencyManager, version, installer);
            } else if (installProfile.containsKey("install") && installProfile.containsKey("versionInfo")) {
                ForgeInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeInstallProfile.class);
                if (!gameVersion.get().equals(profile.getInstall().getMinecraft()))
                    throw new VersionMismatchException(profile.getInstall().getMinecraft(), gameVersion.get());
                return new ForgeOldInstallTask(dependencyManager, version, installer);
            } else {
                throw new IOException();
            }
        }
    }
}
