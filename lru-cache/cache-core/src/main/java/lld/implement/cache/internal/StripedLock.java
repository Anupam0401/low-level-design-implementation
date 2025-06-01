package lld.implement.cache.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides fine-grained locking using a technique called lock striping.
 * 
 * Lock striping divides the lock space into multiple segments (stripes),
 * allowing different threads to acquire locks on different segments concurrently.
 * This reduces contention compared to a single global lock.
 * 
 * @implNote This implementation uses power-of-two sizing for efficient modulo operations.
 */
public final class StripedLock {
    private static final int DEFAULT_CONCURRENCY_LEVEL = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAXIMUM_CAPACITY = 1 << 30; // Same as ConcurrentHashMap
    
    private final Lock[] locks;
    private final int mask;
    
    /**
     * Creates a StripedLock with the default concurrency level (2 * number of available processors).
     */
    public StripedLock() {
        this(DEFAULT_CONCURRENCY_LEVEL);
    }
    
    /**
     * Creates a StripedLock with the specified concurrency level.
     * The actual number of locks will be rounded up to the next power of two.
     *
     * @param concurrencyLevel the estimated number of concurrently updating threads
     * @throws IllegalArgumentException if concurrencyLevel is non-positive
     */
    public StripedLock(int concurrencyLevel) {
        if (concurrencyLevel <= 0) {
            throw new IllegalArgumentException("Concurrency level must be positive");
        }
        
        // Round up to power of 2
        int size = 1;
        while (size < concurrencyLevel) {
            size <<= 1;
        }
        
        // Ensure we don't exceed maximum capacity
        size = Math.min(size, MAXIMUM_CAPACITY);
        
        this.locks = new ReentrantLock[size];
        this.mask = size - 1; // For efficient modulo using bitwise AND
        
        // Initialize all locks
        for (int i = 0; i < size; i++) {
            locks[i] = new ReentrantLock();
        }
    }
    
    /**
     * Returns the lock for the specified key.
     * 
     * @param key the key to get the lock for
     * @return the lock for the specified key
     */
    public Lock getLock(Object key) {
        return locks[lockIndex(key)];
    }
    
    /**
     * Returns the number of locks (stripes) in this StripedLock.
     * 
     * @return the number of locks
     */
    public int getNumberOfStripes() {
        return locks.length;
    }
    
    /**
     * Calculates the lock index for the given key.
     * 
     * @param key the key to calculate the lock index for
     * @return the lock index
     */
    private int lockIndex(Object key) {
        int hash = key == null ? 0 : spread(key.hashCode());
        return hash & mask;
    }
    
    /**
     * Spreads (XORs) higher bits of hash to lower and also forces top bit to 0.
     * Because the table uses power-of-two masking, sets of hashes that vary only in
     * bits above the current mask will always collide. (Among known examples are
     * sets of Float keys holding consecutive whole numbers in small tables.)
     * 
     * This function ensures that hashes are well distributed even for these special cases.
     * 
     * @param h the hash code to spread
     * @return the spread hash code
     */
    private static int spread(int h) {
        return (h ^ (h >>> 16)) & 0x7fffffff;
    }
}
