package ru.spark.slauncher.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * @author Spark1337
 */
public final class JsonUtils {

    public static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .registerTypeAdapter(Date.class, DateTypeAdapter.INSTANCE)
            .registerTypeAdapter(UUID.class, UUIDTypeAdapter.INSTANCE)
            .registerTypeAdapter(File.class, FileTypeAdapter.INSTANCE)
            .registerTypeAdapterFactory(ValidationTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(LowerCaseEnumTypeAdapterFactory.INSTANCE)
            .create();

    private JsonUtils() {
    }

    public static <T> T fromNonNullJson(String json, Class<T> classOfT) throws JsonParseException {
        T parsed = GSON.fromJson(json, classOfT);
        if (parsed == null)
            throw new JsonParseException("Json object cannot be null.");
        return parsed;
    }
}
