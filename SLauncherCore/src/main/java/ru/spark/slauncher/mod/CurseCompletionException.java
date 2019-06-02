package ru.spark.slauncher.mod;

public class CurseCompletionException extends Exception {
    public CurseCompletionException() {
    }

    public CurseCompletionException(String message) {
        super(message);
    }

    public CurseCompletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurseCompletionException(Throwable cause) {
        super(cause);
    }
}
