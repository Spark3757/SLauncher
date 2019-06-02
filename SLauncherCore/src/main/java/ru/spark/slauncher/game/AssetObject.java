package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.Validation;

/**
 * @author Spark1337
 */
public final class AssetObject implements Validation {

    private final String hash;
    private final long size;

    public AssetObject() {
        this("", 0);
    }

    public AssetObject(String hash, long size) {
        this.hash = hash;
        this.size = size;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public String getLocation() {
        return hash.substring(0, 2) + "/" + hash;
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(hash) || hash.length() < 2)
            throw new IllegalStateException("AssetObject hash cannot be blank.");
    }
}
