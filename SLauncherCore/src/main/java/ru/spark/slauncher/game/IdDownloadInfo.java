package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.TolerableValidationException;

/**
 * @author Spark1337
 */
@Immutable
public class IdDownloadInfo extends DownloadInfo {

    @SerializedName("id")
    private final String id;

    public IdDownloadInfo() {
        this("", "");
    }

    public IdDownloadInfo(String id, String url) {
        this(id, url, null);
    }

    public IdDownloadInfo(String id, String url, String sha1) {
        this(id, url, sha1, 0);
    }

    public IdDownloadInfo(String id, String url, String sha1, int size) {
        super(url, sha1, size);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        super.validate();

        if (StringUtils.isBlank(id))
            throw new JsonParseException("IdDownloadInfo id can not be null");
    }

}
