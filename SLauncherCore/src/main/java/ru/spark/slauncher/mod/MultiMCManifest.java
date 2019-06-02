package ru.spark.slauncher.mod;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;
import ru.spark.slauncher.util.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Immutable
public final class MultiMCManifest {

    @SerializedName("formatVersion")
    private final int formatVersion;

    @SerializedName("components")
    private final List<MultiMCManifestComponent> components;

    public MultiMCManifest(int formatVersion, List<MultiMCManifestComponent> components) {
        this.formatVersion = formatVersion;
        this.components = components;
    }

    public static MultiMCManifest readMultiMCModpackManifest(Path zipFile, Charset encoding) throws IOException {
        try (FileSystem fs = CompressingUtils.readonly(zipFile).setEncoding(encoding).build()) {
            Path root = Files.list(fs.getPath("/")).filter(Files::isDirectory).findAny()
                    .orElseThrow(() -> new IOException("Not a valid MultiMC modpack"));
            Path mmcPack = root.resolve("mmc-pack.json");
            if (Files.notExists(mmcPack))
                return null;
            String json = IOUtils.readFullyAsString(Files.newInputStream(mmcPack));
            MultiMCManifest manifest = JsonUtils.fromNonNullJson(json, MultiMCManifest.class);

            if (manifest.getComponents() == null)
                throw new IOException("mmc-pack.json malformed.");

            return manifest;
        }
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public List<MultiMCManifestComponent> getComponents() {
        return components;
    }

    public static final class MultiMCManifestCachedRequires {
        @SerializedName("equals")
        private final String equalsVersion;

        @SerializedName("uid")
        private final String uid;

        @SerializedName("suggests")
        private final String suggests;


        public MultiMCManifestCachedRequires(String equalsVersion, String uid, String suggests) {
            this.equalsVersion = equalsVersion;
            this.uid = uid;
            this.suggests = suggests;
        }

        public String getEqualsVersion() {
            return equalsVersion;
        }

        public String getUid() {
            return uid;
        }

        public String getSuggests() {
            return suggests;
        }
    }

    public static final class MultiMCManifestComponent {
        @SerializedName("cachedName")
        private final String cachedName;

        @SerializedName("cachedRequires")
        private final List<MultiMCManifestCachedRequires> cachedRequires;

        @SerializedName("cachedVersion")
        private final String cachedVersion;

        @SerializedName("important")
        private final boolean important;

        @SerializedName("uid")
        private final String uid;

        @SerializedName("version")
        private final String version;

        public MultiMCManifestComponent(String cachedName, List<MultiMCManifestCachedRequires> cachedRequires, String cachedVersion, boolean important, String uid, String version) {
            this.cachedName = cachedName;
            this.cachedRequires = cachedRequires;
            this.cachedVersion = cachedVersion;
            this.important = important;
            this.uid = uid;
            this.version = version;
        }

        public String getCachedName() {
            return cachedName;
        }

        public List<MultiMCManifestCachedRequires> getCachedRequires() {
            return cachedRequires;
        }

        public String getCachedVersion() {
            return cachedVersion;
        }

        public boolean isImportant() {
            return important;
        }

        public String getUid() {
            return uid;
        }

        public String getVersion() {
            return version;
        }
    }
}
