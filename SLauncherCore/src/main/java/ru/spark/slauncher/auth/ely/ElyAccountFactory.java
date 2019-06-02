package ru.spark.slauncher.auth.ely;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;

import java.util.Map;
import java.util.Objects;

import static ru.spark.slauncher.util.Lang.tryCast;

/**
 * @author Spark1337
 */
public class ElyAccountFactory extends AccountFactory<ElyAccount> {

    public static final ElyAccountFactory ELY = new ElyAccountFactory(ElyService.ELY);

    private ElyService service;

    public ElyAccountFactory(ElyService service) {
        this.service = service;
    }

    @Override
    public ElyAccount create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        return new ElyAccount(service, username, password, selector);
    }

    @Override
    public ElyAccount fromStorage(Map<Object, Object> storage) {
        Objects.requireNonNull(storage);

        ElySession session = ElySession.fromStorage(storage);

        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have username"));

        tryCast(storage.get("profileProperties"), Map.class).ifPresent(
                it -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = it;
                    GameProfile selected = session.getSelectedProfile();
                    service.getProfileRepository().put(selected.getId(), new CompleteGameProfile(selected, properties));
                });

        return new ElyAccount(service, username, session);
    }
}
