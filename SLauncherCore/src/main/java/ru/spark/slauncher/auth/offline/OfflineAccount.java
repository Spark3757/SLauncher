package ru.spark.slauncher.auth.offline;

import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.util.StringUtils;
import ru.spark.slauncher.util.ToStringBuilder;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static ru.spark.slauncher.util.Lang.mapOf;
import static ru.spark.slauncher.util.Pair.pair;

/**
 * @author huang
 */
public class OfflineAccount extends Account {

    private final String username;
    private final UUID uuid;

    protected OfflineAccount(String username, UUID uuid) {
        this.username = requireNonNull(username);
        this.uuid = requireNonNull(uuid);

        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getCharacter() {
        return username;
    }

    @Override
    public AuthInfo logIn() {
        return new AuthInfo(username, uuid, UUIDTypeAdapter.fromUUID(UUID.randomUUID()), "{}");
    }

    @Override
    public AuthInfo logInWithPassword(String password) throws AuthenticationException {
        return logIn();
    }

    @Override
    public Optional<AuthInfo> playOffline() {
        return Optional.of(logIn());
    }

    @Override
    public Map<Object, Object> toStorage() {
        return mapOf(
                pair("uuid", UUIDTypeAdapter.fromUUID(uuid)),
                pair("username", username)
        );
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", username)
                .append("uuid", uuid)
                .toString();
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfflineAccount))
            return false;
        OfflineAccount another = (OfflineAccount) obj;
        return username.equals(another.username);
    }
}
