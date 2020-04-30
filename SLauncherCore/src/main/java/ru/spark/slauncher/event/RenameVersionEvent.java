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
public class RenameVersionEvent extends Event {

    private final String from, to;

    /**
     * @param source {@link GameRepository}
     * @param from   the version id.
     */
    public RenameVersionEvent(Object source, String from, String to) {
        super(source);
        this.from = from;
        this.to = to;
    }

    public String getFromVersion() {
        return from;
    }

    public String getToVersion() {
        return to;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("from", from)
                .append("to", to)
                .toString();
    }
}
