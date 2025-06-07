# LRU Cache – Low-Level Design (LLD)  

> Part 2 • **Decorators & Eviction Policies** • _Author: Anupam Kumar_

> Builds on **Part 1 (Core)**. Describes pluggable features that wrap around the core LRU engine using the **Decorator** & **Strategy** patterns.

---

## 1 Package Layout (additions)
```
lld.implement.cache
├─ api
│   ├─ policy        (CachePolicy, EvictionListener)
│   └─ decorator     (TTL, Metrics, Persistence builders)
├─ core              (LruCache, Node, StripedLock)     ← Part 1
└─ support           (internal helpers)
```

*The `api.policy` and `api.decorator` sub-packages are exported; implementations remain encapsulated.*

## 2 Decorator Pattern
```
Cache              (interface)
   ▲
   │  delegates
   │
AbstractForwardingCache           ← provides default passthrough
   ▲                ▲                  ▲
MetricsDecorator   TTLDecorator   PersistDecorator   … (others)
        ▲
    CoreCache (LruCache)  ← final layer
```
* Each decorator extends `AbstractForwardingCache<K,V>` which simply forwards every call to the `delegate` field.
* Decorators are assembled **inside-out** by `CacheBuilder`:
  1. `CoreCache`
  2. Eviction & Striping (from Part 1)
  3. _Outer_ decorators in this order: TTL → Persistence → Metrics (so metrics include loader latency).

## 3 TTLDecorator – Time-to-Live / Expiry
### 3.1 Config
```java
public final class TTLConfig {
    Duration expireAfterWrite = Duration.ZERO;
    Duration expireAfterAccess = Duration.ZERO;
    long sweeperPeriodMs = 1_000;      // Background janitor frequency
}
```

### 3.2 Fields
```java
class TTLDecorator<K,V> extends AbstractForwardingCache<K,V> {
    private final Clock clock;
    private final TTLConfig cfg;
    private final ScheduledExecutorService sweeper;
}
```

### 3.3 Read Path
```java
V get(K key) {
    V val = delegate().get(key);
    if (val == null) return null;                // miss / expired
    Node n = ((LruCache) delegate()).peekNode(key); // internal SPI
    if (isExpired(n)) { remove(key); return null; }
    return val;
}
```
*`peekNode` is friendship-scoped to decorators* (package-private SPI).

### 3.4 Background Sweeper Algorithm
1. Fixed-rate schedule every `sweeperPeriodMs`.
2. Uses `Iterator<Node>` provided by `LruCache.snapshot()` (no locks held).
3. Stops after evicting `cleanUpBatchSize` expired entries to bound latency.

### 3.5 Concurrency Notes
* Sweeper acquires stripe lock **only** for node’s stripe.
* Clock injected for deterministic tests.

## 4 MetricsDecorator – Observatory
### 4.1 MetricsRecorder Abstraction
```java
public interface MetricsRecorder {
    void incrementHit();
    void incrementMiss();
    void recordLoadSuccess(long nanos);
    void recordLoadFailure(long nanos);
    void incrementEviction();
    Timer.Context recordLatency();   // Micrometer wrapper
}
```

`MetricsRecorder` bridges to Micrometer or Dropwizard without leaking dependencies into core.

### 4.2 Implementation Highlights
* `get` – classify hit / miss; wrap in `Timer.Context` if latency histogram enabled.
* `put` / `remove` – update counters.
* Eviction listener wired to `incrementEviction()`.

Overhead: ~2 ns per op when metrics disabled (via `NoopRecorder`).

## 5 PersistDecorator – CacheLoader / CacheWriter
### 5.1 SPI
```java
public interface CacheLoader<K,V> { V load(K key) throws Exception; }
public interface CacheWriter<K,V> {
    void write(K key, V value);
    void delete(K key, @Nullable V value);
}
```

### 5.2 Write-Through Policy
* `put` delegates to core, then asynchronously invokes `writer.write()` using `ForkJoinPool.commonPool()`.
* On eviction/removal → `writer.delete()`.

### 5.3 Load-On-Miss Flow
```java
V get(K key) {
    V v = delegate().get(key);
    if (v != null) { rec.hit(); return v; }
    long start = clock.nanoTime();
    try {
        v = loader.load(key);
        if (v != null) delegate().put(key, v);
        rec.recordLoadSuccess(clock.nanoTime() - start);
        return v;
    } catch (Exception ex) {
        rec.recordLoadFailure(clock.nanoTime() - start);
        throw new CacheLoadException(ex);
    }
}
```
*Ensures exactly-once load per miss using `ConcurrentHashMap.computeIfAbsent` around a **future bucket** if high contention expected (further optimisation).* 

## 6 Eviction Policies (Strategy)
### 6.1 Interface
```java
public interface CachePolicy<K,V> {
    void onAccess(Node<K,V> node);              // e.g., increment frequency
    Node<K,V> selectVictim(DoublyLinkedList list);
}
```

### 6.2 Provided Implementations
| Policy | Extra State | Victim Selection |
|--------|------------|------------------|
| **LRU** (default) | none | tail node |
| **FIFO** | none | oldest insertion (tail) but no `moveToHead` on read |
| **LFU** | `int frequency` per node; min-heap bucket | node with lowest `freq`, LRU tie-break |

*Policy objects are **stateless** or maintain lightweight side structures indexed by Node id.*

### 6.3 Integrating Policy
* `LruCache.put` calls `policy.selectVictim` instead of always `dll.removeTail()`.
* `policy.onAccess(node)` called inside `get` after promotion.

## 7 Builder Wiring Order
```java
Cache<K,V> cache = new CacheBuilder<K,V>()
        .capacity(100_000)
        .policy(new LfuPolicy<>())
        .expireAfterWrite(Duration.ofMinutes(5))
        .metricsRecorder(new MicrometerRecorder(registry))
        .cacheLoader(dbLoader)
        .cacheWriter(dbWriter)
        .build();
```
Builder applies **decorators in fixed order** guaranteeing consistent metrics.

## 8 Testing Plan
| Aspect | Tool | Test Case |
|--------|------|-----------|
| TTL    | JUnit + fake clock | expires after both write & access paths |
| Metrics| AssertJ + Micrometer `SimpleMeterRegistry` | Counters increment correctly |
| Persistence | Testcontainers (Postgres) | loader & writer invoked exactly once |
| Policy LFU | JCStress | Frequency counts under races |

## 9 Open Points / Future Work
* **ARC (Adaptive Replacement Cache)** policy as research.
* **Rate-limited loader** to protect upstream systems.
* **Prometheus native** recorder.

---
_End of Part 2 – proceed to Part 3 (Sharding & Distributed Extensions) when ready._
