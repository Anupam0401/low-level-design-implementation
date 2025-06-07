package lld.implement.cache.api;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A cache that maps keys to values.
 * 
 * This interface has been enhanced with Java 23 features including:
 * - Optional return types for cleaner null handling
 * - Stream-based operations for bulk processing
 * - Pattern matching-friendly methods
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> extends AutoCloseable {

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this cache contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this cache contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     */
    V get(K key);
    
    /**
     * Returns an Optional containing the value to which the specified key is mapped,
     * or an empty Optional if this cache contains no mapping for the key.
     * This method provides a more idiomatic way to handle cache misses using Java's Optional API.
     *
     * @param key the key whose associated value is to be returned
     * @return an Optional containing the value to which the specified key is mapped,
     *         or an empty Optional if this cache contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     */
    default Optional<V> getOptional(K key) {
        return Optional.ofNullable(get(key));
    }
    
    /**
     * Associates the specified value with the specified key in this cache.
     * If the cache previously contained a mapping for the key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}
     * @throws NullPointerException if the specified key or value is null
     */
    V put(K key, V value);
    
    /**
     * Associates the specified value with the specified key in this cache.
     * If the cache previously contained a mapping for the key, the old value is replaced.
     * Returns an Optional containing the previous value, or an empty Optional if there was no mapping.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return an Optional containing the previous value, or an empty Optional if there was no mapping
     * @throws NullPointerException if the specified key or value is null
     */
    default Optional<V> putOptional(K key, V value) {
        return Optional.ofNullable(put(key, value));
    }
    
    /**
     * Removes the mapping for a key from this cache if it is present.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}
     * @throws NullPointerException if the specified key is null
     */
    V remove(K key);
    
    /**
     * Removes the mapping for a key from this cache if it is present.
     * Returns an Optional containing the previous value, or an empty Optional if there was no mapping.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return an Optional containing the previous value, or an empty Optional if there was no mapping
     * @throws NullPointerException if the specified key is null
     */
    default Optional<V> removeOptional(K key) {
        return Optional.ofNullable(remove(key));
    }

    boolean containsKey(K key);

    long size();

    /**
     * Bulk helper method to put all entries from the given map into this cache.
     * This implementation has been optimized to use forEach for better performance.
     *
     * @param map the map containing entries to put into this cache
     * @throws NullPointerException if the map is null or contains null keys or values
     */
    default void putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }
    
    /**
     * Bulk helper method to put all entries from the given stream of cache entries into this cache.
     * This method is useful for processing filtered or transformed streams of entries.
     *
     * @param entries the stream of entries to put into this cache
     * @throws NullPointerException if the stream is null or contains null entries, keys, or values
     */
    default void putAll(Stream<? extends CacheEntry<? extends K, ? extends V>> entries) {
        entries.forEach(entry -> put(entry.key(), entry.value()));
    }
    
    /**
     * Returns a stream of all cache entries.
     * The order of entries in the stream is undefined and may vary between implementations.
     * 
     * @return a stream of all cache entries
     */
    default Stream<CacheEntry<K, V>> entries() {
        throw new UnsupportedOperationException("entries() not implemented");
    }
    
    /**
     * Computes a value for the given key using the provided mapping function.
     * If the mapping function returns null, no mapping is recorded.
     *
     * @param key the key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key,
     *         or null if the computed value is null
     * @throws NullPointerException if the specified key or mappingFunction is null
     */
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = get(key);
        if (value == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return value;
    }
    
    /**
     * Attempts to compute a mapping for the specified key and its current mapped value (or null if no current mapping).
     * The remapping function might return null, indicating that the mapping should be removed.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key or remappingFunction is null
     */
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V oldValue = get(key);
        V newValue = remappingFunction.apply(key, oldValue);
        
        if (newValue == null) {
            // If the function returns null, remove the mapping
            if (oldValue != null) {
                remove(key);
            }
            return null;
        } else {
            // Put the new value and return it
            put(key, newValue);
            return newValue;
        }
    }

    /**
     * Closes this cache, releasing any resources.
     */
    @Override
    void close();
    
    /**
     * Builder entry-point for creating Cache instances.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @return a new CacheBuilder instance
     */
    static <K, V> CacheBuilder<K, V> builder() {
        return new CacheBuilder<>();
    }
}
