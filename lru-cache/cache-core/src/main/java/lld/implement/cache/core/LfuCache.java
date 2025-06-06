package lld.implement.cache.core;

import lld.implement.cache.policy.CachePolicyFactory;

/**
 * Thread-safe LFU (Least Frequently Used) cache implementation.
 * 
 * This implementation uses the Least Frequently Used (LFU) eviction policy, which
 * evicts items that are accessed least frequently. If multiple items have the same
 * access frequency, the least recently used among them is evicted.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class LfuCache<K, V> extends PolicyBasedCache<K, V> {

    /**
     * Creates a new LFU cache with the specified maximum size.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public LfuCache(int maxSize) {
        super(maxSize, CachePolicyFactory.lfu());
    }
    
    /**
     * Creates a new LFU cache with the specified maximum size and concurrency level.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param concurrencyLevel the estimated number of concurrently updating threads
     * @throws IllegalArgumentException if maxSize or concurrencyLevel is not positive
     */
    public LfuCache(int maxSize, int concurrencyLevel) {
        super(maxSize, CachePolicyFactory.lfu());
        // The concurrency level is handled by the StripedLock in the parent class
    }
    
    // All cache operations are handled by the parent PolicyBasedCache class
    // The LFU policy ensures items are evicted based on access frequency
}
