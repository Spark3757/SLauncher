package ru.spark.slauncher.download.liteloader;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;

import java.util.Collections;
import java.util.Map;

/**
 * @author Spark1337
 */
@Immutable
public final class LiteLoaderVersionsRoot {

    @SerializedName("versions")
    private final Map<String, LiteLoaderGameVersions> versions;

    @SerializedName("meta")
    private final LiteLoaderVersionsMeta meta;

    public LiteLoaderVersionsRoot() {
        this(Collections.emptyMap(), null);
    }

    public LiteLoaderVersionsRoot(Map<String, LiteLoaderGameVersions> versions, LiteLoaderVersionsMeta meta) {
        this.versions = versions;
        this.meta = meta;
    }

    public Map<String, LiteLoaderGameVersions> getVersions() {
        return Collections.unmodifiableMap(versions);
    }

    public LiteLoaderVersionsMeta getMeta() {
        return meta;
    }

}
