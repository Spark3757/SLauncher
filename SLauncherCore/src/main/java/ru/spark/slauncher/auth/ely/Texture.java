package ru.spark.slauncher.auth.ely;

import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.util.Immutable;

import java.util.Map;

@Immutable
public final class Texture {

    private final String url;
    private final Map<String, String> metadata;

    public Texture() {
        this(null, null);
    }

    public Texture(String url, Map<String, String> metadata) {
        this.url = url;
        this.metadata = metadata;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
