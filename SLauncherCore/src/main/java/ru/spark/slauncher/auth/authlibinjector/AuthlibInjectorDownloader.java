package ru.spark.slauncher.auth.authlibinjector;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.task.FileDownloadTask.IntegrityCheck;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;

import static ru.spark.slauncher.util.Logging.LOG;

public class AuthlibInjectorDownloader implements AuthlibInjectorArtifactProvider {

    private static final String LATEST_BUILD_URL = "https://authlib-injector.yushi.moe/artifact/latest.json";

    private Path artifactLocation;
    private Supplier<DownloadProvider> downloadProvider;

    /**
     * The flag will be reset after application restart.
     */
    private boolean updateChecked = false;

    /**
     * @param artifactsDirectory where to save authlib-injector artifacts
     */
    public AuthlibInjectorDownloader(Path artifactsDirectory, Supplier<DownloadProvider> downloadProvider) {
        this.artifactLocation = artifactsDirectory.resolve("authlib-injector.jar");
        this.downloadProvider = downloadProvider;
    }

    @Override
    public AuthlibInjectorArtifactInfo getArtifactInfo() throws IOException {
        synchronized (artifactLocation) {
            Optional<AuthlibInjectorArtifactInfo> local = getLocalArtifact();

            if (!local.isPresent() || !updateChecked) {
                try {
                    update(local);
                    updateChecked = true;
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to download authlib-injector", e);
                    if (!local.isPresent()) {
                        throw e;
                    }
                    LOG.warning("Fallback to use cached artifact: " + local.get());
                }
            }

            return getLocalArtifact().orElseThrow(() -> new IOException("The updated authlib-inejector cannot be recognized"));
        }
    }

    @Override
    public Optional<AuthlibInjectorArtifactInfo> getArtifactInfoImmediately() {
        return getLocalArtifact();
    }

    private void update(Optional<AuthlibInjectorArtifactInfo> local) throws IOException {
        LOG.info("Checking update of authlib-injector");
        AuthlibInjectorVersionInfo latest = getLatestArtifactInfo();

        if (local.isPresent() && local.get().getBuildNumber() >= latest.buildNumber) {
            return;
        }

        try {
            new FileDownloadTask(new URL(downloadProvider.get().injectURL(latest.downloadUrl)), artifactLocation.toFile(),
                    Optional.ofNullable(latest.checksums.get("sha256"))
                            .map(checksum -> new IntegrityCheck("SHA-256", checksum))
                            .orElse(null))
                    .run();
        } catch (Exception e) {
            throw new IOException("Failed to download authlib-injector", e);
        }

        LOG.info("Updated authlib-injector to " + latest.version);
    }

    private AuthlibInjectorVersionInfo getLatestArtifactInfo() throws IOException {
        try {
            return JsonUtils.fromNonNullJson(
                    NetworkUtils.doGet(
                            new URL(downloadProvider.get().injectURL(LATEST_BUILD_URL))),
                    AuthlibInjectorVersionInfo.class);
        } catch (JsonParseException e) {
            throw new IOException("Malformed response", e);
        }
    }

    private Optional<AuthlibInjectorArtifactInfo> getLocalArtifact() {
        if (!Files.isRegularFile(artifactLocation)) {
            return Optional.empty();
        }
        try {
            return Optional.of(AuthlibInjectorArtifactInfo.from(artifactLocation));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Bad authlib-injector artifact", e);
            return Optional.empty();
        }
    }

    private static class AuthlibInjectorVersionInfo {
        @SerializedName("build_number")
        public int buildNumber;

        @SerializedName("version")
        public String version;

        @SerializedName("download_url")
        public String downloadUrl;

        @SerializedName("checksums")
        public Map<String, String> checksums;
    }

}
