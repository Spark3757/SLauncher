package ru.spark.slauncher.download;

import ru.spark.slauncher.download.fabric.FabricVersionList;
import ru.spark.slauncher.download.forge.ForgeBMCLVersionList;
import ru.spark.slauncher.download.game.GameVersionList;
import ru.spark.slauncher.download.liteloader.LiteLoaderBMCLVersionList;
import ru.spark.slauncher.download.optifine.OptiFineBMCLVersionList;

/**
 * @author huang
 */
public class BMCLAPIDownloadProvider implements DownloadProvider {
    private final String apiRoot;
    private final GameVersionList game;
    private final FabricVersionList fabric;
    private final ForgeBMCLVersionList forge;
    private final LiteLoaderBMCLVersionList liteLoader;
    private final OptiFineBMCLVersionList optifine;

    public BMCLAPIDownloadProvider(String apiRoot) {
        this.apiRoot = apiRoot;
        this.game = new GameVersionList(this);
        this.fabric = new FabricVersionList(this);
        this.forge = new ForgeBMCLVersionList(apiRoot);
        this.liteLoader = new LiteLoaderBMCLVersionList(this);
        this.optifine = new OptiFineBMCLVersionList(apiRoot);
    }

    public String getApiRoot() {
        return apiRoot;
    }

    @Override
    public String getVersionListURL() {
        return apiRoot + "/mc/game/version_manifest.json";
    }

    @Override
    public String getAssetBaseURL() {
        return apiRoot + "/assets/";
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        switch (id) {
            case "game":
                return game;
            case "fabric":
                return fabric;
            case "forge":
                return forge;
            case "liteloader":
                return liteLoader;
            case "optifine":
                return optifine;
            default:
                throw new IllegalArgumentException("Unrecognized version list id: " + id);
        }
    }

    @Override
    public String injectURL(String baseURL) {
        return baseURL
                .replace("https://bmclapi2.bangbang93.com", apiRoot)
                .replace("https://launchermeta.mojang.com", apiRoot)
                .replace("https://launcher.mojang.com", apiRoot)
                .replace("https://libraries.minecraft.net", apiRoot + "/libraries")
                .replaceFirst("https?://files\\.minecraftforge\\.net/maven", apiRoot + "/maven")
                .replace("http://dl.liteloader.com/versions/versions.json", apiRoot + "/maven/com/mumfrey/liteloader/versions.json")
                .replace("http://dl.liteloader.com/versions", apiRoot + "/maven")
                .replace("https://meta.fabricmc.net", apiRoot + "/fabric-meta")
                .replace("https://maven.fabricmc.net", apiRoot + "/maven")
                .replace("https://authlib-injector.yushi.moe", apiRoot + "/mirrors/authlib-injector");
    }

    @Override
    public int getConcurrency() {
        return Math.max(Runtime.getRuntime().availableProcessors() * 2, 6);
    }
}
