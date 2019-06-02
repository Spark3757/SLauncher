package ru.spark.slauncher.game;

/**
 * @author Spark1337
 */
public interface ModAdviser {
    ModSuggestion advise(String fileName, boolean isDirectory);

    enum ModSuggestion {
        SUGGESTED,
        NORMAL,
        HIDDEN
    }
}
