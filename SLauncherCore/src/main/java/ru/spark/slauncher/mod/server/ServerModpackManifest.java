package ru.spark.slauncher.mod.server;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.download.LibraryAnalyzer;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.mod.ModpackConfiguration;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.gson.TolerableValidationException;
import ru.spark.slauncher.util.gson.Validation;
import ru.spark.slauncher.util.io.CompressingUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ServerModpackManifest implements Validation {
    private final String name;
    private final String author;
    private final String version;
    private final String description;
    private final String fileApi;
    private final List<ModpackConfiguration.FileInformation> files;
    private final List<Addon> addons;

    public ServerModpackManifest() {
        this("", "", "", "", "", Collections.emptyList(), Collections.emptyList());
    }

    public ServerModpackManifest(String name, String author, String version, String description, String fileApi, List<ModpackConfiguration.FileInformation> files, List<Addon> addons) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
        this.fileApi = fileApi;
        this.files = files;
        this.addons = addons;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getFileApi() {
        return fileApi;
    }

    public List<ModpackConfiguration.FileInformation> getFiles() {
        return files;
    }

    public List<Addon> getAddons() {
        return addons;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        if (fileApi == null)
            throw new JsonParseException("ServerModpackManifest.fileApi cannot be blank");
        if (files == null)
            throw new JsonParseException("ServerModpackManifest.files cannot be null");
    }

    public static final class Addon {
        private final String id;
        private final String version;

        public Addon() {
            this("", "");
        }

        public Addon(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    public Modpack toModpack(Charset encoding) throws IOException {
        String gameVersion = addons.stream().filter(x -> LibraryAnalyzer.LibraryType.MINECRAFT.getPatchId().equals(x.id)).findAny()
                .orElseThrow(() -> new IOException("Cannot find game version")).getVersion();
        return new Modpack(name, author, version, gameVersion, description, encoding, this);
    }

    /**
     * @param zip the CurseForge modpack file.
     * @return the manifest.
     * @throws IOException        if the file is not a valid zip file.
     * @throws JsonParseException if the server-manifest.json is missing or malformed.
     */
    public static Modpack readManifest(Path zip, Charset encoding) throws IOException, JsonParseException {
        String json = CompressingUtils.readTextZipEntry(zip, "server-manifest.json", encoding);
        ServerModpackManifest manifest = JsonUtils.fromNonNullJson(json, ServerModpackManifest.class);
        return manifest.toModpack(encoding);
    }
}
