package ru.spark.slauncher.auth;

/**
 * Thrown when the stored credentials has expired.
 * This exception indicates that a password login should be performed.
 *
 * @author spark1337
 * @see Account#logIn()
 */
public class CredentialExpiredException extends AuthenticationException {

    public CredentialExpiredException() {
    }

    public CredentialExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public CredentialExpiredException(String message) {
        super(message);
    }

    public CredentialExpiredException(Throwable cause) {
        super(cause);
    }
}
