package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.DefaultDependencyManager;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.ReleaseType;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Immutable;

import java.util.Date;
import java.util.List;

/**
 * @author spark1337
 */
@Immutable
public final class GameRemoteVersion extends RemoteVersion {

    private final ReleaseType type;
    private final Date time;

    public GameRemoteVersion(String gameVersion, String selfVersion, List<String> url, ReleaseType type, Date time) {
        super(LibraryAnalyzer.LibraryType.MINECRAFT.getPatchId(), gameVersion, selfVersion, getReleaseType(type), url);
        this.type = type;
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public ReleaseType getType() {
        return type;
    }

    @Override
    public Task<Version> getInstallTask(DefaultDependencyManager dependencyManager, Version baseVersion) {
        return new GameInstallTask(dependencyManager, baseVersion, this);
    }

    @Override
    public int compareTo(RemoteVersion o) {
        if (!(o instanceof GameRemoteVersion))
            return 0;

        return ((GameRemoteVersion) o).getTime().compareTo(getTime());
    }

    private static Type getReleaseType(ReleaseType type) {
        if (type == null) return Type.UNCATEGORIZED;
        switch (type) {
            case RELEASE:
                return Type.RELEASE;
            case SNAPSHOT:
                return Type.SNAPSHOT;
            case UNKNOWN:
                return Type.UNCATEGORIZED;
            default:
                return Type.OLD;
        }
    }
}
