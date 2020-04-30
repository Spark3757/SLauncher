package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.ArtifactMalformedException;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ForgeOldInstallTask extends Task<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final Path installer;
    private final String selfVersion;
    private final List<Task<?>> dependencies = new LinkedList<>();

    ForgeOldInstallTask(DefaultDependencyManager dependencyManager, Version version, String selfVersion, Path installer) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.installer = installer;
        this.selfVersion = selfVersion;

        setSignificance(TaskSignificance.MINOR);
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        try (ZipFile zipFile = new ZipFile(installer.toFile())) {
            InputStream stream = zipFile.getInputStream(zipFile.getEntry("install_profile.json"));
            if (stream == null)
                throw new ArtifactMalformedException("Malformed forge installer file, install_profile.json does not exist.");
            String json = IOUtils.readFullyAsString(stream);
            ForgeInstallProfile installProfile = JsonUtils.fromNonNullJson(json, ForgeInstallProfile.class);

            // unpack the universal jar in the installer file.
            Library forgeLibrary = new Library(installProfile.getInstall().getPath());
            File forgeFile = dependencyManager.getGameRepository().getLibraryFile(version, forgeLibrary);
            if (!FileUtils.makeFile(forgeFile))
                throw new IOException("Cannot make directory " + forgeFile.getParent());

            ZipEntry forgeEntry = zipFile.getEntry(installProfile.getInstall().getFilePath());
            try (InputStream is = zipFile.getInputStream(forgeEntry); OutputStream os = new FileOutputStream(forgeFile)) {
                IOUtils.copyTo(is, os);
            }

            setResult(installProfile.getVersionInfo()
                    .setPriority(30000)
                    .setId(LibraryAnalyzer.LibraryType.FORGE.getPatchId())
                    .setVersion(selfVersion));
            dependencies.add(dependencyManager.checkLibraryCompletionAsync(installProfile.getVersionInfo(), true));
        } catch (ZipException ex) {
            throw new ArtifactMalformedException("Malformed forge installer file", ex);
        }
    }
}
