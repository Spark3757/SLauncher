package ru.spark.slauncher.download.game;

import org.jetbrains.annotations.NotNull;
import ru.spark.slauncher.game.Library;

public class LibraryDownloadException extends Exception {
    private final Library library;

    public LibraryDownloadException(Library library, @NotNull Throwable cause) {
        super("Unable to download library " + library, cause);

        this.library = library;
    }

    public Library getLibrary() {
        return library;
    }
}
