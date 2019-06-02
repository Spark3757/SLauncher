package ru.spark.slauncher.launch;

import java.io.IOException;

public class ProcessCreationException extends IOException {
    public ProcessCreationException() {
    }

    public ProcessCreationException(String message) {
        super(message);
    }

    public ProcessCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessCreationException(Throwable cause) {
        super(cause);
    }
}
