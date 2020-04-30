package ru.spark.slauncher.setting;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import ru.spark.slauncher.Metadata;
import ru.spark.slauncher.util.InvocationDispatcher;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.io.FileUtils;
import ru.spark.slauncher.util.platform.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ConfigHolder {

    public static final String CONFIG_FILENAME = Metadata.SL_DIRECTORY.toAbsolutePath() + "/slauncher.json";
    public static final String CONFIG_FILENAME_LINUX = Metadata.SL_DIRECTORY.toAbsolutePath() + "/.slauncher.json";
    private static Path configLocation;
    private static Config configInstance;
    private static boolean newlyCreated;
    private static InvocationDispatcher<String> configWriter = InvocationDispatcher.runOn(Lang::thread, content -> {
        try {
            writeToConfig(content);
        } catch (IOException e) {
            Logging.LOG.log(Level.SEVERE, "Failed to save config", e);
        }
    });

    private ConfigHolder() {
    }

    public static Config config() {
        if (configInstance == null) {
            throw new IllegalStateException("Configuration hasn't been loaded");
        }
        return configInstance;
    }

    public static boolean isNewlyCreated() {
        return newlyCreated;
    }

    public synchronized static void init() throws IOException {
        if (configInstance != null) {
            throw new IllegalStateException("Configuration is already loaded");
        }

        configLocation = locateConfig();
        configInstance = loadConfig();
        configInstance.addListener(source -> markConfigDirty());

        Settings.init();

        if (newlyCreated) {
            saveConfigSync();

            // hide the config file on windows
            if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
                try {
                    Files.setAttribute(configLocation, "dos:hidden", true);
                } catch (IOException e) {
                    Logging.LOG.log(Level.WARNING, "Failed to set hidden attribute of " + configLocation, e);
                }
            }
        }

        if (!Files.isWritable(configLocation)) {
            // the config cannot be saved
            // throw up the error now to prevent further data loss
            throw new IOException("Config at " + configLocation + " is not writable");
        }
    }

    private static Path locateConfig() {
        Path config = Paths.get(CONFIG_FILENAME);
        if (Files.isRegularFile(config))
            return config;

        Path dotConfig = Paths.get(CONFIG_FILENAME_LINUX);
        if (Files.isRegularFile(dotConfig))
            return dotConfig;

        // create new
        return OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS ? config : dotConfig;
    }

    private static Config loadConfig() throws IOException {
        if (Files.exists(configLocation)) {
            try {
                String content = FileUtils.readText(configLocation);
                Config deserialized = Config.fromJson(content);
                if (deserialized == null) {
                    Logging.LOG.info("Config is empty");
                } else {
                    Map<?, ?> raw = new Gson().fromJson(content, Map.class);
                    ConfigUpgrader.upgradeConfig(deserialized, raw);
                    return deserialized;
                }
            } catch (JsonParseException e) {
                Logging.LOG.log(Level.WARNING, "Malformed config.", e);
            }
        }

        Logging.LOG.info("Creating an empty config");
        newlyCreated = true;
        return new Config();
    }

    private static void writeToConfig(String content) throws IOException {
        Logging.LOG.info("Saving config");
        synchronized (configLocation) {
            Files.write(configLocation, content.getBytes(UTF_8));
        }
    }

    static void markConfigDirty() {
        configWriter.accept(configInstance.toJson());
    }

    private static void saveConfigSync() throws IOException {
        writeToConfig(configInstance.toJson());
    }
}
