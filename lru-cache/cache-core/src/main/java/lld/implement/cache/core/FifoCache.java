package lld.implement.cache.core;

import lld.implement.cache.policy.CachePolicyFactory;

/**
 * Thread-safe FIFO (First-In-First-Out) cache implementation.
 * 
 * This implementation uses the First-In-First-Out (FIFO) eviction policy, which
 * evicts items in the order they were added to the cache, regardless of how frequently
 * or recently they were accessed.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class FifoCache<K, V> extends PolicyBasedCache<K, V> {

    /**
     * Creates a new FIFO cache with the specified maximum size.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public FifoCache(int maxSize) {
        super(maxSize, CachePolicyFactory.fifo());
    }
    
    /**
     * Creates a new FIFO cache with the specified maximum size and concurrency level.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param concurrencyLevel the estimated number of concurrently updating threads
     * @throws IllegalArgumentException if maxSize or concurrencyLevel is not positive
     */
    public FifoCache(int maxSize, int concurrencyLevel) {
        super(maxSize, CachePolicyFactory.fifo());
        // The concurrency level is handled by the StripedLock in the parent class
    }
    
    // All cache operations are handled by the parent PolicyBasedCache class
    // The FIFO policy ensures items are evicted in the order they were added
}
