package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import ru.spark.slauncher.util.Immutable;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @author Spark1337
 */
@Immutable
public class CompleteGameProfile extends GameProfile {

    @JsonAdapter(PropertyMapSerializer.class)
    private final Map<String, String> properties;

    public CompleteGameProfile(UUID id, String name, Map<String, String> properties) {
        super(id, name);
        this.properties = requireNonNull(properties);
    }

    public CompleteGameProfile(GameProfile profile, Map<String, String> properties) {
        this(profile.getId(), profile.getName(), properties);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void validate() throws JsonParseException {
        super.validate();

        if (properties == null)
            throw new JsonParseException("Game profile properties cannot be null");
    }
}
