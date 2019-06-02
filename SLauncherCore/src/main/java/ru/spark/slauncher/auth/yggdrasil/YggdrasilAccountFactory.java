package ru.spark.slauncher.auth.yggdrasil;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;

import java.util.Map;
import java.util.Objects;

import static ru.spark.slauncher.util.Lang.tryCast;

/**
 * @author Spark1337
 */
public class YggdrasilAccountFactory extends AccountFactory<YggdrasilAccount> {

    public static final YggdrasilAccountFactory MOJANG = new YggdrasilAccountFactory(YggdrasilService.MOJANG);

    private YggdrasilService service;

    public YggdrasilAccountFactory(YggdrasilService service) {
        this.service = service;
    }

    @Override
    public YggdrasilAccount create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        return new YggdrasilAccount(service, username, password, selector);
    }

    @Override
    public YggdrasilAccount fromStorage(Map<Object, Object> storage) {
        Objects.requireNonNull(storage);

        YggdrasilSession session = YggdrasilSession.fromStorage(storage);

        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have username"));

        tryCast(storage.get("profileProperties"), Map.class).ifPresent(
                it -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = it;
                    GameProfile selected = session.getSelectedProfile();
                    service.getProfileRepository().put(selected.getId(), new CompleteGameProfile(selected, properties));
                });

        return new YggdrasilAccount(service, username, session);
    }
}
