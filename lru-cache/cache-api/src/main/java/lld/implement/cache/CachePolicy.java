package lld.implement.cache;

/**
 * Strategy interface for cache eviction policies.
 * @param <K> key type
 * @param <V> value type
 */
public interface CachePolicy<K, V> {
    /**
     * Called when a cache entry is accessed (get/put).
     */
    void onAccess(Node<K, V> node);

    /**
     * Select a victim for eviction. Should not modify the node list.
     */
    Node<K, V> evict(DoublyLinkedList<K, V> list);
}
