package ru.spark.slauncher.event;

import ru.spark.slauncher.util.Logging;

import java.util.HashMap;

/**
 * @author Spark1337
 */
public final class EventBus {

    public static final EventBus EVENT_BUS = new EventBus();
    private final HashMap<Class<?>, EventManager<?>> events = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> EventManager<T> channel(Class<T> clazz) {
        if (!events.containsKey(clazz))
            events.put(clazz, new EventManager<>());
        return (EventManager<T>) events.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public Event.Result fireEvent(Event obj) {
        Logging.LOG.info(obj + " gets fired");

        return channel((Class<Event>) obj.getClass()).fireEvent(obj);
    }
}
