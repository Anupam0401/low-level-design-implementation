package lld.implement.cache.policy;

import lld.implement.cache.common.DoublyLinkedList;
import lld.implement.cache.common.Node;

/**
 * Least Recently Used (LRU) cache eviction policy.
 * 
 * This policy evicts the least recently used items first.
 * It maintains a doubly linked list of nodes in order of access,
 * with the most recently used node at the head and the least recently used node at the tail.
 *
 * @param <K> the type of keys maintained by this policy
 * @param <V> the type of mapped values
 */
public class LruPolicy<K, V> implements CachePolicy<K, V> {

    private final DoublyLinkedList<K, V> accessOrder;
    
    /**
     * Creates a new LRU policy.
     */
    public LruPolicy() {
        this.accessOrder = new DoublyLinkedList<>();
    }
    
    @Override
    public void onAccess(Node<K, V> node) {
        accessOrder.moveToHead(node);
    }
    
    @Override
    public void onPut(Node<K, V> node) {
        accessOrder.addFirst(node);
    }
    
    @Override
    public void onRemove(Node<K, V> node) {
        accessOrder.remove(node);
    }
    
    @Override
    public Node<K, V> getEvictionCandidate() {
        return accessOrder.getLast();
    }
    
    @Override
    public void clear() {
        accessOrder.clear();
    }
    
    @Override
    public String getName() {
        return "LRU";
    }
}
