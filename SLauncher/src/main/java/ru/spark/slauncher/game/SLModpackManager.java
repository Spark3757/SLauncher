package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author spark1337
 */
public final class SLModpackManager {

    /**
     * Read the manifest in a SLauncher modpack.
     *
     * @param file     a SLauncher modpack file.
     * @param encoding encoding of modpack zip file.
     * @return the manifest of SLauncher modpack.
     * @throws IOException        if the file is not a valid zip file.
     * @throws JsonParseException if the manifest.json is missing or malformed.
     */
    public static Modpack readSLModpackManifest(Path file, Charset encoding) throws IOException, JsonParseException {
        String manifestJson = CompressingUtils.readTextZipEntry(file, "modpack.json", encoding);
        Modpack manifest = JsonUtils.fromNonNullJson(manifestJson, Modpack.class).setEncoding(encoding);
        String gameJson = CompressingUtils.readTextZipEntry(file, "minecraft/pack.json", encoding);
        Version game = JsonUtils.fromNonNullJson(gameJson, Version.class);
        if (game.getJar() == null)
            if (StringUtils.isBlank(manifest.getVersion()))
                throw new JsonParseException("Cannot recognize the game version of modpack " + file + ".");
            else
                return manifest.setManifest(SLModpackManifest.INSTANCE);
        else
            return manifest.setManifest(SLModpackManifest.INSTANCE).setGameVersion(game.getJar());
    }
}
