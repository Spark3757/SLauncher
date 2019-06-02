package ru.spark.slauncher.download.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.game.ReleaseType;
import ru.spark.slauncher.util.Constants;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.Validation;

import java.util.Date;

/**
 * @author Spark1337
 */
public final class GameRemoteVersionInfo implements Validation {

    @SerializedName("id")
    private final String gameVersion;

    @SerializedName("time")
    private final Date time;

    @SerializedName("releaseTime")
    private final Date releaseTime;

    @SerializedName("type")
    private final ReleaseType type;

    @SerializedName("url")
    private final String url;

    public GameRemoteVersionInfo() {
        this("", new Date(), new Date(), ReleaseType.UNKNOWN);
    }

    public GameRemoteVersionInfo(String gameVersion, Date time, Date releaseTime, ReleaseType type) {
        this(gameVersion, time, releaseTime, type, Constants.DEFAULT_LIBRARY_URL + gameVersion + "/" + gameVersion + ".json");
    }

    public GameRemoteVersionInfo(String gameVersion, Date time, Date releaseTime, ReleaseType type, String url) {
        this.gameVersion = gameVersion;
        this.time = time;
        this.releaseTime = releaseTime;
        this.type = type;
        this.url = url;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public Date getTime() {
        return time;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public ReleaseType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(gameVersion))
            throw new JsonParseException("GameRemoteVersion id cannot be blank");
        if (StringUtils.isBlank(url))
            throw new JsonParseException("GameRemoteVersion url cannot be blank");
    }
}
