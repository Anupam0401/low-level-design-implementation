package lld.implement.cache.core;

import lld.implement.cache.Cache;
import lld.implement.cache.internal.StripedLock;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
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
     * Lock striping for fine-grained concurrency control.
     * Each key maps to a specific lock, allowing operations on different keys
     * to proceed concurrently without contention.
     */
    private final StripedLock stripedLock;

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
        this.stripedLock = new StripedLock(concurrencyLevel);

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
        
        // First check if the key exists without locking
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null; // Key not found
        }
        
        // Lock the specific stripe for this key
        Lock lock = stripedLock.getLock(key);
        lock.lock();
        try {
            // Re-check after acquiring the lock (double-check pattern)
            node = cache.get(key);
            if (node == null) {
                return null; // Key was removed by another thread
            }
            
            // Update access order (move to MRU position)
            accessOrder.moveToHead(node);
            return node.getValue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        
        Lock lock = stripedLock.getLock(key);
        lock.lock();
        try {
            // Check if key already exists
            Node<K, V> existingNode = cache.get(key);
            if (existingNode != null) {
                // Update existing value and move to head
                V oldValue = existingNode.getValue();
                existingNode.setValue(value);
                accessOrder.moveToHead(existingNode);
                return oldValue;
            }
            
            // Check if we need to evict - use synchronized to ensure atomic size check and eviction
            synchronized (this) {
                while (cache.size() >= maxSize) {
                    evict();
                }
            }
            
            // Add new entry
            Node<K, V> newNode = new Node<>(key, value);
            cache.put(key, newNode);
            accessOrder.addFirst(newNode);
            
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Evicts the least recently used entry from the cache.
     * Must be called while holding appropriate locks for the caller's key.
     * 
     * Note: This method doesn't acquire locks itself because it's called from
     * methods that already hold the appropriate lock. This avoids deadlocks
     * that could occur if we tried to acquire multiple locks in different orders.
     */
    private void evict() {
        Node<K, V> toEvict = accessOrder.removeLast();
        if (toEvict != null) {
            K evictKey = toEvict.getKey();
            // We need to acquire the lock for the key we're evicting
            // to ensure thread safety during eviction
            Lock evictLock = stripedLock.getLock(evictKey);
            
            // Only acquire the lock if it's different from the current lock
            // to avoid deadlock or redundant locking
            boolean lockAcquired = false;
            try {
                // Try to acquire the lock, but don't block if unavailable
                lockAcquired = evictLock.tryLock();
                if (lockAcquired) {
                    // Remove with the lock held
                    cache.remove(evictKey, toEvict);
                } else {
                    // If we couldn't get the lock, just try a direct remove
                    // This is safe because ConcurrentHashMap.remove is atomic
                    cache.remove(evictKey, toEvict);
                }
            } finally {
                if (lockAcquired) {
                    evictLock.unlock();
                }
            }
        }
    }

    @Override
    public V remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        Lock lock = stripedLock.getLock(key);
        lock.lock();
        try {
            Node<K, V> node = cache.remove(key);
            if (node != null) {
                accessOrder.remove(node);
                return node.getValue();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        // No need to lock for read-only operations that don't modify the list
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
