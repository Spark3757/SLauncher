package ru.spark.slauncher.game;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.spark.slauncher.download.DefaultCacheRepository;

import java.nio.file.Paths;

public class SLauncherCacheRepository extends DefaultCacheRepository {

    public static final SLauncherCacheRepository REPOSITORY = new SLauncherCacheRepository();
    private final StringProperty directory = new SimpleStringProperty();

    public SLauncherCacheRepository() {
        directory.addListener((a, b, t) -> changeDirectory(Paths.get(t)));
    }

    public String getDirectory() {
        return directory.get();
    }

    public void setDirectory(String directory) {
        this.directory.set(directory);
    }

    public StringProperty directoryProperty() {
        return directory;
    }
}
