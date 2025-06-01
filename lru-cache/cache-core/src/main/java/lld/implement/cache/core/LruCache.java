package lld.implement.cache.core;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lld.implement.cache.Cache;
import lld.implement.cache.DoublyLinkedList;
import lld.implement.cache.Node;

/**
 * Thread-safe LRU cache implementation using lock striping for high concurrency.
 * Implements the {@link Cache} interface with O(1) average time complexity for operations.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class LruCache<K, V> implements Cache<K, V> {
    private static final int DEFAULT_STRIPES = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30; // Same as ConcurrentHashMap

    private final ConcurrentMap<K, Node<K, V>> cache;
    private final DoublyLinkedList<K, V> accessOrder;
    private final int maxSize;
    /**
     * Estimated number of concurrently updating threads. Stored so that subclasses
     * overriding {@link #createMap()} can reuse the value.
     */
    private final int concurrencyLevel;

    /**
     * Creates a new, empty LRU cache with the specified maximum size.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @throws IllegalArgumentException if maxSize is not positive
     */
    public LruCache(int maxSize) {
        this(maxSize, DEFAULT_STRIPES);
    }

    /**
     * Creates a new, empty LRU cache with the specified maximum size and concurrency level.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param concurrencyLevel the estimated number of concurrently updating threads
     * @throws IllegalArgumentException if maxSize or concurrencyLevel is not positive
     */
    public LruCache(int maxSize, int concurrencyLevel) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Maximum size must be positive");
        }
        if (concurrencyLevel <= 0) {
            throw new IllegalArgumentException("Concurrency level must be positive");
        }

        this.maxSize = maxSize;
        this.concurrencyLevel = concurrencyLevel;

        // Use factory methods so that subclasses (e.g., in tests) can inject mocks.
        this.cache = createMap();
        this.accessOrder = createList();
    }

    private static int initialCapacityFor(int maxSize) {
        return Math.min(maxSize << 1, MAXIMUM_CAPACITY);
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null; // Key not found
        }
        
        // Update access order (move to MRU position)
        accessOrder.moveToHead(node);
        return node.getValue();
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        
        // Check if key already exists
        Node<K, V> existingNode = cache.get(key);
        if (existingNode != null) {
            // Update existing value and move to head
            V oldValue = existingNode.getValue();
            existingNode.setValue(value);
            accessOrder.moveToHead(existingNode);
            return oldValue;
        }
        
        // Check if we need to evict
        if (cache.size() >= maxSize) {
            evict();
        }
        
        // Add new entry
        Node<K, V> newNode = new Node<>(key, value);
        cache.put(key, newNode);
        accessOrder.addFirst(newNode);
        
        return null;
    }
    
    /**
     * Evicts the least recently used entry from the cache.
     * Must be called while holding appropriate locks.
     */
    private void evict() {
        Node<K, V> toEvict = accessOrder.removeLast();
        if (toEvict != null) {
            cache.remove(toEvict.getKey(), toEvict);
        }
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        Node<K, V> node = cache.remove(key);
        if (node != null) {
            accessOrder.remove(node);
            return node.getValue();
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return cache.containsKey(key);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public void close() {
        cache.clear();
        // Additional cleanup if needed
    }

    /**
     * Factory method used to create the underlying {@link ConcurrentMap}. Subclasses
     * can override this method to provide a custom or mocked map implementation for
     * testing purposes.
     */
    protected ConcurrentMap<K, Node<K, V>> createMap() {
        return new ConcurrentHashMap<>(initialCapacityFor(maxSize), 0.75f, concurrencyLevel);
    }

    /**
     * Factory method used to create the {@link DoublyLinkedList} that keeps track of
     * access order. Subclasses can override this method to inject a spy or mock for
     * verification in tests.
     */
    protected DoublyLinkedList<K, V> createList() {
        return new DoublyLinkedList<>();
    }
}
