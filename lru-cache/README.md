# Designing a LRU Cache

## Requirements

### 1. Functional Requirements
1. **CRUD-style API**  
   • `put(K key, V value)` – upsert.  
   • `get(K key)` – fetch or `null`.  
   • `containsKey(K key)` – boolean.  
   • `remove(K key)` – explicit delete.  
   • `size()` / `capacity()`.

2. **Eviction Policy**  
   • Default: LRU (access-order).  
   • Pluggable: accept a `CachePolicy` strategy (LFU, FIFO, TTL, size-aware, custom).

3. **Capacity Management**  
   • Fixed upper bound supplied at construction.  
   • Optional **dynamic resizing** via `resize(int newCapacity)`.

4. **TTL / Expiry (Optional Module)**  
   • Key-level or cache-wide time-to-live.  
   • Lazy eviction on access **+** periodic background sweeper.

5. **Persistence Hooks (Optional)**  
   • `CacheLoader<K,V>` to auto-load missing entries.  
   • `CacheWriter<K,V>` to push evicted/updated items to an external store.

6. **Metrics & Monitoring**  
   • Hits, misses, evictions, load time, current memory footprint.  
   • JMX / Micrometer exporter.

### Additional Functional Requirements
1. **Detailed API Semantics**  
   a. `put(K,V)` — overwrites if key exists and returns *previous* value or `null`.  
   b. `get(K)` — returns value or `Optional.empty()` (configurable).  
   c. `remove(K)` — removes mapping and returns the removed value or `null`.  
   d. `putAll(Map<K,V>)`, `invalidateAll(Collection<K>)` for bulk operations (optional).  
   e. All operations exposed via both synchronous and `CompletableFuture` async variants.

2. **Stronger Concurrency Guarantees**  
   • All public operations are **linearizable**.  
   • Aggregate view methods (`size`, `stats`) may be weakly consistent.  
   • No operation may block indefinitely; worst-case wait ≤ configurable timeout.

3. **TTL / Expiry Behaviour**  
   • TTL measured from **write** or **access** (user-selectable).  
   • Expired entries are invisible to future `get`/`contains`.  
   • Provide `cleanUp()` hook for manual sweeping; background janitor configurable.

4. **Observability Hooks**  
   • Metrics counters: `hitCount`, `missCount`, `evictionCount`, `loadSuccess`, `loadFailure`.  
   • Event callbacks: `onEvict`, `onExpire`, `onError`.  
   • Configurable log level; default INFO for anomalous events.

### 2. Non-Functional Requirements
| Aspect              | Requirement                                                                    |
|---------------------|--------------------------------------------------------------------------------|
| **Throughput**      | ≥ 5 M ops/sec on 8-core JVM 17, capacity = 1 M                                   |
| **Latency**         | p99 `get` & `put` ≤ 2 µs; p50 ≤ 400 ns                                          |
| **Time Complexity** | `O(1)` average for `get`, `put`, `remove`.                                      |
| **Memory Overhead** | Node + map entry ≤ 48 bytes over raw value (64-bit JVM, compressed-oops).        |
| **Thread Safety**   | Linearizable operations; no starvation; worst-case wait ≤ configurable timeout. |
| **Scalability**     | 10⁶+ entries; horizontal sharding supported via key hashing.                    |
| **Extensibility**   | New eviction policies without touching core cache logic.                        |
| **Observability**   | SLF4J logging + metrics (Micrometer/JMX).                                       |
| **Failure Modes**   | Null key/value → `IllegalArgumentException`; OOM propagates to caller.          |
| **Configuration**   | All tunables exposed via builder & env overrides (e.g., `CACHE_MAX_SIZE`).      |

### Acceptance Criteria
• All requirements verified by automated JUnit + JMH test-suite.  
• Concurrency stress test (JCStress/Jepsen-style) shows *zero* safety violations.  
• Metrics counters validated via mock registry.  
• Documentation (`README` + Javadoc) updated when requirements change.

## Classes, Interfaces and Enumerations
1. The **Node** class represents a node in the doubly linked list, containing the key, value, and references to the previous and next nodes.
2. The **LRUCache** class implements the LRU cache functionality using a combination of a hash map (cache) and a doubly linked list (head and tail).
3. The get method retrieves the value associated with a given key. If the key exists in the cache, it is moved to the head of the linked list (most recently used) and its value is returned. If the key does not exist, null is returned.
4. The put method inserts a key-value pair into the cache. If the key already exists, its value is updated, and the node is moved to the head of the linked list. If the key does not exist and the cache is at capacity, the least recently used item (at the tail of the linked list) is removed, and the new item is inserted at the head.
5. The addToHead, removeNode, moveToHead, and removeTail methods are helper methods to manipulate the doubly linked list.
6. The synchronized keyword is used on the get and put methods to ensure thread safety, allowing concurrent access from multiple threads.
7. The **LRUCacheDemo** class demonstrates the usage of the LRU cache by creating an instance of LRUCache with a capacity of 3, performing various put and get operations, and printing the results.