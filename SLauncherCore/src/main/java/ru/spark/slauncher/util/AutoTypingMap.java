package ru.spark.slauncher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A map that support auto casting.
 *
 * @author Spark1337
 */
public final class AutoTypingMap<K> {

    private final Map<K, Object> impl;

    public AutoTypingMap(Map<K, Object> impl) {
        this.impl = impl;
    }

    /**
     * Get the value associated with given {@code key} in the mapping.
     * <p>
     * Be careful of the return type {@code <V>}, as you must ensure that {@code <V>} is correct
     *
     * @param key the key that the value associated with
     * @param <V> the type of value which you must ensure type correction
     * @return the value associated with given {@code key}
     * @throws ClassCastException if the return type {@code <V>} is incorrect.
     */
    @SuppressWarnings("unchecked")
    public synchronized <V> V get(K key) throws ClassCastException {
        return (V) impl.get(key);
    }

    public synchronized <V> Optional<V> getOptional(K key) {
        return Optional.ofNullable(get(key));
    }

    public synchronized void set(K key, Object value) {
        if (value != null)
            impl.put(key, value);
    }

    public Collection<Object> values() {
        return impl.values();
    }

    public Set<K> keys() {
        return impl.keySet();
    }

    public boolean containsKey(K key) {
        return impl.containsKey(key);
    }

    public Object remove(K key) {
        return impl.remove(key);
    }
}
