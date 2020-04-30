package ru.spark.slauncher.game;

/**
 * Determines where game runs in and game files such as mods.
 *
 * @author spark1337
 */
public enum GameDirectoryType {
    /**
     * .minecraft
     */
    ROOT_FOLDER,
    /**
     * .minecraft/versions/&lt;version name&gt;
     */
    VERSION_FOLDER,
    /**
     * user customized directory.
     */
    CUSTOM
}