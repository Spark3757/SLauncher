package ru.spark.slauncher.auth;

import java.util.Map;

/**
 * @author spark1337
 */
public abstract class AccountFactory<T extends Account> {

    public abstract T create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException;

    public abstract T fromStorage(Map<Object, Object> storage);
}
