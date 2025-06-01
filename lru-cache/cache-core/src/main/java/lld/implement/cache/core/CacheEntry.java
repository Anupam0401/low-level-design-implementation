package lld.implement.cache.core;

/**
 * An immutable record representing a cache entry with key and value.
 * This record is used for operations that need to return both key and value,
 * such as eviction listeners or iteration.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public record CacheEntry<K, V>(K key, V value) {
    /**
     * Creates a new cache entry with the specified key and value.
     *
     * @param key   the key
     * @param value the value
     * @throws NullPointerException if key or value is null
     */
    public CacheEntry {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
    }
}
