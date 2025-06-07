package lld.implement.cache.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class DoublyLinkedListTest {
    private DoublyLinkedList<String, Integer> list;
    private Node<String, Integer> node1;
    private Node<String, Integer> node2;
    private Node<String, Integer> node3;

    @BeforeEach
    void setUp() {
        list = new DoublyLinkedList<>();
        node1 = new Node<>("one", 1);
        node2 = new Node<>("two", 2);
        node3 = new Node<>("three", 3);
    }

    @Test
    void testAddFirst() {
        // Test adding first node
        list.addFirst(node1);
        assertEquals(node1, list.getLast(), "Node should be the last node after adding first");
        
        // Test adding second node
        list.addFirst(node2);
        assertSame(node1, list.getLast(), "First added node should now be last");
    }

    @Test
    void testMoveToHead() {
        // Setup: add two nodes
        list.addFirst(node1);
        list.addFirst(node2);
        
        // Move node1 to head
        list.moveToHead(node1);
        
        // node2 should now be last
        assertSame(node2, list.getLast(), "Node2 should be last after moving node1 to head");
        
        // Move node2 to head
        list.moveToHead(node2);
        assertSame(node1, list.getLast(), "Node1 should be last after moving node2 to head");
    }

    @Test
    void testRemoveLast() {
        // Test empty list
        assertNull(list.removeLast(), "Should return null for empty list");
        
        // Add nodes
        list.addFirst(node1);
        list.addFirst(node2);
        
        // Remove last and verify
        Node<String, Integer> removed = list.removeLast();
        assertSame(node1, removed, "Should return the last node");
        assertSame(node2, list.getLast(), "node2 should now be last");
        
        // Remove last again
        removed = list.removeLast();
        assertSame(node2, removed, "Should return the new last node");
        
        // List should be empty now
        assertNull(list.removeLast(), "Should return null when list is empty");
    }

    @Test
    void testRemove() {
        // Add nodes
        list.addFirst(node1);
        list.addFirst(node2);
        list.addFirst(node3);
        
        // Remove middle node
        list.remove(node2);
        assertSame(node1, list.getLast(), "node1 should be last after removing node2");
        
        // Remove last node
        list.remove(node1);
        assertSame(node3, list.getLast(), "node3 should be last after removing node1");
        
        // Remove only remaining node
        list.remove(node3);
        assertNull(list.getLast(), "List should be empty after removing all nodes");
    }

    @Test
    void testGetLast() {
        // Test empty list
        assertNull(list.getLast(), "Should return null for empty list");
        
        // Add node and test
        list.addFirst(node1);
        assertSame(node1, list.getLast(), "Should return the only node");
        
        // Add another node and test
        list.addFirst(node2);
        assertSame(node1, list.getLast(), "Should return the last added node");
    }

    @Test
    void testClear() {
        // Add nodes
        list.addFirst(node1);
        list.addFirst(node2);
        
        // Clear list
        list.clear();
        
        // Verify list is empty
        assertNull(list.getLast(), "List should be empty after clear");
        assertNull(list.removeLast(), "removeLast should return null after clear");
    }
    
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void testThreadSafety() throws InterruptedException {
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        
        // Each thread will add and remove nodes
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                Node<String, Integer> node = new Node<>("thread-" + threadId, threadId);
                for (int j = 0; j < 100; j++) {
                    list.addFirst(node);
                    list.moveToHead(node);
                    list.getLast();
                    list.remove(node);
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        // Final state should be consistent
        assertNull(list.getLast(), "List should be empty after all operations");
    }
}