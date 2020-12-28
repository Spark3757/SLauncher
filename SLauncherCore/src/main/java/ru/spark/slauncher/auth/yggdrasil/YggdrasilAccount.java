package ru.spark.slauncher.auth.yggdrasil;

import javafx.beans.binding.ObjectBinding;
import ru.spark.slauncher.auth.*;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;
import ru.spark.slauncher.util.javafx.BindingMapping;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;
import static ru.spark.slauncher.util.Logging.LOG;

public class YggdrasilAccount extends Account {

    protected final YggdrasilService service;
    protected final UUID characterUUID;
    protected final String username;

    private boolean authenticated = false;
    private YggdrasilSession session;

    protected YggdrasilAccount(YggdrasilService service, String username, YggdrasilSession session) {
        this.service = requireNonNull(service);
        this.username = requireNonNull(username);
        this.characterUUID = requireNonNull(session.getSelectedProfile().getId());
        this.session = requireNonNull(session);

        addProfilePropertiesListener();
    }

    protected YggdrasilAccount(YggdrasilService service, String username, String password, CharacterSelector selector) throws AuthenticationException {
        this.service = requireNonNull(service);
        this.username = requireNonNull(username);

        YggdrasilSession acquiredSession = service.authenticate(username, password, randomClientToken());
        if (acquiredSession.getSelectedProfile() == null) {
            if (acquiredSession.getAvailableProfiles() == null || acquiredSession.getAvailableProfiles().isEmpty()) {
                throw new NoCharacterException();
            }

            GameProfile characterToSelect = selector.select(service, acquiredSession.getAvailableProfiles());

            session = service.refresh(
                    acquiredSession.getAccessToken(),
                    acquiredSession.getClientToken(),
                    characterToSelect);
            // response validity has been checked in refresh()
        } else {
            session = acquiredSession;
        }

        characterUUID = session.getSelectedProfile().getId();
        authenticated = true;

        addProfilePropertiesListener();
    }

    private ObjectBinding<Optional<CompleteGameProfile>> profilePropertiesBinding;

    private void addProfilePropertiesListener() {
        // binding() is thread-safe
        // hold the binding so that it won't be garbage-collected
        profilePropertiesBinding = service.getProfileRepository().binding(characterUUID, true);
        // and it's safe to add a listener to an ObjectBinding which does not have any listener attached before (maybe tricky)
        profilePropertiesBinding.addListener((a, b, c) -> this.invalidate());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getCharacter() {
        return session.getSelectedProfile().getName();
    }

    @Override
    public UUID getUUID() {
        return session.getSelectedProfile().getId();
    }

    @Override
    public synchronized AuthInfo logIn() throws AuthenticationException {
        if (!authenticated) {
            if (service.validate(session.getAccessToken(), session.getClientToken())) {
                authenticated = true;
            } else {
                YggdrasilSession acquiredSession;
                try {
                    acquiredSession = service.refresh(session.getAccessToken(), session.getClientToken(), null);
                } catch (RemoteAuthenticationException e) {
                    if ("ForbiddenOperationException".equals(e.getRemoteName())) {
                        throw new CredentialExpiredException(e);
                    } else {
                        throw e;
                    }
                }
                if (acquiredSession.getSelectedProfile() == null ||
                        !acquiredSession.getSelectedProfile().getId().equals(characterUUID)) {
                    throw new ServerResponseMalformedException("Selected profile changed");
                }

                session = acquiredSession;

                authenticated = true;
                invalidate();
            }
        }

        return session.toAuthInfo();
    }

    @Override
    public synchronized AuthInfo logInWithPassword(String password) throws AuthenticationException {
        YggdrasilSession acquiredSession = service.authenticate(username, password, randomClientToken());

        if (acquiredSession.getSelectedProfile() == null) {
            if (acquiredSession.getAvailableProfiles() == null || acquiredSession.getAvailableProfiles().isEmpty()) {
                throw new CharacterDeletedException();
            }

            GameProfile characterToSelect = acquiredSession.getAvailableProfiles().stream()
                    .filter(charatcer -> charatcer.getId().equals(characterUUID))
                    .findFirst()
                    .orElseThrow(CharacterDeletedException::new);

            session = service.refresh(
                    acquiredSession.getAccessToken(),
                    acquiredSession.getClientToken(),
                    characterToSelect);

        } else {
            if (!acquiredSession.getSelectedProfile().getId().equals(characterUUID)) {
                throw new CharacterDeletedException();
            }
            session = acquiredSession;
        }

        authenticated = true;
        invalidate();
        return session.toAuthInfo();
    }

    @Override
    public Optional<AuthInfo> playOffline() {
        return Optional.of(session.toAuthInfo());
    }

    @Override
    public Map<Object, Object> toStorage() {
        Map<Object, Object> storage = new HashMap<>();
        storage.put("username", username);
        storage.putAll(session.toStorage());
        service.getProfileRepository().getImmediately(characterUUID).ifPresent(profile ->
                storage.put("profileProperties", profile.getProperties()));
        return storage;
    }

    public YggdrasilService getYggdrasilService() {
        return service;
    }

    @Override
    public ObjectBinding<Optional<Map<TextureType, Texture>>> getTextures() {
        return BindingMapping.of(service.getProfileRepository().binding(getUUID()))
                .map(profile -> profile.flatMap(it -> {
                    try {
                        return YggdrasilService.getTextures(it);
                    } catch (ServerResponseMalformedException e) {
                        LOG.log(Level.WARNING, "Failed to parse texture payload", e);
                        return Optional.empty();
                    }
                }));

    }

    @Override
    public void clearCache() {
        authenticated = false;
        service.getProfileRepository().invalidate(characterUUID);
    }

    public void uploadSkin(String model, Path file) throws AuthenticationException, UnsupportedOperationException {
        service.uploadSkin(characterUUID, session.getAccessToken(), model, file);
    }

    private static String randomClientToken() {
        return UUIDTypeAdapter.fromUUID(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return "YggdrasilAccount[uuid=" + characterUUID + ", username=" + username + "]";
    }

    @Override
    public int hashCode() {
        return characterUUID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != YggdrasilAccount.class)
            return false;
        YggdrasilAccount another = (YggdrasilAccount) obj;
        return characterUUID.equals(another.characterUUID);
    }
}
