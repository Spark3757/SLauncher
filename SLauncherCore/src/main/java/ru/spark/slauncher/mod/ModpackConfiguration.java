package ru.spark.slauncher.mod;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Immutable
public final class ModpackConfiguration<T> implements Validation {

    private final T manifest;
    private final String type;
    private final List<FileInformation> overrides;

    public ModpackConfiguration() {
        this(null, null, Collections.emptyList());
    }

    public ModpackConfiguration(T manifest, String type, List<FileInformation> overrides) {
        this.manifest = manifest;
        this.type = type;
        this.overrides = new ArrayList<>(overrides);
    }

    public T getManifest() {
        return manifest;
    }

    public ModpackConfiguration<T> setManifest(T manifest) {
        return new ModpackConfiguration<>(manifest, type, overrides);
    }

    public String getType() {
        return type;
    }

    public List<FileInformation> getOverrides() {
        return Collections.unmodifiableList(overrides);
    }

    public ModpackConfiguration<T> setOverrides(List<FileInformation> overrides) {
        return new ModpackConfiguration<>(manifest, type, overrides);
    }

    @Override
    public void validate() throws JsonParseException {
        if (manifest == null)
            throw new JsonParseException("MinecraftInstanceConfiguration missing `manifest`");
        if (type == null)
            throw new JsonParseException("MinecraftInstanceConfiguration missing `type`");
    }

    @Immutable
    public static class FileInformation implements Validation {
        private final String path; // relative
        private final String hash;
        private final String downloadURL;

        public FileInformation() {
            this(null, null);
        }

        public FileInformation(String path, String hash) {
            this(path, hash, null);
        }

        public FileInformation(String path, String hash, String downloadURL) {
            this.path = path;
            this.hash = hash;
            this.downloadURL = downloadURL;
        }

        /**
         * The relative path to Minecraft run directory
         *
         * @return the relative path to Minecraft run directory.
         */
        public String getPath() {
            return path;
        }

        public String getDownloadURL() {
            return downloadURL;
        }

        public String getHash() {
            return hash;
        }

        @Override
        public void validate() throws JsonParseException {
            if (path == null)
                throw new JsonParseException("FileInformation missing `path`.");
            if (hash == null)
                throw new JsonParseException("FileInformation missing file hash code.");
        }
    }
}
