package ru.spark.slauncher.download.game;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * @author spark1337
 */
@Immutable
public final class GameRemoteVersions {

    @SerializedName("versions")
    private final List<GameRemoteVersionInfo> versions;

    @SerializedName("latest")
    private final GameRemoteLatestVersions latest;

    /**
     * No-arg constructor for Gson.
     */
    @SuppressWarnings("unused")
    public GameRemoteVersions() {
        this(Collections.emptyList(), null);
    }

    public GameRemoteVersions(List<GameRemoteVersionInfo> versions, GameRemoteLatestVersions latest) {
        this.versions = versions;
        this.latest = latest;
    }

    public GameRemoteLatestVersions getLatest() {
        return latest;
    }

    public List<GameRemoteVersionInfo> getVersions() {
        return versions;
    }

}
