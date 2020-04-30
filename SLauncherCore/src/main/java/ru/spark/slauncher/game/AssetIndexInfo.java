package ru.spark.slauncher.game;

import ru.spark.slauncher.util.Immutable;

/**
 * @author spark1337
 */
@Immutable
public class AssetIndexInfo extends IdDownloadInfo {

    private final long totalSize;

    public AssetIndexInfo() {
        this("", "");
    }

    public AssetIndexInfo(String id, String url) {
        this(id, url, null);
    }

    public AssetIndexInfo(String id, String url, String sha1) {
        this(id, url, sha1, 0);
    }

    public AssetIndexInfo(String id, String url, String sha1, int size) {
        this(id, url, sha1, size, 0);
    }

    public AssetIndexInfo(String id, String url, String sha1, int size, long totalSize) {
        super(id, url, sha1, size);
        this.totalSize = totalSize;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
