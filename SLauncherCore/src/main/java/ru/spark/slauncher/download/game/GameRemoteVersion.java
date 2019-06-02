package ru.spark.slauncher.download.game;

import ru.spark.slauncher.download.RemoteVersion;
import ru.spark.slauncher.game.ReleaseType;
import ru.spark.slauncher.util.Immutable;

import java.util.Date;

/**
 * @author Spark1337
 */
@Immutable
public final class GameRemoteVersion extends RemoteVersion {

    private final ReleaseType type;
    private final Date time;

    public GameRemoteVersion(String gameVersion, String selfVersion, String url, ReleaseType type, Date time) {
        super(gameVersion, selfVersion, url, getReleaseType(type));
        this.type = type;
        this.time = time;
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

    public Date getTime() {
        return time;
    }

    public ReleaseType getType() {
        return type;
    }

    @Override
    public int compareTo(RemoteVersion o) {
        if (!(o instanceof GameRemoteVersion))
            return 0;

        return ((GameRemoteVersion) o).getTime().compareTo(getTime());
    }
}
