package ru.spark.slauncher.mod;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Immutable
public final class FabricModMetadata {
    private final String name;
    private final String version;
    private final String description;
    private final List<FabricModAuthor> authors;
    private final Map<String, String> contact;

    public FabricModMetadata() {
        this("", "", "", Collections.emptyList(), Collections.emptyMap());
    }

    public FabricModMetadata(String name, String version, String description, List<FabricModAuthor> authors, Map<String, String> contact) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.contact = contact;
    }

    public static ModInfo fromFile(ModManager modManager, File modFile) throws IOException, JsonParseException {
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(modFile.toPath())) {
            Path mcmod = fs.getPath("fabric.mod.json");
            if (Files.notExists(mcmod))
                throw new IOException("File " + modFile + " is not a Fabric mod.");
            FabricModMetadata metadata = JsonUtils.fromNonNullJson(FileUtils.readText(mcmod), FabricModMetadata.class);
            String authors = metadata.authors == null ? "" : metadata.authors.stream().map(author -> author.name).collect(Collectors.joining(", "));
            return new ModInfo(modManager, modFile, metadata.name, metadata.description,
                    authors, metadata.version, "", metadata.contact != null ? metadata.contact.getOrDefault("homepage", "") : "");
        }
    }

    @JsonAdapter(FabricModAuthorSerializer.class)
    public static final class FabricModAuthor {
        private final String name;

        public FabricModAuthor() {
            this("");
        }

        public FabricModAuthor(String name) {
            this.name = name;
        }
    }

    public static final class FabricModAuthorSerializer implements JsonSerializer<FabricModAuthor>, JsonDeserializer<FabricModAuthor> {
        @Override
        public FabricModAuthor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonPrimitive() ? new FabricModAuthor(json.getAsString()) : new FabricModAuthor(json.getAsJsonObject().getAsJsonPrimitive("name").getAsString());
        }

        @Override
        public JsonElement serialize(FabricModAuthor src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.name);
        }
    }
}
