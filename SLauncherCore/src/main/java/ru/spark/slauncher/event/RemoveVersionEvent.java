package ru.spark.slauncher.event;

import ru.spark.slauncher.game.GameRepository;
import ru.spark.slauncher.util.ToStringBuilder;

/**
 * This event gets fired when a minecraft version is being removed.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author spark1337
 */
public class RemoveVersionEvent extends Event {

    private final String version;

    /**
     * @param source  {@link GameRepository}
     * @param version the version id.
     */
    public RemoveVersionEvent(Object source, String version) {
        super(source);
        this.version = version;
    }

    public String getVersion() {
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
