package ru.spark.slauncher.event;

import ru.spark.slauncher.util.Logging;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author spark1337
 */
public final class EventBus {

    private final ConcurrentHashMap<Class<?>, EventManager<?>> events = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> EventManager<T> channel(Class<T> clazz) {
        events.putIfAbsent(clazz, new EventManager<>());
        return (EventManager<T>) events.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public Event.Result fireEvent(Event obj) {
        Logging.LOG.info(obj + " gets fired");

        return channel((Class<Event>) obj.getClass()).fireEvent(obj);
    }

    public static final EventBus EVENT_BUS = new EventBus();
}
