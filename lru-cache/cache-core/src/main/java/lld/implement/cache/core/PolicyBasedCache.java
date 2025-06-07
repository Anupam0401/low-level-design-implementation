package lld.implement.cache.core;

import lld.implement.cache.api.Cache;
import lld.implement.cache.api.CacheEntry;
import lld.implement.cache.common.Node;
import lld.implement.cache.internal.StripedLock;
import lld.implement.cache.policy.CachePolicy;
import lld.implement.cache.policy.CachePolicyFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

/**
 * A thread-safe cache implementation that supports different eviction policies.
 * 
 * This cache uses lock striping for high concurrency and can be configured with
 * different eviction policies such as LRU, LFU, or FIFO.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class PolicyBasedCache<K, V> implements Cache<K, V> {

    private final int maxSize;
    private final ConcurrentMap<K, Node<K, V>> cache;
    private final StripedLock stripedLock;
    private final CachePolicy<K, V> evictionPolicy;
    
    /**
     * Creates a new cache with the specified maximum size and eviction policy.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @param evictionPolicy the policy to use for evicting entries when the cache is full
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     * @throws NullPointerException if evictionPolicy is null
     */
    public PolicyBasedCache(int maxSize, CachePolicy<K, V> evictionPolicy) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        this.maxSize = maxSize;
        this.cache = createMap();
        this.stripedLock = new StripedLock();
        this.evictionPolicy = Objects.requireNonNull(evictionPolicy, "Eviction policy cannot be null");
    }
    
    /**
     * Creates a new cache with the specified maximum size and LRU eviction policy.
     *
     * @param maxSize the maximum number of entries the cache can hold
     * @throws IllegalArgumentException if maxSize is less than or equal to zero
     */
    public PolicyBasedCache(int maxSize) {
        this(maxSize, CachePolicyFactory.lru());
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
            
            // Update access order according to the policy
            evictionPolicy.onAccess(node);
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
            Node<K, V> existingNode = cache.get(key);
            if (existingNode != null) {
                V oldValue = existingNode.getValue();
                existingNode.setValue(value);
                evictionPolicy.onAccess(existingNode);
                return oldValue;
            }
            
            // Ensure capacity before adding new entry
            ensureCapacity();
            
            // Add new entry
            Node<K, V> newNode = new Node<>(key, value);
            cache.put(key, newNode);
            evictionPolicy.onPut(newNode);
            
            return null;
        } finally {
            lock.unlock();
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
                evictionPolicy.onRemove(node);
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
        return cache.containsKey(key);
    }
    
    @Override
    public long size() {
        return cache.size();
    }
    
    @Override
    public Stream<CacheEntry<K, V>> entries() {
        return cache.entrySet().stream()
            .map(entry -> {
                K key = entry.getKey();
                Node<K, V> node = entry.getValue();
                return new CacheEntry<>(key, node.getValue());
            });
    }
    
    @Override
    public void close() {
        clear();
    }
    
    /**
     * Removes all mappings from this cache.
     */
    public void clear() {
        cache.clear();
        evictionPolicy.clear();
    }
    
    /**
     * Returns the maximum size of this cache.
     *
     * @return the maximum size of this cache
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Returns the eviction policy used by this cache.
     *
     * @return the eviction policy
     */
    public CachePolicy<K, V> getEvictionPolicy() {
        return evictionPolicy;
    }
    
    /**
     * Ensures that the cache has capacity for a new entry by evicting entries if necessary.
     * This method must be called with the appropriate lock held.
     */
    private void ensureCapacity() {
        while (cache.size() >= maxSize) {
            evict();
        }
    }
    
    /**
     * Evicts an entry from the cache according to the eviction policy.
     * This method must be called with the appropriate lock held.
     */
    private void evict() {
        Node<K, V> evictionCandidate = evictionPolicy.getEvictionCandidate();
        if (evictionCandidate != null) {
            K keyToEvict = evictionCandidate.getKey();
            
            // Get the lock for the key to be evicted
            Lock evictionLock = stripedLock.getLock(keyToEvict);
            
            // Only acquire the lock if it's different from the current lock
            boolean lockAcquired = false;
            try {
                evictionLock.lock();
                lockAcquired = true;
                
                // Remove the entry from the cache
                Node<K, V> removed = cache.remove(keyToEvict);
                if (removed != null) {
                    evictionPolicy.onRemove(removed);
                }
            } finally {
                if (lockAcquired) {
                    evictionLock.unlock();
                }
            }
        }
    }
    
    /**
     * Creates the map used to store cache entries.
     * This method can be overridden by subclasses to use a different map implementation.
     *
     * @return a new concurrent map
     */
    protected ConcurrentMap<K, Node<K, V>> createMap() {
        return new ConcurrentHashMap<>(16, 0.75f, 16);
    }
}
