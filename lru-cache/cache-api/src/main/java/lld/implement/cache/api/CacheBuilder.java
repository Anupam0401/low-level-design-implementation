package lld.implement.cache.api;

import java.lang.reflect.Constructor;

/**
 * Fluent builder for creating {@link Cache} instances.
 * All methods return {@code this} for method chaining.
 */
public final class CacheBuilder<K, V> {
    private int maxSize = 10_000;
    private int concurrencyLevel = Runtime.getRuntime().availableProcessors() * 2;

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
     * Set the concurrency level (number of stripes) for the cache.
     * Higher values allow more threads to update the cache concurrently
     * without contention, at the cost of slightly higher memory usage.
     * 
     * @param concurrencyLevel must be > 0
     * @return this builder instance
     * @throws IllegalArgumentException if concurrencyLevel ≤ 0
     */
    public CacheBuilder<K, V> concurrencyLevel(int concurrencyLevel) {
        if (concurrencyLevel <= 0) {
            throw new IllegalArgumentException("concurrencyLevel must be positive");
        }
        this.concurrencyLevel = concurrencyLevel;
        return this;
    }
    
    /**
     * Build a new cache instance with the current configuration.
     * 
     * @return a new {@link Cache} instance
     * @throws IllegalStateException if the cache implementation cannot be created
     */
    public Cache<K, V> build() {
        try {
            // Use reflection to create the LruCache instance without direct dependency
            Class<?> lruCacheClass = Class.forName("lld.implement.cache.core.LruCache");
            Constructor<?> constructor = lruCacheClass.getConstructor(int.class, int.class);
            @SuppressWarnings("unchecked")
            Cache<K, V> cache = (Cache<K, V>) constructor.newInstance(maxSize, concurrencyLevel);
            return cache;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create cache instance", e);
        }
    }
}
