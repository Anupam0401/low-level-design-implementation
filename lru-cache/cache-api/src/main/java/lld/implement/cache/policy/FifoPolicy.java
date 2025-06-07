package lld.implement.cache.policy;

import lld.implement.cache.common.DoublyLinkedList;
import lld.implement.cache.common.Node;

/**
 * First-In-First-Out (FIFO) cache eviction policy.
 * 
 * This policy evicts items in the order they were added to the cache,
 * regardless of how frequently or recently they were accessed.
 * The oldest item (first one added) is always the first to be evicted.
 *
 * @param <K> the type of keys maintained by this policy
 * @param <V> the type of mapped values
 */
public class FifoPolicy<K, V> implements CachePolicy<K, V> {

    private final DoublyLinkedList<K, V> insertionOrder;
    
    /**
     * Creates a new FIFO policy.
     */
    public FifoPolicy() {
        this.insertionOrder = new DoublyLinkedList<>();
    }
    
    @Override
    public void onAccess(Node<K, V> node) {
        // No action needed on access for FIFO
        // The position in the list is determined only by insertion order
    }
    
    @Override
    public void onPut(Node<K, V> node) {
        insertionOrder.addFirst(node);
    }
    
    @Override
    public void onRemove(Node<K, V> node) {
        insertionOrder.remove(node);
    }
    
    @Override
    public Node<K, V> getEvictionCandidate() {
        return insertionOrder.getLast();
    }
    
    @Override
    public void clear() {
        insertionOrder.clear();
    }
    
    @Override
    public String getName() {
        return "FIFO";
    }
}
