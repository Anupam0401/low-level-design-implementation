package lld.implement.cache.policy;

import lld.implement.cache.common.Node;

/**
 * Interface for cache eviction policies.
 * 
 * This interface defines the contract for different cache eviction strategies
 * such as LRU (Least Recently Used), LFU (Least Frequently Used), FIFO (First In First Out), etc.
 * 
 * @param <K> the type of keys maintained by this policy
 * @param <V> the type of mapped values
 */
public interface CachePolicy<K, V> {

    /**
     * Called when a key is accessed.
     * 
     * @param node the node being accessed
     */
    void onAccess(Node<K, V> node);
    
    /**
     * Called when a new entry is added to the cache.
     * 
     * @param node the node being added
     */
    void onPut(Node<K, V> node);
    
    /**
     * Called when an entry is removed from the cache.
     * 
     * @param node the node being removed
     */
    void onRemove(Node<K, V> node);
    
    /**
     * Returns the next node to be evicted according to this policy.
     * 
     * @return the node to be evicted, or null if no nodes are available for eviction
     */
    Node<K, V> getEvictionCandidate();
    
    /**
     * Clears all state maintained by this policy.
     */
    void clear();
    
    /**
     * Returns the name of this policy.
     * 
     * @return the name of the policy
     */
    String getName();
}
