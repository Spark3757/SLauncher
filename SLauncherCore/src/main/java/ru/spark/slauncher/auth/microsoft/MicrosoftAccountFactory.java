package ru.spark.slauncher.auth.microsoft;

import java.util.Map;
import java.util.Objects;

import ru.spark.slauncher.auth.AccountFactory;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.CharacterSelector;

public class MicrosoftAccountFactory extends AccountFactory<MicrosoftAccount> {

    private final MicrosoftService service;

    public MicrosoftAccountFactory(MicrosoftService service) {
        this.service = service;
    }

    @Override
    public AccountFactory.AccountLoginType getLoginType() {
        return AccountLoginType.NONE;
    }

    @Override
    public MicrosoftAccount create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException {
        Objects.requireNonNull(selector);

        return new MicrosoftAccount(service, selector);
    }

    @Override
    public MicrosoftAccount fromStorage(Map<Object, Object> storage) {
        Objects.requireNonNull(storage);
        MicrosoftSession session = MicrosoftSession.fromStorage(storage);
        return new MicrosoftAccount(service, session);
    }
}
