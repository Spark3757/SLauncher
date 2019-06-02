package ru.spark.slauncher.auth.authlibinjector;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;
import ru.spark.slauncher.auth.yggdrasil.CompleteGameProfile;
import ru.spark.slauncher.auth.yggdrasil.GameProfile;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilSession;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static ru.spark.slauncher.util.Lang.tryCast;

public class AuthlibInjectorAccountFactory extends AccountFactory<AuthlibInjectorAccount> {
    private AuthlibInjectorArtifactProvider downloader;
    private Function<String, AuthlibInjectorServer> serverLookup;

    /**
     * @param serverLookup a function that looks up {@link AuthlibInjectorServer} by url
     */
    public AuthlibInjectorAccountFactory(AuthlibInjectorArtifactProvider downloader, Function<String, AuthlibInjectorServer> serverLookup) {
        this.downloader = downloader;
        this.serverLookup = serverLookup;
    }

    @Override
    public AuthlibInjectorAccount create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        AuthlibInjectorServer server = (AuthlibInjectorServer) additionalData;

        return new AuthlibInjectorAccount(server, downloader, username, password, selector);
    }

    @Override
    public AuthlibInjectorAccount fromStorage(Map<Object, Object> storage) {
        Objects.requireNonNull(storage);

        YggdrasilSession session = YggdrasilSession.fromStorage(storage);

        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have username"));
        String apiRoot = tryCast(storage.get("serverBaseURL"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have API root."));

        AuthlibInjectorServer server = serverLookup.apply(apiRoot);

        tryCast(storage.get("profileProperties"), Map.class).ifPresent(
                it -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = it;
                    GameProfile selected = session.getSelectedProfile();
                    server.getYggdrasilService().getProfileRepository().put(selected.getId(), new CompleteGameProfile(selected, properties));
                });

        return new AuthlibInjectorAccount(server, downloader, username, session);
    }
}
