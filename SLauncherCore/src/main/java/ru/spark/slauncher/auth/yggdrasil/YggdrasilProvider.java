package ru.spark.slauncher.auth.yggdrasil;

import ru.spark.slauncher.auth.AuthenticationException;

import java.net.URL;
import java.util.UUID;

/**
 * @see <a href="http://wiki.vg">http://wiki.vg</a>
 */
public interface YggdrasilProvider {

    URL getAuthenticationURL() throws AuthenticationException;

    URL getRefreshmentURL() throws AuthenticationException;

    URL getValidationURL() throws AuthenticationException;

    URL getInvalidationURL() throws AuthenticationException;

    URL getProfilePropertiesURL(UUID uuid) throws AuthenticationException;

}
