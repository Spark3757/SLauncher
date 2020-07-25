package ru.spark.slauncher.upgrade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ru.spark.slauncher.task.FileDownloadTask;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.NetworkUtils;
import ru.spark.slauncher.util.platform.SystemUtils;

import java.io.IOException;
import java.util.Optional;

public class RemoteVersion {

    private String version;
    private String url;
    private Type type;
    private FileDownloadTask.IntegrityCheck integrityCheck;

    public RemoteVersion(String version, String url, Type type, FileDownloadTask.IntegrityCheck integrityCheck) {
        this.version = version;
        this.url = url;
        this.type = type;
        this.integrityCheck = integrityCheck;
    }

    public static RemoteVersion fetch(String url) throws IOException {
        try {
            JsonObject response = JsonUtils.fromNonNullJson(NetworkUtils.doGet(NetworkUtils.toURL(url)), JsonObject.class);
            String version = Optional.ofNullable(response.get("version")).map(JsonElement::getAsString).orElseThrow(() -> new IOException("version is missing"));
            String jarUrl = Optional.ofNullable(response.get("jar")).map(JsonElement::getAsString).orElse(null);
            String jarHash = Optional.ofNullable(response.get("jarsha1")).map(JsonElement::getAsString).orElse(null);
            String packXZUrl = Optional.ofNullable(response.get("packxz")).map(JsonElement::getAsString).orElse(null);
            String packXZHash = Optional.ofNullable(response.get("packxzsha1")).map(JsonElement::getAsString).orElse(null);
            if (SystemUtils.JRE_CAPABILITY_PACK200 && packXZUrl != null && packXZHash != null) {
                return new RemoteVersion(version, packXZUrl, Type.PACK_XZ, new FileDownloadTask.IntegrityCheck("SHA-1", packXZHash));
            } else if (jarUrl != null && jarHash != null) {
                return new RemoteVersion(version, jarUrl, Type.JAR, new FileDownloadTask.IntegrityCheck("SHA-1", jarHash));
            } else {
                throw new IOException("No download url is available");
            }
        } catch (JsonParseException e) {
            throw new IOException("Malformed response", e);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    public FileDownloadTask.IntegrityCheck getIntegrityCheck() {
        return integrityCheck;
    }

    @Override
    public String toString() {
        return "[" + version + " from " + url + "]";
    }

    public enum Type {
        PACK_XZ,
        JAR
    }
}
