package ru.spark.slauncher.launch;

import java.io.IOException;

/**
 * Threw if unable to make file executable.
 */
public class PermissionException extends IOException {
    public PermissionException() {
    }

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionException(Throwable cause) {
        super(cause);
    }
}
