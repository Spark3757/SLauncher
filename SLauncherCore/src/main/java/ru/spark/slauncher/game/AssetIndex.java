package ru.spark.slauncher.game;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author spark1337
 */
public final class AssetIndex {

    @SerializedName("virtual")
    private final boolean virtual;

    @SerializedName("objects")
    private final Map<String, AssetObject> objects;

    public AssetIndex() {
        this(false, Collections.emptyMap());
    }

    public AssetIndex(boolean virtual, Map<String, AssetObject> objects) {
        this.virtual = virtual;
        this.objects = new HashMap<>(objects);
    }

    public boolean isVirtual() {
        return virtual;
    }

    public Map<String, AssetObject> getObjects() {
        return Collections.unmodifiableMap(objects);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("virtual", virtual).append("objects", objects).toString();
    }
}
