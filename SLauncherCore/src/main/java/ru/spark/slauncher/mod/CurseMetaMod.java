package ru.spark.slauncher.mod;

import com.google.gson.annotations.SerializedName;
import ru.spark.slauncher.util.Immutable;

@Immutable
public final class CurseMetaMod {
    @SerializedName("Id")
    private final int id;

    @SerializedName("FileName")
    private final String fileName;

    @SerializedName("FileNameOnDisk")
    private final String fileNameOnDisk;

    @SerializedName("DownloadURL")
    private final String downloadURL;

    public CurseMetaMod() {
        this(0, "", "", "");
    }

    public CurseMetaMod(int id, String fileName, String fileNameOnDisk, String downloadURL) {
        this.id = id;
        this.fileName = fileName;
        this.fileNameOnDisk = fileNameOnDisk;
        this.downloadURL = downloadURL;
    }

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileNameOnDisk() {
        return fileNameOnDisk;
    }

    public String getDownloadURL() {
        return downloadURL;
    }
}
