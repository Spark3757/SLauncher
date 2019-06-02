package ru.spark.slauncher.game;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.mod.Modpack;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.io.CompressingUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Spark1337
 */
public final class SLauncherModpackManager {

    public static final List<String> MODPACK_BLACK_LIST = Lang.immutableListOf(
            "usernamecache.json", "usercache.json", // Minecraft
            "launcher_profiles.json", "launcher.pack.lzma", // Minecraft Launcher
            "pack.json", "launcher.jar", "slaunchermc.log", "cache", // SLauncher
            "manifest.json", "minecraftinstance.json", ".curseclient", // Curse
            "minetweaker.log", // Mods
            "jars", "logs", "versions", "assets", "libraries", "crash-reports", "NVIDIA", "AMD", "screenshots", "natives", "native", "$native", "server-resource-packs", // Minecraft
            "downloads", // Curse
            "asm", "backups", "TCNodeTracker", "CustomDISkins", "data" // Mods
    );
    public static final List<String> MODPACK_SUGGESTED_BLACK_LIST = Lang.immutableListOf(
            "fonts", // BetterFonts
            "saves", "servers.dat", "options.txt", // Minecraft
            "blueprints" /* BuildCraft */,
            "optionsof.txt" /* OptiFine */,
            "journeymap" /* JourneyMap */,
            "optionsshaders.txt",
            "mods/VoxelMods");

    public static ModAdviser.ModSuggestion suggestMod(String fileName, boolean isDirectory) {
        if (match(MODPACK_BLACK_LIST, fileName, isDirectory))
            return ModAdviser.ModSuggestion.HIDDEN;
        if (match(MODPACK_SUGGESTED_BLACK_LIST, fileName, isDirectory))
            return ModAdviser.ModSuggestion.NORMAL;
        else
            return ModAdviser.ModSuggestion.SUGGESTED;
    }

    private static boolean match(List<String> l, String fileName, boolean isDirectory) {
        for (String s : l)
            if (isDirectory) {
                if (fileName.startsWith(s + "/"))
                    return true;
            } else if (fileName.equals(s))
                return true;
        return false;
    }

    /**
     * Read the manifest in a SLauncher modpack.
     *
     * @param file     a SLauncher modpack file.
     * @param encoding encoding of modpack zip file.
     * @return the manifest of SLauncher modpack.
     * @throws IOException        if the file is not a valid zip file.
     * @throws JsonParseException if the manifest.json is missing or malformed.
     */
    public static Modpack readSLauncherModpackManifest(Path file, Charset encoding) throws IOException, JsonParseException {
        String manifestJson = CompressingUtils.readTextZipEntry(file, "modpack.json", encoding);
        Modpack manifest = JsonUtils.fromNonNullJson(manifestJson, Modpack.class).setEncoding(encoding);
        String gameJson = CompressingUtils.readTextZipEntry(file, "minecraft/pack.json", encoding);
        Version game = JsonUtils.fromNonNullJson(gameJson, Version.class);
        if (game.getJar() == null)
            if (StringUtils.isBlank(manifest.getVersion()))
                throw new JsonParseException("Cannot recognize the game version of modpack " + file + ".");
            else
                return manifest.setManifest(SLauncherModpackManifest.INSTANCE);
        else
            return manifest.setManifest(SLauncherModpackManifest.INSTANCE).setGameVersion(game.getJar());
    }
}
