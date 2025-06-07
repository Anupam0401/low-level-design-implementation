package lld.implement.cache.policy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import lld.implement.cache.common.Node;

/**
 * Least Frequently Used (LFU) cache eviction policy.
 * <p>
 * This policy evicts items that are accessed least frequently.
 * If multiple items have the same access frequency, the least recently used among them is evicted.
 * </p>
 * This implementation uses a frequency counter for each node and organizes nodes by frequency.
 * Within each frequency bucket, nodes are ordered by access time (LRU order).
 *
 * @param <K> the type of keys maintained by this policy
 * @param <V> the type of mapped values
 */
public class LfuPolicy<K, V> implements CachePolicy<K, V> {

    // Maps each node to its access frequency
    private final Map<Node<K, V>, Integer> frequencies;
    
    // Maps each frequency to a list of nodes with that frequency (in LRU order)
    private final NavigableMap<Integer, LinkedHashSet<Node<K, V>>> frequencyMap;
    
    // Tracks the minimum frequency for O(1) eviction candidate lookup
    private int minFrequency;
    
    /**
     * Creates a new LFU policy.
     */
    public LfuPolicy() {
        this.frequencies = new HashMap<>();
        this.frequencyMap = new TreeMap<>();
        this.minFrequency = 0;
    }
    
    @Override
    public synchronized void onAccess(Node<K, V> node) {
        // Get the current frequency of the node
        int currentFreq = frequencies.getOrDefault(node, 0);
        
        // Remove the node from its current frequency bucket
        if (currentFreq > 0) {
            removeFromFrequency(node, currentFreq);
        }
        
        // Increment the frequency
        int newFreq = currentFreq + 1;
        frequencies.put(node, newFreq);
        
        // Add the node to its new frequency bucket
        frequencyMap.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(node);
        
        // Update minFrequency if needed
        if (currentFreq == minFrequency && frequencyMap.get(currentFreq).isEmpty()) {
            minFrequency = newFreq;
        }
    }
    
    @Override
    public synchronized void onPut(Node<K, V> node) {
        // New nodes start with frequency 1
        frequencies.put(node, 1);
        frequencyMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(node);
        minFrequency = 1; // New node has the minimum frequency
    }
    
    @Override
    public synchronized void onRemove(Node<K, V> node) {
        Integer freq = frequencies.remove(node);
        if (freq != null) {
            removeFromFrequency(node, freq);
        }
    }
    
    @Override
    public synchronized Node<K, V> getEvictionCandidate() {
        // If the cache is empty, return null
        if (frequencyMap.isEmpty()) {
            return null;
        }
        
        // Get the set of nodes with the minimum frequency
        LinkedHashSet<Node<K, V>> candidates = frequencyMap.get(minFrequency);
        if (candidates == null || candidates.isEmpty()) {
            // This shouldn't happen if the policy is properly maintained
            // But just in case, find the lowest non-empty frequency
            for (Map.Entry<Integer, LinkedHashSet<Node<K, V>>> entry : frequencyMap.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    candidates = entry.getValue();
                    minFrequency = entry.getKey();
                    break;
                }
            }
        }
        
        // If we still don't have candidates, return null
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        
        // Return the least recently used node with the minimum frequency
        return candidates.getFirst();
    }
    
    @Override
    public synchronized void clear() {
        frequencies.clear();
        frequencyMap.clear();
        minFrequency = 0;
    }
    
    @Override
    public String getName() {
        return "LFU";
    }
    
    /**
     * Helper method to remove a node from its frequency bucket.
     */
    private void removeFromFrequency(Node<K, V> node, int frequency) {
        LinkedHashSet<Node<K, V>> nodes = frequencyMap.get(frequency);
        if (nodes != null) {
            nodes.remove(node);
            if (nodes.isEmpty()) {
                frequencyMap.remove(frequency);
            }
        }
    }
    
    /**
     * A simple LinkedHashSet implementation to maintain insertion order.
     * This is used to track nodes in LRU order within each frequency bucket.
     */
    private static class LinkedHashSet<E> {
        private final Map<E, Boolean> map = new LinkedHashMap<>();
        
        public void add(E e) {
            map.put(e, Boolean.TRUE);
        }
        
        public boolean remove(E e) {
            return map.remove(e) != null;
        }
        
        public boolean isEmpty() {
            return map.isEmpty();
        }
        
        public E getFirst() {
            if (map.isEmpty()) {
                return null;
            }
            return map.keySet().iterator().next();
        }
    }
}
