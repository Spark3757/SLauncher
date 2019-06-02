package ru.spark.slauncher.event;

import ru.spark.slauncher.game.GameRepository;

/**
 * This event gets fired when all the versions in .minecraft folder are loaded.
 * <br>
 * This event is fired on the {@link EventBus#EVENT_BUS}
 *
 * @author Spark1337
 */
public final class RefreshedVersionsEvent extends Event {

    /**
     * Constructor.
     *
     * @param source {@link GameRepository]
     */
    public RefreshedVersionsEvent(Object source) {
        super(source);
    }

}
