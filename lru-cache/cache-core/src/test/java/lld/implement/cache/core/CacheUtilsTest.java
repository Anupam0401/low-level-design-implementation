package lld.implement.cache.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import lld.implement.cache.api.Cache;
import lld.implement.cache.api.CacheEntry;
import lld.implement.cache.internal.CacheUtils;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link CacheUtils} class.
 * These tests demonstrate Java 23 features like pattern matching for instanceof and records.
 */
class CacheUtilsTest {

    @Test
    void mergeCaches_shouldMergeAllEntriesAndReturnTargetCache() {
        // Given
        LruCache<String, String> source = new LruCache<>(10);
        source.put("key1", "value1");
        source.put("key2", "value2");
        
        LruCache<String, String> target = new LruCache<>(10);
        target.put("key3", "value3");
        
        // When
        Cache<String, String> result = CacheUtils.mergeCaches(target, source);
        
        // Then
        assertThat(result).isSameAs(target); // Verify method chaining returns the target
        assertThat(target.size()).isEqualTo(3); // All entries merged
        assertThat(target.get("key1")).isEqualTo("value1");
        assertThat(target.get("key2")).isEqualTo("value2");
        assertThat(target.get("key3")).isEqualTo("value3");
    }
    
    @Test
    void extractValue_shouldExtractFromCacheEntry() {
        // Given
        CacheEntry<String, Integer> entry = new CacheEntry<>("key", 42);
        
        // When
        Optional<Integer> result = CacheUtils.extractValue(entry, Integer.class);
        
        // Then
        assertThat(result).isPresent().contains(42);
    }
    
    @Test
    void extractValue_shouldExtractFromMapEntry() {
        // Given
        Map.Entry<String, Double> entry = new AbstractMap.SimpleEntry<>("key", 3.14);
        
        // When
        Optional<Double> result = CacheUtils.extractValue(entry, Double.class);
        
        // Then
        assertThat(result).isPresent().contains(3.14);
    }
    
    @Test
    void extractValue_shouldExtractFromOptional() {
        // Given
        Optional<String> optional = Optional.of("value");
        
        // When
        Optional<String> result = CacheUtils.extractValue(optional, String.class);
        
        // Then
        assertThat(result).isPresent().contains("value");
    }
    
    @Test
    void extractValue_shouldReturnEmptyForUnsupportedType() {
        // Given
        String unsupported = "unsupported";
        
        // When
        Optional<Object> result = CacheUtils.extractValue(unsupported, Object.class);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void extractValue_shouldRespectTypeConstraints() {
        // Given
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        
        // When - try to extract as Integer when actual value is String
        Optional<Integer> result = CacheUtils.extractValue(entry, Integer.class);
        
        // Then - should be empty because of type mismatch
        assertThat(result).isEmpty();
    }
    
    @Test
    void filterByType_shouldFilterStreamByType() {
        // Given
        Stream<?> mixedStream = Stream.of(
            "string",
            42,
            3.14,
            new CacheEntry<>("key", "value"),
            null,
            true
        );
        
        // When
        List<Integer> integers = CacheUtils.filterByType(mixedStream, Integer.class)
            .toList();
        
        // Then
        assertThat(integers).containsExactly(42);
    }
}
