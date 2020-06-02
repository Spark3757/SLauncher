package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilProvider;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class AuthlibInjectorProvider implements YggdrasilProvider {

    private final String apiRoot;

    public AuthlibInjectorProvider(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public URL getAuthenticationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/authenticate");
    }

    @Override
    public URL getRefreshmentURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/refresh");
    }

    @Override
    public URL getValidationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/validate");
    }

    @Override
    public URL getInvalidationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/invalidate");
    }

    @Override
    public URL getSkinUploadURL(UUID uuid) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getProfilePropertiesURL(UUID uuid) throws AuthenticationException {
        return toURL(apiRoot + "sessionserver/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(uuid));
    }

    @Override
    public String toString() {
        return apiRoot;
    }

    private URL toURL(String url) throws AuthenticationException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AuthenticationException(e);
        }
    }
}
