package ru.spark.slauncher.mod;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.gson.Validation;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@Immutable
public class PackMcMeta implements Validation {

    @SerializedName("pack")
    private final PackInfo pack;

    public PackMcMeta() {
        this(new PackInfo());
    }

    public PackMcMeta(PackInfo packInfo) {
        this.pack = packInfo;
    }

    public PackInfo getPackInfo() {
        return pack;
    }

    @Override
    public void validate() throws JsonParseException {
        if (pack == null)
            throw new JsonParseException("pack cannot be null");
    }

    public static class PackInfo {
        @SerializedName("pack_format")
        private final int packFormat;

        @SerializedName("description")
        private final String description;

        public PackInfo() {
            this(0, "");
        }

        public PackInfo(int packFormat, String description) {
            this.packFormat = packFormat;
            this.description = description;
        }

        public int getPackFormat() {
            return packFormat;
        }

        public String getDescription() {
            return description;
        }
    }

    public static ModInfo fromFile(ModManager modManager, File modFile) throws IOException, JsonParseException {
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(modFile.toPath())) {
            Path mcmod = fs.getPath("pack.mcmeta");
            if (Files.notExists(mcmod))
                throw new IOException("File " + modFile + " is not a resource pack.");
            PackMcMeta metadata = JsonUtils.fromNonNullJson(FileUtils.readText(mcmod), PackMcMeta.class);
            return new ModInfo(modManager, modFile, metadata.pack.description, "", "", "", "", "");
        }
    }
}
