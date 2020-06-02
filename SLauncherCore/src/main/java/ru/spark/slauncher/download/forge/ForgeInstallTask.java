package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.DependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.VersionMismatchException;
import ru.spark.slauncher.download.optifine.OptiFineInstallTask;
import ru.spark.slauncher.game.GameVersion;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static ru.spark.slauncher.util.StringUtils.removePrefix;
import static ru.spark.slauncher.util.StringUtils.removeSuffix;

/**
 * @author spark1337
 */
public final class ForgeInstallTask extends Task<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private Path installer;
    private final ForgeRemoteVersion remote;
    private FileDownloadTask dependent;
    private Task<Version> dependency;

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

        dependent = new FileDownloadTask(
                dependencyManager.getDownloadProvider().injectURLsWithCandidates(remote.getUrls()),
                installer.toFile(), null);
        dependent.setCacheRepository(dependencyManager.getCacheRepository());
        dependent.setCaching(true);
        dependent.addIntegrityCheckHandler(FileDownloadTask.ZIP_INTEGRITY_CHECK_HANDLER);
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
    public Collection<Task<?>> getDependents() {
        return Collections.singleton(dependent);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return Collections.singleton(dependency);
    }

    @Override
    public void execute() throws IOException, VersionMismatchException, OptiFineInstallTask.UnsupportedOptiFineInstallationException {
        String originalMainClass = version.resolve(dependencyManager.getGameRepository()).getMainClass();
        if (VersionNumber.VERSION_COMPARATOR.compare("1.13", remote.getGameVersion()) <= 0) {
            // Forge 1.13 is not compatible with fabric.
            if (!LibraryAnalyzer.VANILLA_MAIN.equals(originalMainClass) && !LibraryAnalyzer.MOD_LAUNCHER_MAIN.equals(originalMainClass) && !LibraryAnalyzer.LAUNCH_WRAPPER_MAIN.equals(originalMainClass))
                throw new OptiFineInstallTask.UnsupportedOptiFineInstallationException();
        } else {
            // Forge 1.12 and older versions is compatible with vanilla and launchwrapper.
            // if (!"net.minecraft.client.main.Main".equals(originalMainClass) && !"net.minecraft.launchwrapper.Launch".equals(originalMainClass))
            //     throw new OptiFineInstallTask.UnsupportedOptiFineInstallationException();
        }


        if (detectForgeInstallerType(dependencyManager, version, installer))
            dependency = new ForgeNewInstallTask(dependencyManager, version, remote.getSelfVersion(), installer);
        else
            dependency = new ForgeOldInstallTask(dependencyManager, version, remote.getSelfVersion(), installer);
    }

    /**
     * Detect Forge installer type.
     *
     * @param dependencyManager game repository
     * @param version           version.json
     * @param installer         the Forge installer, either the new or old one.
     * @return true for new, false for old
     * @throws IOException              if unable to read compressed content of installer file, or installer file is corrupted, or the installer is not the one we want.
     * @throws VersionMismatchException if required game version of installer does not match the actual one.
     */
    public static boolean detectForgeInstallerType(DependencyManager dependencyManager, Version version, Path installer) throws IOException, VersionMismatchException {
        Optional<String> gameVersion = GameVersion.minecraftVersion(dependencyManager.getGameRepository().getVersionJar(version));
        if (!gameVersion.isPresent()) throw new IOException();
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(installer)) {
            String installProfileText = FileUtils.readText(fs.getPath("install_profile.json"));
            Map<?, ?> installProfile = JsonUtils.fromNonNullJson(installProfileText, Map.class);
            if (installProfile.containsKey("spec")) {
                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.get().equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion.get());
                return true;
            } else if (installProfile.containsKey("install") && installProfile.containsKey("versionInfo")) {
                ForgeInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeInstallProfile.class);
                if (!gameVersion.get().equals(profile.getInstall().getMinecraft()))
                    throw new VersionMismatchException(profile.getInstall().getMinecraft(), gameVersion.get());
                return false;
            } else {
                throw new IOException();
            }
        }
    }

    /**
     * Install Forge library from existing local file.
     * This method will try to identify this installer whether it is in old or new format.
     *
     * @param dependencyManager game repository
     * @param version           version.json
     * @param installer         the Forge installer, either the new or old one.
     * @return the task to install library
     * @throws IOException              if unable to read compressed content of installer file, or installer file is corrupted, or the installer is not the one we want.
     * @throws VersionMismatchException if required game version of installer does not match the actual one.
     */
    public static Task<Version> install(DefaultDependencyManager dependencyManager, Version version, Path installer) throws IOException, VersionMismatchException {
        Optional<String> gameVersion = GameVersion.minecraftVersion(dependencyManager.getGameRepository().getVersionJar(version));
        if (!gameVersion.isPresent()) throw new IOException();
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(installer)) {
            String installProfileText = FileUtils.readText(fs.getPath("install_profile.json"));
            Map<?, ?> installProfile = JsonUtils.fromNonNullJson(installProfileText, Map.class);
            if (installProfile.containsKey("spec")) {
                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.get().equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion.get());
                return new ForgeNewInstallTask(dependencyManager, version, modifyVersion(gameVersion.get(), profile.getPath().getVersion().replaceAll("(?i)forge", "")), installer);
            } else if (installProfile.containsKey("install") && installProfile.containsKey("versionInfo")) {
                ForgeInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeInstallProfile.class);
                if (!gameVersion.get().equals(profile.getInstall().getMinecraft()))
                    throw new VersionMismatchException(profile.getInstall().getMinecraft(), gameVersion.get());
                return new ForgeOldInstallTask(dependencyManager, version, modifyVersion(gameVersion.get(), profile.getInstall().getPath().getVersion().replaceAll("(?i)forge", "")), installer);
            } else {
                throw new IOException();
            }
        }
    }

    private static String modifyVersion(String gameVersion, String version) {
        return removeSuffix(removePrefix(removeSuffix(removePrefix(version.replace(gameVersion, "").trim(), "-"), "-"), "_"), "_");
    }
}
