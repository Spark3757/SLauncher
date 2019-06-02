package ru.spark.slauncher.mod;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Complete the CurseForge version.
 *
 * @author Spark1337
 */
public final class CurseCompletionTask extends Task {

    private final DefaultGameRepository repository;
    private final ModManager modManager;
    private final String version;
    private final List<Task> dependents = new LinkedList<>();
    private final List<Task> dependencies = new LinkedList<>();
    private CurseManifest manifest = null;

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager.
     * @param version           the existent and physical version.
     */
    public CurseCompletionTask(DefaultDependencyManager dependencyManager, String version) {
        this(dependencyManager, version, null);
    }

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager.
     * @param version           the existent and physical version.
     * @param manifest          the CurseForgeModpack manifest.
     */
    public CurseCompletionTask(DefaultDependencyManager dependencyManager, String version, CurseManifest manifest) {
        this.repository = dependencyManager.getGameRepository();
        this.modManager = repository.getModManager(version);
        this.version = version;
        this.manifest = manifest;

        if (manifest == null)
            try {
                File manifestFile = new File(repository.getVersionRoot(version), "manifest.json");
                if (manifestFile.exists())
                    this.manifest = JsonUtils.GSON.fromJson(FileUtils.readText(manifestFile), CurseManifest.class);
            } catch (Exception e) {
                Logging.LOG.log(Level.WARNING, "Unable to read CurseForge modpack manifest.json", e);
            }
    }

    @Override
    public Collection<Task> getDependencies() {
        return dependencies;
    }

    @Override
    public Collection<Task> getDependents() {
        return dependents;
    }

    @Override
    public void execute() throws Exception {
        if (manifest == null)
            return;

        File root = repository.getVersionRoot(version);

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger finished = new AtomicInteger(0);
        AtomicBoolean notFound = new AtomicBoolean(false);

        // Because in China, Curse is too difficult to visit,
        // if failed, ignore it and retry next time.
        CurseManifest newManifest = manifest.setFiles(
                manifest.getFiles().parallelStream()
                        .map(file -> {
                            updateProgress(finished.incrementAndGet(), manifest.getFiles().size());
                            if (StringUtils.isBlank(file.getFileName())) {
                                try {
                                    return file.withFileName(NetworkUtils.detectFileName(file.getUrl()));
                                } catch (FileNotFoundException e) {
                                    try {
                                        String result = NetworkUtils.doGet(NetworkUtils.toURL(String.format("https://cursemeta.dries007.net/%d/%d.json", file.getProjectID(), file.getFileID())));
                                        CurseMetaMod mod = JsonUtils.fromNonNullJson(result, CurseMetaMod.class);
                                        return file.withFileName(mod.getFileNameOnDisk()).withURL(mod.getDownloadURL());
                                    } catch (IOException | JsonParseException e2) {
                                        Logging.LOG.log(Level.WARNING, "Could not query cursemeta for deleted mods: " + file.getUrl(), e2);
                                        notFound.set(true);
                                        return file;
                                    }

                                } catch (IOException ioe) {
                                    Logging.LOG.log(Level.WARNING, "Unable to fetch the file name of URL: " + file.getUrl(), ioe);
                                    flag.set(false);
                                    return file;
                                }
                            } else
                                return file;
                        })
                        .collect(Collectors.toList()));
        FileUtils.writeText(new File(root, "manifest.json"), JsonUtils.GSON.toJson(newManifest));

        for (CurseManifestFile file : newManifest.getFiles())
            if (StringUtils.isNotBlank(file.getFileName())) {
                if (!modManager.hasSimpleMod(file.getFileName())) {
                    dependencies.add(new FileDownloadTask(file.getUrl(), modManager.getSimpleModPath(file.getFileName()).toFile()).setCaching(true));
                }
            }

        // Let this task fail if the curse manifest has not been completed.
        // But continue other downloads.
        if (!flag.get() || notFound.get())
            dependencies.add(Task.of(() -> {
                if (notFound.get())
                    throw new CurseCompletionException(new FileNotFoundException());
                else
                    throw new CurseCompletionException();
            }));
    }

}
