package lld.implement.cache;

/**
 * Intrusive doubly-linked list for LRU ordering.
 * Not thread-safe; external synchronization required.
 */
public class DoublyLinkedList<K, V> {
    private final Node<K, V> head;
    private final Node<K, V> tail;

    public DoublyLinkedList() {
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public void moveToHead(Node<K, V> node) {
        remove(node);
        addFirst(node);
    }

    public void addFirst(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public Node<K, V> removeLast() {
        if (tail.prev == head) {
            return null; // empty
        }
        Node<K, V> last = tail.prev;
        remove(last);
        return last;
    }

    public void remove(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
