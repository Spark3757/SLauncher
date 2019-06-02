package ru.spark.slauncher.download;

import ru.spark.slauncher.download.forge.ForgeBMCLVersionList;
import ru.spark.slauncher.download.game.GameVersionList;
import ru.spark.slauncher.download.liteloader.LiteLoaderVersionList;
import ru.spark.slauncher.download.optifine.OptiFineBMCLVersionList;

/**
 * @author Spark1337
 * @see <a href="http://wiki.vg">http://wiki.vg</a>
 */
public class MojangDownloadProvider implements DownloadProvider {

    @Override
    public String getVersionListURL() {
        return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    }

    @Override
    public String getAssetBaseURL() {
        return "https://resources.download.minecraft.net/";
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        switch (id) {
            case "game":
                return GameVersionList.INSTANCE;
            case "forge":
                return ForgeBMCLVersionList.INSTANCE;
            case "liteloader":
                return LiteLoaderVersionList.INSTANCE;
            case "optifine":
                return OptiFineBMCLVersionList.INSTANCE;
            default:
                throw new IllegalArgumentException("Unrecognized version list id: " + id);
        }
    }

    @Override
    public String injectURL(String baseURL) {
        return baseURL
                .replaceFirst("https?://files\\.minecraftforge\\.net/maven", "https://bmclapi2.bangbang93.com/maven");
    }
}
