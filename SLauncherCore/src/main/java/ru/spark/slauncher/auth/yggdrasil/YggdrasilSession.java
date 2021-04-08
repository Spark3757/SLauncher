package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.Pair;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.util.*;
import java.util.stream.Collectors;

@Immutable
public class YggdrasilSession {

    private final String clientToken;
    private final String accessToken;
    private final GameProfile selectedProfile;
    private final List<GameProfile> availableProfiles;
    @Nullable
    private final Map<String, String> userProperties;

    public YggdrasilSession(String clientToken, String accessToken, GameProfile selectedProfile, List<GameProfile> availableProfiles, Map<String, String> userProperties) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
        this.selectedProfile = selectedProfile;
        this.availableProfiles = availableProfiles;
        this.userProperties = userProperties;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    /**
     * @return nullable (null if no character is selected)
     */
    public GameProfile getSelectedProfile() {
        return selectedProfile;
    }

    /**
     * @return nullable (null if the YggdrasilSession is loaded from storage)
     */
    public List<GameProfile> getAvailableProfiles() {
        return availableProfiles;
    }

    public static YggdrasilSession fromStorage(Map<?, ?> storage) {
        UUID uuid = Lang.tryCast(storage.get("uuid"), String.class).map(UUIDTypeAdapter::fromString).orElseThrow(() -> new IllegalArgumentException("uuid is missing"));
        String name = Lang.tryCast(storage.get("displayName"), String.class).orElseThrow(() -> new IllegalArgumentException("displayName is missing"));
        String clientToken = Lang.tryCast(storage.get("clientToken"), String.class).orElseThrow(() -> new IllegalArgumentException("clientToken is missing"));
        String accessToken = Lang.tryCast(storage.get("accessToken"), String.class).orElseThrow(() -> new IllegalArgumentException("accessToken is missing"));
        Map<String, String> userProperties = Lang.tryCast(storage.get("userProperties"), Map.class).orElse(null);
        return new YggdrasilSession(clientToken, accessToken, new GameProfile(uuid, name), null, userProperties);
    }

    public Map<Object, Object> toStorage() {
        if (selectedProfile == null)
            throw new IllegalStateException("No character is selected");

        return Lang.mapOf(
                Pair.pair("clientToken", clientToken),
                Pair.pair("accessToken", accessToken),
                Pair.pair("uuid", UUIDTypeAdapter.fromUUID(selectedProfile.getId())),
                Pair.pair("displayName", selectedProfile.getName()),
                Pair.pair("userProperties", userProperties));
    }

    public AuthInfo toAuthInfo() {
        if (selectedProfile == null)
            throw new IllegalStateException("No character is selected");

        return new AuthInfo(selectedProfile.getName(), selectedProfile.getId(), accessToken,
                Optional.ofNullable(userProperties)
                        .map(properties -> properties.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        e -> Collections.singleton(e.getValue()))))
                        .map(GSON_PROPERTIES::toJson).orElse("{}"));
    }

    private static final Gson GSON_PROPERTIES = new Gson();
}
