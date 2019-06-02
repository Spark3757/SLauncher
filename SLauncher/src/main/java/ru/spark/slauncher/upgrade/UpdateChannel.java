package ru.spark.slauncher.upgrade;

public enum UpdateChannel {
    STABLE("stable"),
    DEVELOPMENT("dev");

    public final String channelName;

    UpdateChannel(String channelName) {
        this.channelName = channelName;
    }
}
