package ru.spark.slauncher.launch;

import java.io.IOException;

public class NotDecompressingNativesException extends IOException {
    public NotDecompressingNativesException() {
    }

    public NotDecompressingNativesException(String message) {
        super(message);
    }

    public NotDecompressingNativesException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotDecompressingNativesException(Throwable cause) {
        super(cause);
    }
}
