package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.Validation;

import java.util.Map;

/**
 * @author spark1337
 */
@Immutable
public final class User implements Validation {

    private final String id;

    @Nullable
    @JsonAdapter(PropertyMapSerializer.class)
    private final Map<String, String> properties;

    public User(String id) {
        this(id, null);
    }

    public User(String id, @Nullable Map<String, String> properties) {
        this.id = id;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(id))
            throw new JsonParseException("User id cannot be empty.");
    }
}
