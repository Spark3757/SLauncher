package ru.spark.slauncher.game;

import ru.spark.slauncher.util.Immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Spark1337
 */
@Immutable
public final class LibrariesDownloadInfo {

    private final LibraryDownloadInfo artifact;
    private final Map<String, LibraryDownloadInfo> classifiers;

    public LibrariesDownloadInfo(LibraryDownloadInfo artifact) {
        this(artifact, Collections.emptyMap());
    }

    public LibrariesDownloadInfo(LibraryDownloadInfo artifact, Map<String, LibraryDownloadInfo> classifiers) {
        this.artifact = artifact;
        this.classifiers = classifiers == null ? null : new HashMap<>(classifiers);
    }

    public LibraryDownloadInfo getArtifact() {
        return artifact;
    }

    public Map<String, LibraryDownloadInfo> getClassifiers() {
        return classifiers == null ? Collections.emptyMap() : Collections.unmodifiableMap(classifiers);
    }

}
