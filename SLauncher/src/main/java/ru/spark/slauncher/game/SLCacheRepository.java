package ru.spark.slauncher.game;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ru.spark.slauncher.download.DefaultCacheRepository;

import java.nio.file.Paths;

public class SLCacheRepository extends DefaultCacheRepository {

    public static final SLCacheRepository REPOSITORY = new SLCacheRepository();
    private final StringProperty directory = new SimpleStringProperty();

    public SLCacheRepository() {
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
