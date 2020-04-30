package ru.spark.slauncher.setting;

import com.google.gson.JsonParseException;
import ru.spark.slauncher.auth.authlibinjector.AuthlibInjectorServer;
import ru.spark.slauncher.task.Schedulers;
import ru.spark.slauncher.task.Task;
import ru.spark.slauncher.util.Logging;
import ru.spark.slauncher.util.gson.JsonUtils;
import ru.spark.slauncher.util.gson.Validation;
import ru.spark.slauncher.util.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static ru.spark.slauncher.setting.ConfigHolder.config;

public class AuthlibInjectorServers implements Validation {

    public static final String CONFIG_FILENAME = "authlib-injectors.json";
    private static final Path configLocation = Paths.get(CONFIG_FILENAME);
    private static AuthlibInjectorServers configInstance;
    private final List<String> urls;

    public AuthlibInjectorServers(List<String> urls) {
        this.urls = urls;
    }

    public synchronized static void init() {
        if (configInstance != null) {
            throw new IllegalStateException("AuthlibInjectorServers is already loaded");
        }

        configInstance = new AuthlibInjectorServers(Collections.emptyList());

        if (Files.exists(configLocation)) {
            try {
                String content = FileUtils.readText(configLocation);
                configInstance = JsonUtils.GSON.fromJson(content, AuthlibInjectorServers.class);
            } catch (IOException | JsonParseException e) {
                Logging.LOG.log(Level.WARNING, "Malformed authlib-injectors.json", e);
            }
        }

        if (ConfigHolder.isNewlyCreated() && !AuthlibInjectorServers.getConfigInstance().getUrls().isEmpty()) {
            config().setPreferredLoginType(Accounts.getLoginType(Accounts.FACTORY_AUTHLIB_INJECTOR));
            for (String url : AuthlibInjectorServers.getConfigInstance().getUrls()) {
                Task.supplyAsync(Schedulers.io(), () -> AuthlibInjectorServer.locateServer(url))
                        .thenAcceptAsync(Schedulers.javafx(), server -> config().getAuthlibInjectorServers().add(server))
                        .start();
            }
        }
    }

    public static AuthlibInjectorServers getConfigInstance() {
        return configInstance;
    }

    public List<String> getUrls() {
        return urls;
    }

    @Override
    public void validate() throws JsonParseException {
        if (urls == null)
            throw new JsonParseException("authlib-injectors.json -> urls cannot be null");
    }
}
