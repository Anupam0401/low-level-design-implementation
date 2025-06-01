package lld.implement.cache;

/**
 * Minimal public API for cache implementations. Methods currently throw
 * {@link UnsupportedOperationException}; they will be implemented in later
 * milestones.
 */
public interface Cache<K, V> extends AutoCloseable {

    V get(K key);

    V put(K key, V value);

    V remove(K key);

    boolean containsKey(K key);

    long size();

    @Override
    default void close() {
        // no-op for stubs
    }
}
