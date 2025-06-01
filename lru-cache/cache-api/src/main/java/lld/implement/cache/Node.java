package lld.implement.cache;

/**
 * Internal node for the doubly-linked list used in LRU cache.
 * @param <K> key type
 * @param <V> value type
 */
public class Node<K, V> {
    final K key;  
    V value;      
    Node<K, V> prev;  
    Node<K, V> next;  

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
    
    // Package-private for testing
    void setNext(Node<K, V> next) {
        this.next = next;
    }
    
    void setPrev(Node<K, V> prev) {
        this.prev = prev;
    }
}
