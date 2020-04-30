package ru.spark.slauncher.auth;

/**
 * This exception gets threw when a monitor of {@link CharacterSelector} cannot select a
 * valid character.
 *
 * @author spark1337
 * @see CharacterSelector
 */
public final class NoSelectedCharacterException extends AuthenticationException {
    public NoSelectedCharacterException() {
    }
}
