package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.auth.yggdrasil.YggdrasilProvider;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.net.URL;
import java.util.UUID;

public class AuthlibInjectorProvider implements YggdrasilProvider {

    private final String apiRoot;

    public AuthlibInjectorProvider(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public URL getAuthenticationURL() {
        return NetworkUtils.toURL(apiRoot + "authserver/authenticate");
    }

    @Override
    public URL getRefreshmentURL() {
        return NetworkUtils.toURL(apiRoot + "authserver/refresh");
    }

    @Override
    public URL getValidationURL() {
        return NetworkUtils.toURL(apiRoot + "authserver/validate");
    }

    @Override
    public URL getInvalidationURL() {
        return NetworkUtils.toURL(apiRoot + "authserver/invalidate");
    }

    @Override
    public URL getProfilePropertiesURL(UUID uuid) {
        return NetworkUtils.toURL(apiRoot + "sessionserver/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(uuid));
    }

    @Override
    public String toString() {
        return apiRoot;
    }
}
