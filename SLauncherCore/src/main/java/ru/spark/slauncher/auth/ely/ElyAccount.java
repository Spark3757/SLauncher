package ru.spark.slauncher.auth.ely;

import ru.spark.slauncher.auth.*;
import ru.spark.slauncher.util.gson.UUIDTypeAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class ElyAccount extends Account {

    private final ElyService service;
    private final UUID characterUUID;
    private final String username;

    private boolean authenticated = false;
    private ElySession session;

    protected ElyAccount(ElyService service, String username, ElySession session) {
        this.service = requireNonNull(service);
        this.username = requireNonNull(username);
        this.characterUUID = requireNonNull(session.getSelectedProfile().getId());
        this.session = requireNonNull(session);
    }

    protected ElyAccount(ElyService service, String username, String password, CharacterSelector selector) throws AuthenticationException {
        this.service = requireNonNull(service);
        this.username = requireNonNull(username);

        ElySession acquiredSession = service.authenticate(username, password, randomClientToken());
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
    }

    private static String randomClientToken() {
        return UUIDTypeAdapter.fromUUID(UUID.randomUUID());
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
                ElySession acquiredSession;
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
        ElySession acquiredSession = service.authenticate(username, password, randomClientToken());

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
        service.getProfileRepository().getImmediately(characterUUID).ifPresent(profile -> {
            storage.put("profileProperties", profile.getProperties());
        });
        return storage;
    }

    public ElyService getElyService() {
        return service;
    }

    @Override
    public void clearCache() {
        authenticated = false;
        service.getProfileRepository().invalidate(characterUUID);
    }

    @Override
    public String toString() {
        return "ElyAccount[uuid=" + characterUUID + ", username=" + username + "]";
    }

    @Override
    public int hashCode() {
        return characterUUID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ElyAccount))
            return false;
        ElyAccount another = (ElyAccount) obj;
        return characterUUID.equals(another.characterUUID);
    }
}
