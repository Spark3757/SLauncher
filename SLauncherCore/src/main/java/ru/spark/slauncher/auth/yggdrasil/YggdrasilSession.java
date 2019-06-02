package ru.spark.slauncher.auth.yggdrasil;

import com.google.gson.Gson;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.util.Immutable;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.spark.slauncher.util.Lang.mapOf;
import static ru.spark.slauncher.util.Lang.tryCast;
import static ru.spark.slauncher.util.Pair.pair;

@Immutable
public class YggdrasilSession {

    private static final Gson GSON_PROPERTIES = new Gson();
    private final String clientToken;
    private final String accessToken;
    private final GameProfile selectedProfile;
    private final List<GameProfile> availableProfiles;
    private final User user;

    public YggdrasilSession(String clientToken, String accessToken, GameProfile selectedProfile, List<GameProfile> availableProfiles, User user) {
        this.clientToken = clientToken;
        this.accessToken = accessToken;
        this.selectedProfile = selectedProfile;
        this.availableProfiles = availableProfiles;
        this.user = user;
    }

    public static YggdrasilSession fromStorage(Map<?, ?> storage) {
        UUID uuid = tryCast(storage.get("uuid"), String.class).map(UUIDTypeAdapter::fromString).orElseThrow(() -> new IllegalArgumentException("uuid is missing"));
        String name = tryCast(storage.get("displayName"), String.class).orElseThrow(() -> new IllegalArgumentException("displayName is missing"));
        String clientToken = tryCast(storage.get("clientToken"), String.class).orElseThrow(() -> new IllegalArgumentException("clientToken is missing"));
        String accessToken = tryCast(storage.get("accessToken"), String.class).orElseThrow(() -> new IllegalArgumentException("accessToken is missing"));
        String userId = tryCast(storage.get("userid"), String.class).orElseThrow(() -> new IllegalArgumentException("userid is missing"));
        Map<String, String> userProperties = tryCast(storage.get("userProperties"), Map.class).orElse(null);
        return new YggdrasilSession(clientToken, accessToken, new GameProfile(uuid, name), null, new User(userId, userProperties));
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return nullable (null if no character is selected)
     */
    public GameProfile getSelectedProfile() {
        return selectedProfile;
    }

    /**
     * @return nullable (null if the ElySession is loaded from storage)
     */
    public List<GameProfile> getAvailableProfiles() {
        return availableProfiles;
    }

    public User getUser() {
        return user;
    }

    public Map<Object, Object> toStorage() {
        if (selectedProfile == null)
            throw new IllegalStateException("No character is selected");
        if (user == null)
            throw new IllegalStateException("No user is specified");

        return mapOf(
                pair("clientToken", clientToken),
                pair("accessToken", accessToken),
                pair("uuid", UUIDTypeAdapter.fromUUID(selectedProfile.getId())),
                pair("displayName", selectedProfile.getName()),
                pair("userid", user.getId()),
                pair("userProperties", user.getProperties()));
    }

    public AuthInfo toAuthInfo() {
        if (selectedProfile == null)
            throw new IllegalStateException("No character is selected");
        if (user == null)
            throw new IllegalStateException("No user is specified");

        return new AuthInfo(selectedProfile.getName(), selectedProfile.getId(), accessToken,
                Optional.ofNullable(user.getProperties()).map(GSON_PROPERTIES::toJson).orElse("{}"));
    }
}
