package ru.spark.slauncher.event;

import ru.spark.slauncher.util.SimpleMultimap;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * @author Spark1337
 */
public final class EventManager<T extends Event> {

    private final SimpleMultimap<EventPriority, Consumer<T>> handlers
            = new SimpleMultimap<>(() -> new EnumMap<>(EventPriority.class), HashSet::new);

    public Consumer<T> registerWeak(Consumer<T> consumer) {
        register(new WeakListener(consumer));
        return consumer;
    }

    public Consumer<T> registerWeak(Consumer<T> consumer, EventPriority priority) {
        register(new WeakListener(consumer), priority);
        return consumer;
    }

    public void register(Consumer<T> consumer) {
        register(consumer, EventPriority.NORMAL);
    }

    public synchronized void register(Consumer<T> consumer, EventPriority priority) {
        if (!handlers.get(priority).contains(consumer))
            handlers.put(priority, consumer);
    }

    public void register(Runnable runnable) {
        register(t -> runnable.run());
    }

    public void register(Runnable runnable, EventPriority priority) {
        register(t -> runnable.run(), priority);
    }

    public synchronized Event.Result fireEvent(T event) {
        for (EventPriority priority : EventPriority.values()) {
            for (Consumer<T> handler : handlers.get(priority))
                handler.accept(event);
        }

        if (event.hasResult())
            return event.getResult();
        else
            return Event.Result.DEFAULT;
    }

    private class WeakListener implements Consumer<T> {
        private final WeakReference<Consumer<T>> ref;

        public WeakListener(Consumer<T> listener) {
            this.ref = new WeakReference<>(listener);
        }

        @Override
        public void accept(T t) {
            Consumer<T> listener = ref.get();
            if (listener == null) {
                handlers.removeValue(this);
            } else {
                listener.accept(t);
            }
        }
    }
}
