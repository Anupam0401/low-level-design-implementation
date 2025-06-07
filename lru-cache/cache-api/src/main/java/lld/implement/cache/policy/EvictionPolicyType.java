package lld.implement.cache.util;

import lld.implement.cache.core.PolicyBasedCache;

/**
 * Enumeration of cache eviction policies.
 * 
 * This enum defines the available cache eviction strategies that can be used
 * with the {@link PolicyBasedCache}.
 */
public enum EvictionPolicy {
    /**
     * Least Recently Used policy.
     * Evicts the least recently accessed items first.
     */
    LRU,
    
    /**
     * Least Frequently Used policy.
     * Evicts the least frequently accessed items first.
     */
    LFU,
    
    /**
     * First In First Out policy.
     * Evicts items in the order they were added, regardless of access patterns.
     */
    FIFO
}
