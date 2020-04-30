package ru.spark.slauncher.download;

import ru.spark.slauncher.download.fabric.FabricVersionList;
import ru.spark.slauncher.download.forge.ForgeBMCLVersionList;
import ru.spark.slauncher.download.game.GameVersionList;
import ru.spark.slauncher.download.liteloader.LiteLoaderVersionList;
import ru.spark.slauncher.download.optifine.OptiFineBMCLVersionList;

/**
 * @author spark1337
 * @see <a href="http://wiki.vg">http://wiki.vg</a>
 */
public class MojangDownloadProvider implements DownloadProvider {
    private final GameVersionList game;
    private final FabricVersionList fabric;
    private final ForgeBMCLVersionList forge;
    private final LiteLoaderVersionList liteLoader;
    private final OptiFineBMCLVersionList optifine;

    public MojangDownloadProvider() {
        String apiRoot = "https://bmclapi2.bangbang93.com";

        this.game = new GameVersionList(this);
        this.fabric = new FabricVersionList(this);
        this.forge = new ForgeBMCLVersionList(apiRoot);
        this.liteLoader = new LiteLoaderVersionList(this);
        this.optifine = new OptiFineBMCLVersionList(apiRoot);
    }

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
        return baseURL;
    }

    @Override
    public int getConcurrency() {
        return 6;
    }
}
