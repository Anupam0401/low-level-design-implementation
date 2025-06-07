package lld.implement.cache.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import lld.implement.cache.api.Cache;

/**
 * Tests for the cache implementations with different eviction policies.
 */
public class PolicyBasedCacheTest {

    private static final int TEST_CAPACITY = 3;
    
    // Cache instances to be closed after each test
    private Cache<String, String> testCache;

    @AfterEach
    void tearDown() {
        if (testCache != null) {
            testCache.close();
        }
    }

    @Test
    void constructor_shouldThrowWhenCapacityIsZero() {
        // When & Then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(0))
            .withMessage("Cache size must be positive");
    }

    @Test
    void constructor_shouldThrowWhenCapacityIsNegative() {
        // When & Then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(-1))
            .withMessage("Cache size must be positive");
    }

    @Test
    void constructor_shouldThrowWhenConcurrencyLevelIsNegative() {
        // When & Then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(10, -1))
            .withMessage("Concurrency level must be positive");
    }

    @Test
    void lruCache_shouldEvictLeastRecentlyUsedItem() {
        // Given
        testCache = new LruCache<>(TEST_CAPACITY);
        Cache<String, String> cache = testCache;
        
        // When - Add items and access them to test LRU order
        cache.put("key1", "value1"); // [1]
        cache.put("key2", "value2"); // [2,1]
        cache.put("key3", "value3"); // [3,2,1]
        cache.get("key1");           // [1,3,2] (access key1, making it most recently used)
        cache.put("key4", "value4"); // [4,1,3] (should evict key2)
        
        // Then
        assertThat(cache.get("key1")).isEqualTo("value1"); // Still in cache
        assertThat(cache.get("key2")).isNull(); // Should be evicted
        assertThat(cache.get("key3")).isEqualTo("value3"); // Still in cache
        assertThat(cache.get("key4")).isEqualTo("value4"); // Newest item
    }

    @Test
    void fifoCache_shouldEvictOldestItem() {
        // Given
        testCache = new FifoCache<>(TEST_CAPACITY);
        Cache<String, String> cache = testCache;
        
        // When - Add items to test FIFO order
        cache.put("key1", "value1"); // [1]
        cache.put("key2", "value2"); // [2,1]
        cache.put("key3", "value3"); // [3,2,1]
        cache.get("key1");           // [3,2,1] (access doesn't change order)
        cache.put("key4", "value4"); // [4,3,2] (should evict key1)
        
        // Then
        assertThat(cache.get("key1")).isNull(); // Should be evicted (oldest)
        assertThat(cache.get("key2")).isEqualTo("value2"); // Still in cache
        assertThat(cache.get("key3")).isEqualTo("value3"); // Still in cache
        assertThat(cache.get("key4")).isEqualTo("value4"); // Newest item
    }

    @Test
    void lfuCache_shouldEvictLeastFrequentlyUsedItem() {
        // Given
        testCache = new LfuCache<>(TEST_CAPACITY);
        Cache<String, String> cache = testCache;
        
        // When - Add items and access them to test LFU order
        cache.put("key1", "value1"); // freq: 1
        cache.put("key2", "value2"); // freq: 1
        cache.put("key3", "value3"); // freq: 1
        
        // Increase frequency of key1 and key2
        cache.get("key1");           // freq: 2
        cache.get("key2");           // freq: 2
        cache.get("key1");           // freq: 3
        
        // Add new item, should evict key3 (freq: 1)
        cache.put("key4", "value4"); // freq: 1, evict key3
        
        // Then
        assertThat(cache.get("key1")).isEqualTo("value1"); // Highest frequency (3)
        assertThat(cache.get("key2")).isEqualTo("value2"); // Medium frequency (2)
        assertThat(cache.get("key3")).isNull(); // Should be evicted (lowest frequency)
        assertThat(cache.get("key4")).isEqualTo("value4"); // Newest item (freq: 1)
        
        // Add another item, should evict key4 (freq: 2 vs freq: 1)
        cache.get("key4");           // freq: 2
        cache.put("key5", "value5"); // freq: 1
        
        // Then
        assertThat(cache.get("key4")).isEqualTo("value4"); // freq: 2
        assertThat(cache.get("key5")).isEqualTo("value5"); // freq: 1
    }
    
    @Test
    void differentCaches_shouldBehaveDifferentlyUnderLoad() {
        // Given
        Cache<Integer, String> lruCache = new LruCache<>(10);
        Cache<Integer, String> fifoCache = new FifoCache<>(10);
        Cache<Integer, String> lfuCache = new LfuCache<>(10);
        
        try {
            // When - Add 20 items (exceeding capacity)
            for (int i = 0; i < 20; i++) {
                String value = "value" + i;
                lruCache.put(i, value);
                fifoCache.put(i, value);
                lfuCache.put(i, value);
                
                // Access some items more frequently
                if (i % 3 == 0) {
                    for (int j = 0; j < 3; j++) {
                        lruCache.get(i);
                        fifoCache.get(i);
                        lfuCache.get(i);
                    }
                }
            }
            
            // Then - Each cache should have different items based on policy
            List<Integer> lruKeys = lruCache.entries()
                    .map(entry -> entry.key())
                    .collect(Collectors.toList());
            
            List<Integer> fifoKeys = fifoCache.entries()
                    .map(entry -> entry.key())
                    .collect(Collectors.toList());
            
            List<Integer> lfuKeys = lfuCache.entries()
                    .map(entry -> entry.key())
                    .collect(Collectors.toList());
            
            // Verify different eviction behaviors
            assertThat(lruKeys).isNotEqualTo(fifoKeys);
            assertThat(lruKeys).isNotEqualTo(lfuKeys);
            assertThat(fifoKeys).isNotEqualTo(lfuKeys);
            
            // LRU should have more recently accessed items
            assertThat(lruKeys).contains(18, 19);
            
            // FIFO should have more recently added items
            assertThat(fifoKeys).contains(19);
            
            // LFU should have more frequently accessed items
            assertThat(lfuKeys).contains(18); // Recently accessed
            assertThat(lfuKeys).contains(15); // Accessed multiple times
        } finally {
            // Clean up
            lruCache.close();
            fifoCache.close();
            lfuCache.close();
        }
    }
    
    @Test
    void cacheImplementations_shouldHaveCorrectTypes() {
        // Given
        try (Cache<String, String> lruCache = new LruCache<>(TEST_CAPACITY);
             Cache<String, String> fifoCache = new FifoCache<>(TEST_CAPACITY);
             Cache<String, String> lfuCache = new LfuCache<>(TEST_CAPACITY)) {
            
            // Then - Verify correct implementation types
            assertThat(lruCache).isInstanceOf(LruCache.class);
            assertThat(fifoCache).isInstanceOf(FifoCache.class);
            assertThat(lfuCache).isInstanceOf(LfuCache.class);
        }
    }
}
