package ru.spark.slauncher.download;

/**
 * The service provider that provides Minecraft online file downloads.
 *
 * @author Spark1337
 */
public interface DownloadProvider {

    String getVersionListURL();

    String getAssetBaseURL();

    /**
     * Inject into original URL provided by Mojang and Forge.
     * <p>
     * Since there are many provided URLs that are written in JSONs and are unmodifiable,
     * this method provides a way to change them.
     *
     * @param baseURL original URL provided by Mojang and Forge.
     * @return the URL that is equivalent to [baseURL], but belongs to your own service provider.
     */
    String injectURL(String baseURL);

    /**
     * the specific version list that this download provider provides. i.e. "forge", "liteloader", "game", "optifine"
     *
     * @param id the id of specific version list that this download provider provides. i.e. "forge", "liteloader", "game", "optifine"
     * @return the version list
     */
    VersionList<?> getVersionListById(String id);
}
