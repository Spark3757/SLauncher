package ru.spark.slauncher.game;

/**
 * @author Spark1337
 */
public final class VersionNotFoundException extends RuntimeException {

    public VersionNotFoundException() {
    }

    public VersionNotFoundException(String message) {
        super(message);
    }

    public VersionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
