package ru.spark.slauncher.auth.ely;

import java.net.URL;
import java.util.UUID;

/**
 * @see <a href="https://docs.ely.by/ru/minecraft-auth.html">https://docs.ely.by/ru/minecraft-auth.html</a>
 */
public interface ElyProvider {

    URL getAuthenticationURL();

    URL getRefreshmentURL();

    URL getValidationURL();

    URL getInvalidationURL();

    URL getProfilePropertiesURL(UUID uuid);

}
