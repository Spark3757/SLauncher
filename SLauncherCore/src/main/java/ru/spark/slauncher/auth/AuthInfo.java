package ru.spark.slauncher.auth;

import ru.spark.slauncher.game.Arguments;
import ru.spark.slauncher.util.Immutable;

import java.util.UUID;

/**
 * @author Spark1337
 */
@Immutable
public final class AuthInfo {

    private final String username;
    private final UUID uuid;
    private final String accessToken;
    private final String userProperties;
    private final Arguments arguments;

    public AuthInfo(String username, UUID uuid, String accessToken, String userProperties) {
        this(username, uuid, accessToken, userProperties, null);
    }

    public AuthInfo(String username, UUID uuid, String accessToken, String userProperties, Arguments arguments) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.userProperties = userProperties;
        this.arguments = arguments;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Properties of this user.
     * Don't know the difference between user properties and user property map.
     *
     * @return the user property map in JSON.
     */
    public String getUserProperties() {
        return userProperties;
    }

    /**
     * @return null if no argument is specified
     */
    public Arguments getArguments() {
        return arguments;
    }

    public AuthInfo withArguments(Arguments arguments) {
        return new AuthInfo(username, uuid, accessToken, userProperties, arguments);
    }
}
