package ru.spark.slauncher.mod;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Spark1337
 */
@Immutable
public final class LiteModMetadata {
    private final String name;
    private final String version;
    private final String mcversion;
    private final String revision;
    private final String author;
    private final String classTransformerClasses;
    private final String description;
    private final String modpackName;
    private final String modpackVersion;
    private final String checkUpdateUrl;
    private final String updateURI;

    public LiteModMetadata() {
        this("", "", "", "", "", "", "", "", "", "", "");
    }

    public LiteModMetadata(String name, String version, String mcversion, String revision, String author, String classTransformerClasses, String description, String modpackName, String modpackVersion, String checkUpdateUrl, String updateURI) {
        this.name = name;
        this.version = version;
        this.mcversion = mcversion;
        this.revision = revision;
        this.author = author;
        this.classTransformerClasses = classTransformerClasses;
        this.description = description;
        this.modpackName = modpackName;
        this.modpackVersion = modpackVersion;
        this.checkUpdateUrl = checkUpdateUrl;
        this.updateURI = updateURI;
    }

    public static ModInfo fromFile(ModManager modManager, File modFile) throws IOException, JsonParseException {
        try (ZipFile zipFile = new ZipFile(modFile)) {
            ZipEntry entry = zipFile.getEntry("litemod.json");
            if (entry == null)
                throw new IOException("File " + modFile + "is not a LiteLoader mod.");
            LiteModMetadata metadata = JsonUtils.GSON.fromJson(IOUtils.readFullyAsString(zipFile.getInputStream(entry)), LiteModMetadata.class);
            if (metadata == null)
                throw new IOException("Mod " + modFile + " `litemod.json` is malformed.");
            return new ModInfo(modManager, modFile, metadata.getName(), metadata.getDescription(), metadata.getAuthor(),
                    metadata.getVersion(), metadata.getGameVersion(), metadata.getUpdateURI());
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getGameVersion() {
        return mcversion;
    }

    public String getRevision() {
        return revision;
    }

    public String getAuthor() {
        return author;
    }

    public String getClassTransformerClasses() {
        return classTransformerClasses;
    }

    public String getDescription() {
        return description;
    }

    public String getModpackName() {
        return modpackName;
    }

    public String getModpackVersion() {
        return modpackVersion;
    }

    public String getCheckUpdateUrl() {
        return checkUpdateUrl;
    }

    public String getUpdateURI() {
        return updateURI;
    }

}
