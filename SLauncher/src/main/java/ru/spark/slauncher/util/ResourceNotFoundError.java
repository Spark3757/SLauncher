package ru.spark.slauncher.util;

public class ResourceNotFoundError extends Error {
    public ResourceNotFoundError(String message) {
        super(message);
    }

    public ResourceNotFoundError(String message, Throwable cause) {
        super(message, cause);
    }
}