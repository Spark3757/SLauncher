package ru.spark.slauncher.download;

import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.versioning.VersionNumber;

import java.util.Objects;

/**
 * The remote version.
 *
 * @author Spark1337
 */
public class RemoteVersion implements Comparable<RemoteVersion> {

    private final String gameVersion;
    private final String selfVersion;
    private final String url;
    private final Type type;

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param url         the installer or universal jar URL.
     */
    public RemoteVersion(String gameVersion, String selfVersion, String url) {
        this(gameVersion, selfVersion, url, Type.UNCATEGORIZED);
    }

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param url         the installer or universal jar URL.
     */
    public RemoteVersion(String gameVersion, String selfVersion, String url, Type type) {
        this.gameVersion = Objects.requireNonNull(gameVersion);
        this.selfVersion = Objects.requireNonNull(selfVersion);
        this.url = Objects.requireNonNull(url);
        this.type = Objects.requireNonNull(type);
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getSelfVersion() {
        return selfVersion;
    }

    public String getUrl() {
        return url;
    }

    public Type getVersionType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemoteVersion && Objects.equals(selfVersion, ((RemoteVersion) obj).selfVersion);
    }

    @Override
    public int hashCode() {
        return selfVersion.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selfVersion", selfVersion)
                .append("gameVersion", gameVersion)
                .toString();
    }

    @Override
    public int compareTo(RemoteVersion o) {
        // newer versions are smaller than older versions
        return VersionNumber.asVersion(o.selfVersion).compareTo(VersionNumber.asVersion(selfVersion));
    }

    public enum Type {
        UNCATEGORIZED,
        RELEASE,
        SNAPSHOT,
        OLD
    }
}
