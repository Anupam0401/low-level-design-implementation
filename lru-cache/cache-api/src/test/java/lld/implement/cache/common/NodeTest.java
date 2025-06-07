package lld.implement.cache.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class NodeTest {
    
    private Node<String, Integer> node;
    private final String testKey = "testKey";
    private final Integer testValue = 42;
    
    @BeforeEach
    void setUp() {
        node = new Node<>(testKey, testValue);
    }
    
    @Test
    void testConstructor() {
        assertEquals(testKey, node.getKey(), "Key should match constructor argument");
        assertEquals(testValue, node.getValue(), "Value should match constructor argument");
        assertNull(node.prev, "Prev should be null after construction");
        assertNull(node.next, "Next should be null after construction");
    }
    
    @Test
    void testGetKey() {
        assertEquals(testKey, node.getKey(), "getKey() should return the correct key");
    }
    
    @Test
    void testGetValue() {
        assertEquals(testValue, node.getValue(), "getValue() should return the current value");
    }
    
    @Test
    void testSetValue() {
        Integer newValue = 100;
        node.setValue(newValue);
        assertEquals(newValue, node.getValue(), "setValue() should update the node's value");
        
        // Test setting to null
        node.setValue(null);
        assertNull(node.getValue(), "setValue() should allow setting value to null");
    }
    
    @Test
    void testSetNext() {
        Node<String, Integer> nextNode = new Node<>("nextKey", 100);
        node.setNext(nextNode);
        
        assertSame(nextNode, node.next, "setNext() should set the next node reference");
        
        // Test setting to null
        node.setNext(null);
        assertNull(node.next, "setNext() should allow setting next to null");
    }
    
    @Test
    void testSetPrev() {
        Node<String, Integer> prevNode = new Node<>("prevKey", 50);
        node.setPrev(prevNode);
        
        assertSame(prevNode, node.prev, "setPrev() should set the previous node reference");
        
        // Test setting to null
        node.setPrev(null);
        assertNull(node.prev, "setPrev() should allow setting prev to null");
    }
    
    @Test
    void testNodeLinking() {
        Node<String, Integer> first = new Node<>("first", 1);
        Node<String, Integer> middle = new Node<>("middle", 2);
        Node<String, Integer> last = new Node<>("last", 3);
        
        // Link nodes: first <-> middle <-> last
        first.setNext(middle);
        middle.setPrev(first);
        middle.setNext(last);
        last.setPrev(middle);
        
        // Verify forward links
        assertSame(middle, first.next, "first.next should point to middle");
        assertSame(last, middle.next, "middle.next should point to last");
        
        // Verify backward links
        assertSame(middle, last.prev, "last.prev should point to middle");
        assertSame(first, middle.prev, "middle.prev should point to first");
        
        // Verify end conditions
        assertNull(first.prev, "first.prev should be null");
        assertNull(last.next, "last.next should be null");
    }
    
    @Test
    void testWithDifferentTypes() {
        // Test with Integer key and String value
        Node<Integer, String> intKeyNode = new Node<>(123, "test");
        assertEquals(123, intKeyNode.getKey());
        assertEquals("test", intKeyNode.getValue());
        
        // Test with custom object as key
        class CustomKey {
            final int id;
            CustomKey(int id) { this.id = id; }
            @Override public boolean equals(Object o) { return o instanceof CustomKey && ((CustomKey)o).id == id; }
            @Override public int hashCode() { return id; }
        }
        
        CustomKey key1 = new CustomKey(1);
        Node<CustomKey, Double> customKeyNode = new Node<>(key1, 3.14);
        assertEquals(key1, customKeyNode.getKey());
        assertEquals(3.14, customKeyNode.getValue(), 0.001);
    }
}