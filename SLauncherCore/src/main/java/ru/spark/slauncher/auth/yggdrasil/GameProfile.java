package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.gson.Validation;

import java.util.Objects;
import java.util.UUID;

/**
 * @author spark1337
 */
@Immutable
public class GameProfile implements Validation {

    @JsonAdapter(UUIDTypeAdapter.class)
    private final UUID id;

    private final String name;

    public GameProfile(UUID id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public void validate() throws JsonParseException {
        Validation.requireNonNull(id, "Game profile id cannot be null");
        Validation.requireNonNull(name, "Game profile name cannot be null");
    }
}
