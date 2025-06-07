package lld.implement.cache.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

import lld.implement.cache.api.CacheEntry;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class LruCacheTest {

    private static final int TEST_CAPACITY = 2;
    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";

    private LruCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new LruCache<>(TEST_CAPACITY);
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    void constructor_shouldInitializeWithGivenCapacity() {
        // Given
        int capacity = 5;

        // When
        try (LruCache<String, String> localCache = new LruCache<>(capacity)) {
            // Then
            assertThat(localCache).isNotNull();
            assertThat(localCache.size()).isZero();
        }
    }

    // This test is no longer applicable as we're using PolicyBasedCache

    @Test
    void constructor_shouldThrowWhenCapacityIsZero() {
        // When & Then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(0))
            .withMessage("Cache size must be positive");
    }

    @Test
    void get_shouldReturnNullWhenKeyNotExists() {
        // When
        String result = cache.get("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void get_shouldReturnValueAndUpdateAccessOrderWhenKeyExists() {
        // Given
        cache.put(KEY_1, VALUE_1);

        // When
        String result = cache.get(KEY_1);

        // Then
        assertThat(result).isEqualTo(VALUE_1);
    }

    @Test
    void put_shouldAddNewEntryWhenKeyNotExists() {
        // When
        String result = cache.put(KEY_1, VALUE_1);

        // Then
        assertThat(result).isNull();
        assertThat(cache.get(KEY_1)).isEqualTo(VALUE_1);
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void put_shouldUpdateExistingEntryAndReturnOldValue() {
        // Given
        cache.put(KEY_1, VALUE_1);

        // When
        String result = cache.put(KEY_1, VALUE_2);

        // Then
        assertThat(result).isEqualTo(VALUE_1);
        assertThat(cache.get(KEY_1)).isEqualTo(VALUE_2);
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void put_shouldEvictLruWhenCapacityExceeded() {
        // Given
        cache.put(KEY_1, VALUE_1);
        cache.put(KEY_2, VALUE_2);

        // When
        cache.put(KEY_3, VALUE_3);

        // Then
        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get(KEY_1)).isNull(); // LRU should be evicted
        assertThat(cache.get(KEY_2)).isEqualTo(VALUE_2);
        assertThat(cache.get(KEY_3)).isEqualTo(VALUE_3);
    }

    @Test
    void eviction_shouldRemoveLeastRecentlyUsedItemWhenFull() {
        // Given
        LruCache<String, String> local = new LruCache<>(2);
        
        try {
            // When
            local.put(KEY_1, VALUE_1);
            local.put(KEY_2, VALUE_2);
            local.put(KEY_3, VALUE_3);
            
            // Then
            assertThat(local.get(KEY_1)).isNull(); // Should be evicted
            assertThat(local.get(KEY_2)).isEqualTo(VALUE_2);
            assertThat(local.get(KEY_3)).isEqualTo(VALUE_3);
        } finally {
            local.close(); // Fix resource leak
        }
    }

    @Test
    void remove_shouldReturnNullWhenKeyNotExists() {
        // When
        String result = cache.remove("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void remove_shouldRemoveAndReturnValueWhenKeyExists() {
        // Given
        cache.put(KEY_1, VALUE_1);

        // When
        String result = cache.remove(KEY_1);

        // Then
        assertThat(result).isEqualTo(VALUE_1);
        assertThat(cache.get(KEY_1)).isNull();
        assertThat(cache.size()).isZero();
    }

    @Test
    void containsKey_shouldReturnTrueWhenKeyExists() {
        // Given
        cache.put(KEY_1, VALUE_1);

        // When & Then
        assertThat(cache.containsKey(KEY_1)).isTrue();
    }

    @Test
    void containsKey_shouldReturnFalseWhenKeyNotExists() {
        // When & Then
        assertThat(cache.containsKey("nonexistent")).isFalse();
    }

    @Test
    void size_shouldReturnCurrentSize() {
        // When & Then
        assertThat(cache.size()).isZero();

        // When
        cache.put(KEY_1, VALUE_1);

        // Then
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void close_shouldClearCache() {
        // Given
        cache.put(KEY_1, VALUE_1);

        // When
        cache.close();

        // Then
        assertThat(cache.size()).isZero();
    }

    @Test
    void close_shouldNotThrowException() {
        // Given
        // When/Then - No exception
        cache.close();
    }
    
    // Tests for Java 23 features
    
    @Test
    void getOptional_shouldReturnEmptyForMissingKey() {
        // Given
        String key = "missing-key";
        
        // When
        Optional<String> result = cache.getOptional(key);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void getOptional_shouldReturnValueForExistingKey() {
        // Given
        cache.put(KEY_1, VALUE_1);
        
        // When
        Optional<String> result = cache.getOptional(KEY_1);
        
        // Then
        assertThat(result).isPresent().contains(VALUE_1);
    }
    
    @Test
    void putOptional_shouldReturnEmptyForNewKey() {
        // Given
        String key = "new-key";
        String value = "new-value";
        
        // When
        Optional<String> result = cache.putOptional(key, value);
        
        // Then
        assertThat(result).isEmpty();
        assertThat(cache.get(key)).isEqualTo(value);
    }
    
    @Test
    void entries_shouldReturnAllCacheEntries() {
        // Given
        cache.put(KEY_1, VALUE_1);
        cache.put(KEY_2, VALUE_2);
        
        // When
        List<CacheEntry<String, String>> entries = cache.entries().collect(Collectors.toList());
        
        // Then
        assertThat(entries).hasSize(2);
        Map<String, String> entryMap = entries.stream()
            .collect(Collectors.toMap(CacheEntry::key, CacheEntry::value));
        assertThat(entryMap).containsEntry(KEY_1, VALUE_1).containsEntry(KEY_2, VALUE_2);
    }
    
    @Test
    void computeIfAbsent_shouldComputeValueWhenKeyMissing() {
        // Given
        String key = "compute-key";
        Function<String, String> computeFunction = k -> k + "-computed";
        
        // When
        String result = cache.computeIfAbsent(key, computeFunction);
        
        // Then
        assertThat(result).isEqualTo("compute-key-computed");
        assertThat(cache.get(key)).isEqualTo("compute-key-computed");
    }
    
    @Test
    void computeIfAbsent_shouldReturnExistingValueWhenKeyPresent() {
        // Given
        cache.put(KEY_1, VALUE_1);
        Function<String, String> computeFunction = k -> k + "-should-not-be-used";
        
        // When
        String result = cache.computeIfAbsent(KEY_1, computeFunction);
        
        // Then
        assertThat(result).isEqualTo(VALUE_1);
        assertThat(cache.get(KEY_1)).isEqualTo(VALUE_1); // Value unchanged
    }

    @Test
    void lruOrder_shouldBeMaintained() {
        // Given
        LruCache<String, String> local = new LruCache<>(2);
        
        try {
            // When - Add items and access them to test LRU order
            local.put(KEY_1, VALUE_1); // [1]
            local.put(KEY_2, VALUE_2); // [2,1]
            local.get(KEY_1);          // [1,2] (access KEY_1, making it most recently used)
            local.put(KEY_3, VALUE_3); // [3,1] (should evict KEY_2)
            
            // Then
            assertThat(local.get(KEY_1)).isEqualTo(VALUE_1); // Still in cache
            assertThat(local.get(KEY_2)).isNull(); // Should be evicted
            assertThat(local.get(KEY_3)).isEqualTo(VALUE_3); // Newest item
        } finally {
            local.close(); // Fix resource leak
        }
    }

    @Test
    void put_shouldNotAcceptNullKey() {
        assertThatNullPointerException()
            .isThrownBy(() -> cache.put(null, VALUE_1))
            .withMessage("Key cannot be null");
    }

    @Test
    void put_shouldNotAcceptNullValue() {
        assertThatNullPointerException()
            .isThrownBy(() -> cache.put(KEY_1, null))
            .withMessage("Value cannot be null");
    }

    @Test
    void get_shouldNotAcceptNullKey() {
        assertThatNullPointerException()
            .isThrownBy(() -> cache.get(null))
            .withMessage("Key cannot be null");
    }

    @Test
    void remove_shouldNotAcceptNullKey() {
        assertThatNullPointerException()
            .isThrownBy(() -> cache.remove(null))
            .withMessage("Key cannot be null");
    }

    // Factory helper tests removed as they're no longer applicable with PolicyBasedCache

    @Test
    void initialCapacityFor_shouldDoubleCapacityButCapAtMax() throws Exception {
        java.lang.reflect.Method m = LruCache.class.getDeclaredMethod("initialCapacityFor", int.class);
        m.setAccessible(true);
        int small = (int) m.invoke(null, 5);
        assertThat(small).isEqualTo(10);
        int big = (int) m.invoke(null, 1 << 29);
        assertThat(big).isEqualTo(1 << 30);
    }

    @Test
    void constructor_shouldThrowWhenConcurrencyLevelIsNotPositive() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(2, 0))
            .withMessage("Concurrency level must be positive");
    }
}
