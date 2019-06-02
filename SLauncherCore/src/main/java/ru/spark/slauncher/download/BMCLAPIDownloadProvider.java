package ru.spark.slauncher.download;

import ru.spark.slauncher.download.forge.ForgeBMCLVersionList;
import ru.spark.slauncher.download.game.GameVersionList;
import ru.spark.slauncher.download.liteloader.LiteLoaderBMCLVersionList;
import ru.spark.slauncher.download.optifine.OptiFineBMCLVersionList;

/**
 * @author huang
 */
public class BMCLAPIDownloadProvider implements DownloadProvider {

    @Override
    public String getVersionListURL() {
        return "https://bmclapi2.bangbang93.com/mc/game/version_manifest.json";
    }

    @Override
    public String getAssetBaseURL() {
        return "https://bmclapi2.bangbang93.com/assets/";
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        switch (id) {
            case "game":
                return GameVersionList.INSTANCE;
            case "forge":
                return ForgeBMCLVersionList.INSTANCE;
            case "liteloader":
                return LiteLoaderBMCLVersionList.INSTANCE;
            case "optifine":
                return OptiFineBMCLVersionList.INSTANCE;
            default:
                throw new IllegalArgumentException("Unrecognized version list id: " + id);
        }
    }

    @Override
    public String injectURL(String baseURL) {
        return baseURL
                .replace("https://launchermeta.mojang.com", "https://bmclapi2.bangbang93.com")
                .replace("https://launcher.mojang.com", "https://bmclapi2.bangbang93.com")
                .replace("https://libraries.minecraft.net", "https://bmclapi2.bangbang93.com/libraries")
                .replaceFirst("https?://files\\.minecraftforge\\.net/maven", "https://bmclapi2.bangbang93.com/maven")
                .replace("http://dl.liteloader.com/versions/versions.json", "https://bmclapi2.bangbang93.com/maven/com/mumfrey/liteloader/versions.json")
                .replace("http://dl.liteloader.com/versions", "https://bmclapi2.bangbang93.com/maven")
                .replace("https://authlib-injector.yushi.moe", "https://bmclapi2.bangbang93.com/mirrors/authlib-injector");
    }

}
