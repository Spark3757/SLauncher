package ru.spark.slauncher.game;

import static ru.spark.slauncher.util.i18n.I18n.i18n;

public enum LoadingState {
    DEPENDENCIES("launch.state.dependencies"),
    MODS("launch.state.modpack"),
    LOGGING_IN("launch.state.logging_in"),
    LAUNCHING("launch.state.waiting_launching"),
    DONE("launch.state.done");

    private final String key;

    LoadingState(String key) {
        this.key = key;
    }

    public String getLocalizedMessage() {
        return i18n(key);
    }
}
