package lld.implement.cache.core;

import lld.implement.cache.policy.CachePolicyFactory;

/**
 * Thread-safe LRU cache implementation using lock striping for high concurrency.
 * 
 * This implementation uses the Least Recently Used (LRU) eviction policy, which
 * evicts the least recently accessed items first when the cache reaches its capacity.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class LruCache<K, V> extends PolicyBasedCache<K, V> {

    /**
     * Creates a new LRU cache with the specified maximum size.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public LruCache(int maxSize) {
        super(maxSize, CachePolicyFactory.lru());
    }
    
    /**
     * Creates a new LRU cache with the specified maximum size and concurrency level.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param concurrencyLevel the estimated number of concurrently updating threads
     * @throws IllegalArgumentException if maxSize or concurrencyLevel is not positive
     */
    public LruCache(int maxSize, int concurrencyLevel) {
        super(maxSize, CachePolicyFactory.lru());
        // The concurrency level is handled by the StripedLock in the parent class
    }
    // All cache operations are handled by the parent PolicyBasedCache class
    
    // No additional methods needed as all functionality is provided by PolicyBasedCache
}
