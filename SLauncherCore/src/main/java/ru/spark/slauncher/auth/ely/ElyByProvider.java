package ru.spark.slauncher.auth.ely;

import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.io.NetworkUtils;

import java.net.URL;
import java.util.UUID;

public class ElyByProvider implements ElyProvider {

    @Override
    public URL getAuthenticationURL() {
        return NetworkUtils.toURL("https://authserver.ely.by/auth/authenticate");
    }

    @Override
    public URL getRefreshmentURL() {
        return NetworkUtils.toURL("https://authserver.ely.by/auth/refresh");
    }

    @Override
    public URL getValidationURL() {
        return NetworkUtils.toURL("https://authserver.ely.by/auth/validate");
    }

    @Override
    public URL getInvalidationURL() {
        return NetworkUtils.toURL("https://authserver.ely.by/auth/invalidate");
    }

    @Override
    public URL getProfilePropertiesURL(UUID uuid) {
        return NetworkUtils.toURL("https://authserver.ely.by/session/profile/" + UUIDTypeAdapter.fromUUID(uuid));
    }

    @Override
    public String toString() {
        return "ely";
    }
}
