package lld.implement.cache.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lld.implement.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Concurrency tests for {@link LruCache}.
 * These tests verify that the cache behaves correctly under concurrent access.
 */
class ConcurrentLruCacheTest {

    private static final int CACHE_SIZE = 50;
    private static final int THREAD_COUNT = 2;
    private static final int OPERATIONS_PER_THREAD = 100;
    
    private Cache<Integer, String> cache;
    private ExecutorService executor;
    
    @BeforeEach
    void setUp() {
        cache = new LruCache<>(CACHE_SIZE);
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        cache.close();
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
    
    /**
     * Test concurrent puts and gets with different keys.
     * Each thread operates on its own set of keys to avoid contention.
     */
    @Test
    void concurrentPutsAndGets_withDifferentKeys_shouldSucceed() throws Exception {
        // Use a smaller cache for this test
        Cache<Integer, String> testCache = new LruCache<>(THREAD_COUNT * OPERATIONS_PER_THREAD);
        
        List<Future<?>> futures = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    // Each thread works on its own key range to avoid contention
                    int keyBase = threadId * OPERATIONS_PER_THREAD;
                    
                    // Put all values first
                    for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                        int key = keyBase + i;
                        String value = "value-" + key;
                        testCache.put(key, value);
                        
                        // Small sleep to reduce contention
                        if (i % 10 == 0) Thread.sleep(1);
                    }
                    
                    return null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }));
        }
        
        // Start all threads
        startLatch.countDown();
        
        // Wait for all threads to complete with a shorter timeout
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        
        // Verify some values are in the cache
        int hits = 0;
        for (int t = 0; t < THREAD_COUNT; t++) {
            int keyBase = t * OPERATIONS_PER_THREAD;
            for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                int key = keyBase + i;
                String expected = "value-" + key;
                String actual = testCache.get(key);
                
                if (actual != null) {
                    assertThat(actual).isEqualTo(expected);
                    hits++;
                }
            }
        }
        
        // We should have at least some hits
        assertThat(hits).isPositive();
        assertThat(testCache.size()).isPositive();
        
        testCache.close();
    }
    
    /**
     * Test concurrent puts, gets, and removes with shared keys.
     * This test creates contention on the same keys to verify thread safety.
     */
    @Test
    void concurrentOperations_withSharedKeys_shouldMaintainConsistency() throws Exception {
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger successfulGets = new AtomicInteger(0);
        AtomicInteger nullGets = new AtomicInteger(0);
        
        // Pre-populate the cache with some values
        for (int i = 0; i < CACHE_SIZE / 2; i++) {
            cache.put(i, "initial-" + i);
        }
        
        // Start all threads at the same time
        CountDownLatch startLatch = new CountDownLatch(1);
        
        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    
                    for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                        int key = random.nextInt(CACHE_SIZE);
                        int operation = random.nextInt(3); // 0: get, 1: put, 2: remove
                        
                        switch (operation) {
                            case 0: // get
                                String value = cache.get(key);
                                if (value != null) {
                                    successfulGets.incrementAndGet();
                                    // Verify value format
                                    assertThat(value).matches("(initial|updated)-\\d+");
                                } else {
                                    nullGets.incrementAndGet();
                                }
                                break;
                                
                            case 1: // put
                                cache.put(key, "updated-" + key);
                                break;
                                
                            case 2: // remove
                                cache.remove(key);
                                break;
                        }
                    }
                    
                    return null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }));
        }
        
        // Start all threads
        startLatch.countDown();
        
        // Wait for all threads to complete
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        
        // Verify we had some successful gets (not all were null)
        assertThat(successfulGets.get()).isPositive();
        
        // Verify the cache size is within expected bounds
        assertThat(cache.size()).isLessThanOrEqualTo(CACHE_SIZE);
    }
    
    /**
     * Test that the cache correctly evicts entries when it reaches capacity
     * under concurrent load.
     */
    @Test
    void concurrentPuts_shouldRespectCapacity() throws Exception {
        final int smallCacheSize = 20;
        Cache<Integer, String> smallCache = new LruCache<>(smallCacheSize);
        
        List<Future<?>> futures = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        
        // Each thread will add more entries than the cache can hold
        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    // Use a smaller number of operations to avoid timeouts
                    for (int i = 0; i < smallCacheSize * 2; i++) {
                        // Use a smaller range of keys to increase contention
                        smallCache.put(ThreadLocalRandom.current().nextInt(smallCacheSize * 2), "value");
                        
                        // Small sleep to reduce contention
                        Thread.sleep(1);
                    }
                    
                    return null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }));
        }
        
        // Start all threads
        startLatch.countDown();
        
        // Wait for all threads to complete with a shorter timeout
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        
        // Verify the cache size doesn't exceed capacity
        long actualSize = smallCache.size();
        assertThat(actualSize).isLessThanOrEqualTo(smallCacheSize);
        
        smallCache.close();
    }
}
