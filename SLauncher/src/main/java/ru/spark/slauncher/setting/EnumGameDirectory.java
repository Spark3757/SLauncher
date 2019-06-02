package ru.spark.slauncher.setting;

/**
 * Determines where game runs in and game files such as mods.
 *
 * @author Spark1337
 */
public enum EnumGameDirectory {
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