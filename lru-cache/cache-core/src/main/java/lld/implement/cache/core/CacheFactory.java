package lld.implement.cache.core;

import lld.implement.cache.api.Cache;
import lld.implement.cache.policy.EvictionPolicyType;

/**
 * Factory for creating cache instances with different eviction policies.
 * <p>
 * This factory provides methods to create different types of caches
 * based on the specified eviction policy.
 */
public final class CacheFactory {
    
    private CacheFactory() {
        // Utility class, no instances
    }
    
    /**
     * Creates a new cache with the specified maximum size and eviction policy.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @param maxSize the maximum number of entries the cache can hold
     * @param policy the eviction policy to use
     * @return a new cache instance
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public static <K, V> Cache<K, V> createCache(int maxSize, EvictionPolicyType policy) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        
        return switch (policy) {
            case LRU -> new LruCache<>(maxSize);
            case FIFO -> new FifoCache<>(maxSize);
            case LFU -> new LfuCache<>(maxSize);
        };
    }
    
    /**
     * Creates a new LRU (Least Recently Used) cache with the specified maximum size.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @param maxSize the maximum number of entries the cache can hold
     * @return a new LRU cache instance
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public static <K, V> Cache<K, V> createLruCache(int maxSize) {
        return new LruCache<>(maxSize);
    }
    
    /**
     * Creates a new FIFO (First In First Out) cache with the specified maximum size.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @param maxSize the maximum number of entries the cache can hold
     * @return a new FIFO cache instance
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public static <K, V> Cache<K, V> createFifoCache(int maxSize) {
        return new FifoCache<>(maxSize);
    }
    
    /**
     * Creates a new LFU (Least Frequently Used) cache with the specified maximum size.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @param maxSize the maximum number of entries the cache can hold
     * @return a new LFU cache instance
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public static <K, V> Cache<K, V> createLfuCache(int maxSize) {
        return new LfuCache<>(maxSize);
    }
}
