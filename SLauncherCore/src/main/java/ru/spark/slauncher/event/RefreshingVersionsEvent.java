package ru.spark.slauncher.event;

import ru.spark.slauncher.game.GameRepository;

/**
 * This event gets fired when loading versions in a .minecraft folder.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author Spark1337
 */
public final class RefreshingVersionsEvent extends Event {

    /**
     * Constructor.
     *
     * @param source {@link GameRepository}
     */
    public RefreshingVersionsEvent(Object source) {
        super(source);
    }

    @Override
    public boolean hasResult() {
        return true;
    }
}
