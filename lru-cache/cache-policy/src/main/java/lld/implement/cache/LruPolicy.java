package lld.implement.cache;

/**
 * LRU (Least Recently Used) eviction policy.
 */
final class LruPolicy<K, V> implements CachePolicy<K, V> {
    @Override
    public void onAccess(Node<K, V> node) {
        // No-op for LRU; list handles move-to-head
    }

    @Override
    public Node<K, V> evict(DoublyLinkedList<K, V> list) {
        return list.removeLast();
    }
}
