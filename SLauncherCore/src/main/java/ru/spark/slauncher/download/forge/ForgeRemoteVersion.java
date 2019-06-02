package ru.spark.slauncher.download.forge;

import ru.spark.slauncher.download.RemoteVersion;

public class ForgeRemoteVersion extends RemoteVersion {
    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param url         the installer or universal jar URL.
     */
    public ForgeRemoteVersion(String gameVersion, String selfVersion, String url) {
        super(gameVersion, selfVersion, url);
    }
}
