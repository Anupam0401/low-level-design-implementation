package lld.implement.cache.internal;

import lld.implement.cache.api.Cache;
import lld.implement.cache.api.CacheEntry;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for cache operations, demonstrating Java 23 features.
 * This class showcases pattern matching for instanceof and records.
 */
public final class CacheUtils {
    
    /**
     * Merges two caches, putting all entries from the source cache into the target cache.
     * This method demonstrates pattern matching for instanceof with records.
     *
     * @param target the target cache to merge entries into
     * @param source the source cache to get entries from
     * @param <K> the type of keys
     * @param <V> the type of values
     * @return the target cache with merged entries (for method chaining)
     */
    public static <K, V> Cache<K, V> mergeCaches(Cache<K, V> target, Cache<K, V> source) {
        source.entries()
            .forEach(entry -> target.put(entry.key(), entry.value()));
        return target;
    }
    
    /**
     * Extracts a value from an object based on its type.
     * This method demonstrates pattern matching for instanceof with records and
     * sealed classes.
     *
     * @param obj the object to extract a value from
     * @return an Optional containing the extracted value, or empty if no value
     *         could be extracted
     * @deprecated Use {@link #extractValue(Object, Class)} for type-safe extraction
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @SuppressWarnings("unchecked")
    public static <V> Optional<V> extractValue(Object obj) {
        // Using pattern matching for instanceof (Java 23 feature)
        if (obj instanceof CacheEntry<?, ?> entry) {
            return Optional.ofNullable((V) entry.value());
        } else if (obj instanceof Map.Entry<?, ?> entry) {
            return Optional.ofNullable((V) entry.getValue());
        } else if (obj instanceof Optional<?> opt) {
            return opt.isPresent() ? Optional.ofNullable((V) opt.get()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Extracts a value from an object based on its type in a type-safe manner.
     * This method demonstrates pattern matching for instanceof with records and
     * type checking.
     *
     * @param <V> the type of value to extract
     * @param obj the object to extract a value from
     * @param type the class representing the expected type of the value
     * @return an Optional containing the extracted value if it matches the expected type,
     *         or empty if no matching value could be extracted
     */
    public static <V> Optional<V> extractValue(Object obj, Class<V> type) {
        // Using pattern matching for instanceof (Java 23 feature) with type checking
        if (obj instanceof CacheEntry<?, ?> entry && type.isInstance(entry.value())) {
            return Optional.of(type.cast(entry.value()));
        } else if (obj instanceof Map.Entry<?, ?> entry && type.isInstance(entry.getValue())) {
            return Optional.of(type.cast(entry.getValue()));
        } else if (obj instanceof Optional<?> opt && opt.isPresent() && type.isInstance(opt.get())) {
            return Optional.of(type.cast(opt.get()));
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Filters a stream of objects to only include those that match the given type.
     * This method demonstrates pattern matching for instanceof with generics.
     *
     * @param stream the stream to filter
     * @param clazz the class to filter by
     * @param <T> the type to filter by
     * @return a stream containing only objects of the given type
     */
    public static <T> Stream<T> filterByType(Stream<?> stream, Class<T> clazz) {
        return stream
            .filter(clazz::isInstance)
            .map(clazz::cast);
    }
}
