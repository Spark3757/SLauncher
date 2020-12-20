package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.auth.yggdrasil.YggdrasilProvider;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.net.URL;
import java.util.UUID;

import static ru.spark.slauncher.util.io.NetworkUtils.toURL;

public class AuthlibInjectorProvider implements YggdrasilProvider {

    private final String apiRoot;

    public AuthlibInjectorProvider(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public URL getAuthenticationURL() {
        return toURL(apiRoot + "authserver/authenticate");
    }

    @Override
    public URL getRefreshmentURL() {
        return toURL(apiRoot + "authserver/refresh");
    }

    @Override
    public URL getValidationURL() {
        return toURL(apiRoot + "authserver/validate");
    }

    @Override
    public URL getInvalidationURL() {
        return toURL(apiRoot + "authserver/invalidate");
    }

    @Override
    public URL getSkinUploadURL(UUID uuid) throws UnsupportedOperationException {
        return toURL(apiRoot + "api/user/profile/" + UUIDTypeAdapter.fromUUID(uuid) + "/skin");
    }

    @Override
    public URL getProfilePropertiesURL(UUID uuid) {
        return toURL(apiRoot + "sessionserver/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(uuid));
    }

    public String toString() {
        return apiRoot;
    }
}
