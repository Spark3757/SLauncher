package ru.spark.slauncher.setting;

import javafx.beans.binding.Bindings;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.game.SLCacheRepository;
import ru.spark.slauncher.util.CacheRepository;
import ru.spark.slauncher.util.io.FileUtils;

import java.util.Locale;

import static ru.spark.slauncher.setting.ConfigHolder.config;

public class Settings {

    private static Settings instance;

    private Settings() {
        config().localizationProperty().addListener(unused -> updateSystemLocale());
        updateSystemLocale();

        DownloadProviders.init();
        ProxyManager.init();
        Accounts.init();
        Profiles.init();
        AuthlibInjectorServers.init();

        CacheRepository.setInstance(SLCacheRepository.REPOSITORY);
        SLCacheRepository.REPOSITORY.directoryProperty().bind(Bindings.createStringBinding(() -> {
            if (FileUtils.canCreateDirectory(getCommonDirectory())) {
                return getCommonDirectory();
            } else {
                return getDefaultCommonDirectory();
            }
        }, config().commonDirectoryProperty(), config().commonDirTypeProperty()));
    }

    public static Settings instance() {
        if (instance == null) {
            throw new IllegalStateException("Settings hasn't been initialized");
        }
        return instance;
    }

    /**
     * Should be called from {@link ConfigHolder#init()}.
     */
    static void init() {
        instance = new Settings();
    }

    public static String getDefaultCommonDirectory() {
        return Metadata.MINECRAFT_DIRECTORY.toString();
    }

    public String getCommonDirectory() {
        switch (config().getCommonDirType()) {
            case DEFAULT:
                return getDefaultCommonDirectory();
            case CUSTOM:
                return config().getCommonDirectory();
            default:
                return null;
        }
    }

    private static void updateSystemLocale() {
        Locale.setDefault(config().getLocalization().getLocale());
    }
}
