package ru.spark.slauncher.mod;

import ru.spark.slauncher.game.DefaultGameRepository;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class ModpackUpdateTask extends Task {

    private final DefaultGameRepository repository;
    private final String id;
    private final Task updateTask;
    private final Path backupFolder;

    public ModpackUpdateTask(DefaultGameRepository repository, String id, Task updateTask) {
        this.repository = repository;
        this.id = id;
        this.updateTask = updateTask;

        Path backup = repository.getBaseDirectory().toPath().resolve("backup");
        while (true) {
            int num = (int) (Math.random() * 10000000);
            if (!Files.exists(backup.resolve(id + "-" + num))) {
                backupFolder = backup.resolve(id + "-" + num);
                break;
            }
        }
    }

    @Override
    public Collection<? extends Task> getDependencies() {
        return Collections.singleton(updateTask);
    }

    @Override
    public void execute() throws Exception {
        FileUtils.copyDirectory(repository.getVersionRoot(id).toPath(), backupFolder);
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        if (isDependenciesSucceeded()) {
            // Keep backup game version for further repair.
        } else {
            // Restore backup
            repository.removeVersionFromDisk(id);

            FileUtils.copyDirectory(backupFolder, repository.getVersionRoot(id).toPath());
        }
    }
}
