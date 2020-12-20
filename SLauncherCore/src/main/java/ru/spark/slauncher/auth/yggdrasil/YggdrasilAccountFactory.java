package ru.spark.slauncher.auth.yggdrasil;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;
import ru.spark.slauncher.util.Lang;
import ru.spark.slauncher.util.javafx.ObservableOptionalCache;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author spark1337
 */
public class YggdrasilAccountFactory extends AccountFactory<YggdrasilAccount> {

    public static final YggdrasilAccountFactory MOJANG = new YggdrasilAccountFactory(YggdrasilService.MOJANG);

    private final YggdrasilService service;

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

        String username = Lang.tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have username"));

        Lang.tryCast(storage.get("profileProperties"), Map.class).ifPresent(
                it -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = it;
                    GameProfile selected = session.getSelectedProfile();
                    ObservableOptionalCache<UUID, CompleteGameProfile, AuthenticationException> profileRepository = service.getProfileRepository();
                    profileRepository.put(selected.getId(), new CompleteGameProfile(selected, properties));
                    profileRepository.invalidate(selected.getId());
                });

        return new YggdrasilAccount(service, username, session);
    }

    @Override
    public AccountLoginType getLoginType() {
        return AccountLoginType.USERNAME_PASSWORD;
    }
}
