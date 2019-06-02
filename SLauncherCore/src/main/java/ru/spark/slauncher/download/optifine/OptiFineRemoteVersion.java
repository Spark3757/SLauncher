package ru.spark.slauncher.download.optifine;

import ru.spark.slauncher.download.RemoteVersion;

import java.util.function.Supplier;

public class OptiFineRemoteVersion extends RemoteVersion {
    private final Supplier<String> url;

    public OptiFineRemoteVersion(String gameVersion, String selfVersion, Supplier<String> url, boolean snapshot) {
        super(gameVersion, selfVersion, "", snapshot ? Type.SNAPSHOT : Type.RELEASE);

        this.url = url;
    }

    @Override
    public String getUrl() {
        return url.get();
    }
}
