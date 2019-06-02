package ru.spark.slauncher.event;

import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.game.Version;
import ru.spark.slauncher.util.ToStringBuilder;

/**
 * This event gets fired when a minecraft version has been loaded.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author Spark1337
 */
public final class LoadedOneVersionEvent extends Event {

    private final Version version;

    /**
     * @param source  {@link GameRepository}
     * @param version the version id.
     */
    public LoadedOneVersionEvent(Object source, Version version) {
        super(source);
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("version", version)
                .toString();
    }
}
