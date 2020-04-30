package ru.spark.slauncher.download.liteloader;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.game.Library;
import ru.spark.slauncher.util.Immutable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author spark1337
 */
@Immutable
public final class LiteLoaderBranch {

    @SerializedName("libraries")
    private final Collection<Library> libraries;

    @SerializedName("com.mumfrey:liteloader")
    private final Map<String, LiteLoaderVersion> liteLoader;

    /**
     * No-arg constructor for Gson.
     */
    @SuppressWarnings("unused")
    public LiteLoaderBranch() {
        this(Collections.emptySet(), Collections.emptyMap());
    }

    public LiteLoaderBranch(Collection<Library> libraries, Map<String, LiteLoaderVersion> liteLoader) {
        this.libraries = libraries;
        this.liteLoader = liteLoader;
    }

    public Collection<Library> getLibraries() {
        return Collections.unmodifiableCollection(libraries);
    }

    public Map<String, LiteLoaderVersion> getLiteLoader() {
        return Collections.unmodifiableMap(liteLoader);
    }

}
