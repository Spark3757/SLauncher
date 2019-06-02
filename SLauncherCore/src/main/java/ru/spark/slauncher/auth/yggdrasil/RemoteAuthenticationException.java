package ru.spark.slauncher.auth.yggdrasil;

import ru.spark.slauncher.auth.AuthenticationException;

public class RemoteAuthenticationException extends AuthenticationException {

    private final String name;
    private final String message;
    private final String cause;

    public RemoteAuthenticationException(String name, String message, String cause) {
        super(buildMessage(name, message, cause));
        this.name = name;
        this.message = message;
        this.cause = cause;
    }

    private static String buildMessage(String name, String message, String cause) {
        StringBuilder builder = new StringBuilder(name);
        if (message != null)
            builder.append(": ").append(message);

        if (cause != null)
            builder.append(": ").append(cause);

        return builder.toString();
    }

    public String getRemoteName() {
        return name;
    }

    public String getRemoteMessage() {
        return message;
    }

    public String getRemoteCause() {
        return cause;
    }
}
