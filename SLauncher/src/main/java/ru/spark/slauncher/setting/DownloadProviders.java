package ru.spark.slauncher.setting;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import ru.spark.slauncher.download.BMCLAPIDownloadProvider;
import ru.spark.slauncher.download.DownloadProvider;
import ru.spark.slauncher.download.MojangDownloadProvider;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Pair;

import java.util.Map;
import java.util.Optional;

public final class DownloadProviders {
    public static final Map<String, DownloadProvider> providersById = Lang.mapOf(
            Pair.pair("mojang", new MojangDownloadProvider()),
            Pair.pair("bmclapi", new BMCLAPIDownloadProvider()));
    public static final String DEFAULT_PROVIDER_ID = "bmclapi";
    private static ObjectBinding<DownloadProvider> downloadProviderProperty;

    private DownloadProviders() {
    }

    static void init() {
        downloadProviderProperty = Bindings.createObjectBinding(
                () -> Optional.ofNullable(providersById.get(ConfigHolder.config().getDownloadType()))
                        .orElse(providersById.get(DEFAULT_PROVIDER_ID)),
                ConfigHolder.config().downloadTypeProperty());
    }

    public static DownloadProvider getDownloadProvider() {
        return downloadProviderProperty.get();
    }

    public static ObservableObjectValue<DownloadProvider> downloadProviderProperty() {
        return downloadProviderProperty;
    }
}
