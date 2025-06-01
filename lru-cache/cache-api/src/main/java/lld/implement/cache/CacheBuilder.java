package lld.implement.cache;

/**
 * Fluent builder for creating {@link Cache} instances.
 * All methods return {@code this} for method chaining.
 */
public final class CacheBuilder<K, V> {
    private int maxSize = 10_000;

    /**
     * Set the maximum number of entries in the cache.
     *
     * @param maxSize must be > 0
     * @throws IllegalArgumentException if maxSize ≤ 0
     */
    public CacheBuilder<K, V> maxSize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Build a new cache instance with the current configuration.
     * Currently, returns a dummy implementation that throws UnsupportedOperationException.
     */
    public Cache<K, V> build() {
        return new Cache<>() {
            @Override public V get(K key) { throw new UnsupportedOperationException("Not implemented yet"); }
            @Override public V put(K key, V value) { throw new UnsupportedOperationException("Not implemented yet"); }
            @Override public V remove(K key) { throw new UnsupportedOperationException("Not implemented yet"); }
            @Override public boolean containsKey(K key) { throw new UnsupportedOperationException("Not implemented yet"); }
            @Override public long size() { return 0; }
        };
    }
}
