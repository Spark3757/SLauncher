package ru.spark.slauncher.setting;

import ru.spark.slauncher.download.AdaptedDownloadProvider;
import ru.spark.slauncher.download.BMCLAPIDownloadProvider;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.MojangDownloadProvider;
import ru.spark.slauncher.ui.FXUtils;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.spark.slauncher.setting.ConfigHolder.config;
import static ru.spark.slauncher.util.Lang.mapOf;
import static ru.spark.slauncher.util.Pair.pair;

public final class DownloadProviders {
    public static final Map<String, DownloadProvider> providersById;
    public static final String DEFAULT_PROVIDER_ID = "mojang";
    private static final AdaptedDownloadProvider DOWNLOAD_PROVIDER = new AdaptedDownloadProvider();

    static {
        String bmclapiRoot = "https://bmclapi2.bangbang93.com";
        String bmclapiRootOverride = System.getProperty("slauncher.bmclapi.override");
        if (bmclapiRootOverride != null) bmclapiRoot = bmclapiRootOverride;

        providersById = mapOf(
                pair("mojang", new MojangDownloadProvider()),
                pair("bmclapi", new BMCLAPIDownloadProvider(bmclapiRoot)),
                pair("mcbbs", new BMCLAPIDownloadProvider("https://download.mcbbs.net")));
    }

    private DownloadProviders() {
    }

    static void init() {
        FXUtils.onChangeAndOperate(config().downloadTypeProperty(), downloadType -> {
            DownloadProvider primary = Optional.ofNullable(providersById.get(config().getDownloadType()))
                    .orElse(providersById.get(DEFAULT_PROVIDER_ID));
            DOWNLOAD_PROVIDER.setDownloadProviderCandidates(
                    Stream.concat(
                            Stream.of(primary),
                            providersById.values().stream().filter(x -> x != primary)
                    ).collect(Collectors.toList())
            );
        });
    }

    public static String getPrimaryDownloadProviderId() {
        String downloadType = config().getDownloadType();
        if (providersById.containsKey(downloadType))
            return downloadType;
        else
            return DEFAULT_PROVIDER_ID;
    }

    public static AdaptedDownloadProvider getDownloadProviderByPrimaryId(String primaryId) {
        AdaptedDownloadProvider adaptedDownloadProvider = new AdaptedDownloadProvider();
        DownloadProvider primary = Optional.ofNullable(providersById.get(primaryId))
                .orElse(providersById.get(DEFAULT_PROVIDER_ID));
        adaptedDownloadProvider.setDownloadProviderCandidates(
                Stream.concat(
                        Stream.of(primary),
                        providersById.values().stream().filter(x -> x != primary)
                ).collect(Collectors.toList())
        );
        return adaptedDownloadProvider;
    }

    /**
     * Get current primary preferred download provider
     */
    public static AdaptedDownloadProvider getDownloadProvider() {
        return DOWNLOAD_PROVIDER;
    }
}
