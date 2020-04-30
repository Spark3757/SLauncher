package ru.spark.slauncher.mod;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author spark1337
 */
public final class Modpack {
    private final String name;
    private final String author;
    private final String version;
    private final String gameVersion;
    private final String description;
    private final transient Charset encoding;
    private final Object manifest;

    public Modpack() {
        this("", null, null, null, null, null, null);
    }

    public Modpack(String name, String author, String version, String gameVersion, String description, Charset encoding, Object manifest) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.gameVersion = gameVersion;
        this.description = description;
        this.encoding = encoding;
        this.manifest = manifest;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public Modpack setGameVersion(String gameVersion) {
        return new Modpack(name, author, version, gameVersion, description, encoding, manifest);
    }

    public String getDescription() {
        return description;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public Modpack setEncoding(Charset encoding) {
        return new Modpack(name, author, version, gameVersion, description, encoding, manifest);
    }

    public Object getManifest() {
        return manifest;
    }

    public Modpack setManifest(Object manifest) {
        return new Modpack(name, author, version, gameVersion, description, encoding, manifest);
    }

    public static boolean acceptFile(String path, List<String> blackList, List<String> whiteList) {
        if (path.isEmpty())
            return true;
        for (String s : blackList)
            if (path.equals(s))
                return false;
        if (whiteList == null || whiteList.isEmpty())
            return true;
        for (String s : whiteList)
            if (path.equals(s))
                return true;
        return false;
    }
}
