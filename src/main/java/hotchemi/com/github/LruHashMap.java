package hotchemi.com.github;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom {@link java.util.HashMap} using a LRU policy.
 *
 * @param <K> key
 * @param <V> value
 * @author Shintaro Katafuchi
 */
final class LruHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public LruHashMap(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * This function Overrides the LinkedHashMap - and is actually: shouldRemoveEldestEntry
     * @param entry
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry entry) {
        return size() > capacity;
    }

    /**
     * Implement a method which returns the oldest entry in the map
     * @return oldest entry
     */
    public synchronized Map.Entry<K, V> getEldestEntry() {
        //Your code here
        return null;

    }
}
