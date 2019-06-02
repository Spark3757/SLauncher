package ru.spark.slauncher.download.liteloader;

import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.Library;

import java.util.Collection;

public class LiteLoaderRemoteVersion extends RemoteVersion {
    private final String tweakClass;
    private final Collection<Library> libraries;

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param url         the installer or universal jar URL.
     */
    LiteLoaderRemoteVersion(String gameVersion, String selfVersion, String url, String tweakClass, Collection<Library> libraries) {
        super(gameVersion, selfVersion, url);

        this.tweakClass = tweakClass;
        this.libraries = libraries;
    }

    public Collection<Library> getLibraries() {
        return libraries;
    }

    public String getTweakClass() {
        return tweakClass;
    }

}
