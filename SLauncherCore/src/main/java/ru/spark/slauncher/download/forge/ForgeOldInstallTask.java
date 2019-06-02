package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.game.SimpleVersionProvider;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.task.TaskResult;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ForgeOldInstallTask extends TaskResult<Version> {

    private final DefaultDependencyManager dependencyManager;
    private final Version version;
    private final Path installer;
    private final List<Task> dependencies = new LinkedList<>();

    public ForgeOldInstallTask(DefaultDependencyManager dependencyManager, Version version, Path installer) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        this.installer = installer;

        setSignificance(TaskSignificance.MINOR);
    }

    @Override
    public List<Task> getDependencies() {
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
                throw new IOException("Malformed forge installer file, install_profile.json does not exist.");
            String json = IOUtils.readFullyAsString(stream);
            ForgeInstallProfile installProfile = JsonUtils.fromNonNullJson(json, ForgeInstallProfile.class);

            // unpack the universal jar in the installer file.
            Library forgeLibrary = Library.fromName(installProfile.getInstall().getPath());
            File forgeFile = dependencyManager.getGameRepository().getLibraryFile(version, forgeLibrary);
            if (!FileUtils.makeFile(forgeFile))
                throw new IOException("Cannot make directory " + forgeFile.getParent());

            ZipEntry forgeEntry = zipFile.getEntry(installProfile.getInstall().getFilePath());
            try (InputStream is = zipFile.getInputStream(forgeEntry); OutputStream os = new FileOutputStream(forgeFile)) {
                IOUtils.copyTo(is, os);
            }

            // resolve the version
            SimpleVersionProvider provider = new SimpleVersionProvider();
            provider.addVersion(version);

            setResult(installProfile.getVersionInfo()
                    .setInheritsFrom(version.getId())
                    .resolve(provider).setJar(null)
                    .setId(version.getId()).setLogging(Collections.emptyMap()));

            dependencies.add(dependencyManager.checkLibraryCompletionAsync(installProfile.getVersionInfo()));
        }
    }
}
