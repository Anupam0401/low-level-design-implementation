package lld.implement.cache.policy;

/**
 * Factory for creating cache eviction policy instances.
 * 
 * This factory provides methods to create different types of cache eviction policies
 * such as LRU, LFU, and FIFO.
 */
public final class CachePolicyFactory {
    
    private CachePolicyFactory() {
        // Utility class, no instances
    }
    
    /**
     * Available cache eviction policies.
     */
    public enum PolicyType {
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
    
    /**
     * Creates a new cache policy of the specified type.
     *
     * @param <K> the type of keys maintained by the policy
     * @param <V> the type of mapped values
     * @param type the type of policy to create
     * @return a new cache policy instance
     * @throws IllegalArgumentException if the policy type is not supported
     */
    public static <K, V> CachePolicy<K, V> create(PolicyType type) {
        return switch (type) {
            case LRU -> new LruPolicy<>();
            case LFU -> new LfuPolicy<>();
            case FIFO -> new FifoPolicy<>();
        };
    }
    
    /**
     * Creates a new LRU (Least Recently Used) cache policy.
     *
     * @param <K> the type of keys maintained by the policy
     * @param <V> the type of mapped values
     * @return a new LRU cache policy
     */
    public static <K, V> CachePolicy<K, V> lru() {
        return new LruPolicy<>();
    }
    
    /**
     * Creates a new LFU (Least Frequently Used) cache policy.
     *
     * @param <K> the type of keys maintained by the policy
     * @param <V> the type of mapped values
     * @return a new LFU cache policy
     */
    public static <K, V> CachePolicy<K, V> lfu() {
        return new LfuPolicy<>();
    }
    
    /**
     * Creates a new FIFO (First In First Out) cache policy.
     *
     * @param <K> the type of keys maintained by the policy
     * @param <V> the type of mapped values
     * @return a new FIFO cache policy
     */
    public static <K, V> CachePolicy<K, V> fifo() {
        return new FifoPolicy<>();
    }
}
