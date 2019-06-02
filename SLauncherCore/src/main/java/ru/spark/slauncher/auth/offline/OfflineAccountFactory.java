package ru.spark.slauncher.auth.offline;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.CharacterSelector;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.spark.slauncher.util.Lang.tryCast;

/**
 * @author Spark1337
 */
public class OfflineAccountFactory extends AccountFactory<OfflineAccount> {
    public static final OfflineAccountFactory INSTANCE = new OfflineAccountFactory();

    private OfflineAccountFactory() {
    }

    private static UUID getUUIDFromUserName(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(UTF_8));
    }

    public OfflineAccount create(String username, UUID uuid) {
        return new OfflineAccount(username, uuid);
    }

    @Override
    public OfflineAccount create(CharacterSelector selector, String username, String password, Object additionalData) {
        return new OfflineAccount(username, getUUIDFromUserName(username));
    }

    @Override
    public OfflineAccount fromStorage(Map<Object, Object> storage) {
        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalStateException("Offline account configuration malformed."));
        UUID uuid = tryCast(storage.get("uuid"), String.class)
                .map(UUIDTypeAdapter::fromString)
                .orElse(getUUIDFromUserName(username));

        return new OfflineAccount(username, uuid);
    }

}
