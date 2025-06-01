package lld.implement.cache.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lld.implement.cache.DoublyLinkedList;
import lld.implement.cache.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
        cache = new LruCache<String, String>(TEST_CAPACITY) {
            @Override
            protected ConcurrentMap<String, Node<String, String>> createMap() {
                return new ConcurrentHashMap<>();
            }

            @Override
            protected DoublyLinkedList<String, String> createList() {
                return new DoublyLinkedList<>();
            }
        };
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
        try (LruCache<String, String> localCache = new LruCache<String, String>(capacity) {
            @Override
            protected ConcurrentMap<String, Node<String, String>> createMap() {
                return new ConcurrentHashMap<>();
            }

            @Override
            protected DoublyLinkedList<String, String> createList() {
                return new DoublyLinkedList<>();
            }
        }) {
            // Then
            assertThat(localCache).isNotNull();
            assertThat(localCache.size()).isZero();
        }
    }

    @Test
    void constructor_shouldThrowWhenCapacityIsZero() {
        // When & Then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new LruCache<>(0))
            .withMessage("Maximum size must be positive");
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
        // Track whether moveToHead is invoked
        AtomicBoolean moved = new AtomicBoolean(false);

        DoublyLinkedList<String, String> trackingList = new DoublyLinkedList<>() {
            @Override
            public void moveToHead(Node<String, String> node) {
                moved.set(true);
                super.moveToHead(node);
            }
        };

        LruCache<String, String> testCache = new LruCache<String, String>(TEST_CAPACITY) {
            @Override
            protected ConcurrentMap<String, Node<String, String>> createMap() {
                return new ConcurrentHashMap<>();
            }

            @Override
            protected DoublyLinkedList<String, String> createList() {
                return trackingList;
            }
        };

        // Put value so that node exists
        testCache.put(KEY_1, VALUE_1);

        // When
        String result = testCache.get(KEY_1);

        // Then
        assertThat(result).isEqualTo(VALUE_1);
        assertThat(moved.get()).isTrue();
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
    void lruOrder_shouldBeMaintained() {
        // When
        cache.put(KEY_1, VALUE_1); // [1]
        cache.put(KEY_2, VALUE_2); // [2,1]
        cache.get(KEY_1);          // [1,2]
        cache.put(KEY_3, VALUE_3); // [3,1] (2 should be evicted)

        // Then
        assertThat(cache.get(KEY_1)).isEqualTo(VALUE_1);
        assertThat(cache.get(KEY_2)).isNull(); // Should be evicted
        assertThat(cache.get(KEY_3)).isEqualTo(VALUE_3);
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

    // ---- Additional tests for factory helpers ----

    private static class ExposedLruCache extends LruCache<String, String> {
        ExposedLruCache(int max) { super(max); }
        ConcurrentMap<String, Node<String, String>> exposeCreateMap() { return createMap(); }
        DoublyLinkedList<String, String> exposeCreateList() { return createList(); }
    }

    @Test
    void createList_shouldReturnNewListInstance() {
        ExposedLruCache local = new ExposedLruCache(TEST_CAPACITY);
        DoublyLinkedList<String, String> list = local.exposeCreateList();
        assertThat(list).isNotNull();
        assertThat(list).isInstanceOf(DoublyLinkedList.class);
    }

    @Test
    void createMap_shouldReturnConcurrentHashMap() {
        ExposedLruCache local = new ExposedLruCache(TEST_CAPACITY);
        ConcurrentMap<String, Node<String, String>> map = local.exposeCreateMap();
        assertThat(map).isNotNull();
        assertThat(map).isInstanceOf(ConcurrentHashMap.class);
        assertThat(map).isEmpty();
    }

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
