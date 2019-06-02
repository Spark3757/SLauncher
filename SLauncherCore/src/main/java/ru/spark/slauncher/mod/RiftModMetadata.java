package ru.spark.slauncher.mod;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Immutable
public final class RiftModMetadata {
    private final String id;
    private final String name;
    private final List<String> authors;

    public RiftModMetadata() {
        this("", "", Collections.emptyList());
    }

    public RiftModMetadata(String id, String name, List<String> authors) {
        this.id = id;
        this.name = name;
        this.authors = authors;
    }

    public static ModInfo fromFile(ModManager modManager, File modFile) throws IOException, JsonParseException {
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(modFile.toPath())) {
            Path mcmod = fs.getPath("riftmod.json");
            if (Files.notExists(mcmod))
                throw new IOException("File " + modFile + " is not a Forge mod.");
            RiftModMetadata metadata = JsonUtils.fromNonNullJson(IOUtils.readFullyAsString(Files.newInputStream(mcmod)), RiftModMetadata.class);
            String authors = metadata.getAuthors() == null ? "" : String.join(", ", metadata.getAuthors());
            return new ModInfo(modManager, modFile, metadata.getName(), "",
                    authors, "", "", "");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getAuthors() {
        return authors;
    }
}
