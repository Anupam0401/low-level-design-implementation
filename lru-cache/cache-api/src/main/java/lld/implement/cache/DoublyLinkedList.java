package lld.implement.cache;

/**
 * Intrusive doubly-linked list for LRU ordering.
 * Thread-safe implementation with internal synchronization.
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

    /**
     * Moves the given node to the head of the list.
     * This operation is atomic and thread-safe.
     * 
     * @param node the node to move to the head
     */
    public synchronized void moveToHead(Node<K, V> node) {
        remove(node);
        addFirst(node);
    }

    /**
     * Adds a node to the head of the list.
     * This operation is atomic and thread-safe.
     * 
     * @param node the node to add
     */
    public synchronized void addFirst(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Removes and returns the last node in the list.
     * This operation is atomic and thread-safe.
     * 
     * @return the removed node, or null if the list is empty
     */
    public synchronized Node<K, V> removeLast() {
        if (tail.prev == head) {
            return null; // empty
        }
        Node<K, V> last = tail.prev;
        remove(last);
        return last;
    }

    /**
     * Removes a node from the list.
     * This operation is atomic and thread-safe.
     * 
     * @param node the node to remove
     */
    public synchronized void remove(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
