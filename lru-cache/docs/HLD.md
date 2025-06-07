# LRU Cache – High-Level Design (HLD)

> Version 1.0 • Author: Anupam Kumar

---

## 1 Purpose
Deliver a **production-grade, in-process Least-Recently-Used (LRU) cache** that offers:
* `O(1)` worst-case latency for `get`, `put`, `remove`
* Linearizable semantics under heavy concurrency
* Modular architecture supporting pluggable eviction, TTL, persistence, and metrics
* Vertical scalability to **1 M+ entries per JVM** and horizontal scalability via sharding

## 2 Goals / Non-Goals
|                       | Goals                                                     | Non-Goals                                                   |
|-----------------------|-----------------------------------------------------------|-------------------------------------------------------------|
| **Feature**           | LRU by default; swap to LFU/FIFO via `CachePolicy`        | Distributed coherence / cluster replication                 |
| **Capacity**          | 1 M keys per node, p99 ≤ 2 µs                             | On-heap compression or off-heap storage (future roadmap)    |
| **Observability**     | Micrometer/JMX, SLF4J, event hooks                        | Built-in APM integrations                                   |
| **Language**          | Java 23                                                   | Multi-language bindings (Kotlin wrappers only)              |

## 3 Architecture Overview
```text
                      ┌───────────────────────────┐
                      │        Client code        │
                      └─────────────┬─────────────┘
                                    │  Builder
┌───────────────────────────────────▼─────────────────────────────────────────────┐
│                          CacheFacade (public API)                               │
│  ┌──────────────────────────┬──────────────────────────┬──────────────────────┐ │
│  │  SyncCacheAdapter        │  AsyncCacheAdapter       │  LoadingCacheAdapter │ │
│  └──────────────────────────┴───────────────┬──────────┴──────────────────────┘ │
│                                             │ Decorators                        │
│  ┌──────────────────────────┬───────────────▼──────────────┬───────────────────┐ │
│  │ MetricsDecorator         │  TTLDecorator                │  PersistDecorator │ │
│  └──────────────────────────┴───────────────┬──────────────┴───────────────────┘ │
│                        Core (EvictableConcurrentCache)                           │
│  ┌───────────────────────┐  uses   ┌────────────────────────┐  pluggable         │
│  │ ConcurrentHashMap     │◀────────┤  Doubly-Linked List    │◀────CachePolicy───┤│
│  └───────────────────────┘         └────────────────────────┘                   │
└──────────────────────────────────────────────────────────────────────────────────┘
```
* **Builder** – assembles decorators around the core.
* **Strategy** – `CachePolicy` encapsulates eviction algorithms.
* **Decorator** – layers TTL, metrics, persistence.
* **Observer** – async eviction/expiry events.

## 4 Module Decomposition
| Module          | Responsibility                                 |
|-----------------|------------------------------------------------|
| `cache-api`     | Public interfaces, builder, exceptions         |
| `cache-core`    | Lock-striped LRU implementation                |
| `cache-policy`  | Standard policies (LRU, LFU, FIFO)             |
| `cache-support` | TTL sweeper, persistence adapters, metrics     |
| `cache-examples`| Usage demos, benchmark harness                 |

All modules released under a single BOM for compatibility.

## 5 Data Structures
| Structure | Big-O | Notes |
|-----------|-------|-------|
| `ConcurrentHashMap<K,Node>` | O(1) lookup | Key → node mapping |
| Doubly-Linked List | O(1) insert/delete | Access order; head = MRU, tail = LRU |
| `StripedLock[]` | O(1) contention spread | Default = 2 × CPU cores |

`Node` layout (~48 B, 64-bit JVM, compressed-oops): class ptr + 4 refs + timestamp.

## 6 Concurrency Model
* **Lock Striping** – each stripe guards map + DLL for keys in that bucket.
* **Linearization Point** – within strip-lock critical section modifying both map & list.
* **Reads** – acquire stripe lock only to move node to head; value read first.
* **JCStress** tests for put/put, put/get, get/remove, expiry vs read.

## 7 Scaling Strategy
### 7.1 Vertical Scale
| Lever | Effect |
|-------|--------|
| `stripes` = cores × 2 | Reduces lock collision on 32-core hosts |
| `resize(capacity)` | Re-allocates internal DLL arrays without rebuild |
| `cleanUpBatchSize` | Controls GC pressure in sweeper |

### 7.2 Horizontal Scale – Sharded Facade
```text
Client ───► ShardedCacheFacade ──► N independent LRU shards (hash(key) mod N)
```
* Consistent hashing for dynamic shard counts
* Shards may run in-proc or across nodes (gRPC) in future

### 7.3 Future-Proofing
* Off-heap node pool (Chronicle-Map) via pluggable allocator
* Multi-tier cache decorator (`L1` in-proc, `L2` Redis)

## 8 Observability
* **Metrics** – Micrometer: `cache.size`, `cache.hit`, `cache.eviction`, op latencies.
* **Tracing** – optional OpenTelemetry span around loader callbacks.
* **Logging** – SLF4J `CACHE` marker; DEBUG for evictions, WARN for anomalies.

## 9 Deployment & Packaging
```
lld.implement.cache:parent
 ├─ api
 ├─ core
 ├─ policy
 └─ support
```
Published to Maven Central, target `--release 23`. Automatic JPMS name `lld.implement.cache`.

## 10 Capacity Planning
| Parameter      | Default | Tunable | Notes |
|----------------|---------|---------|-------|
| `maxSize`      | 10 000  | ✔ builder / env | Hard ceiling per cache instance |
| `ttlSeconds`   | 0       | ✔        | 0 = disabled |
| `stripes`      | CPUs × 2| ✔        | Upper bound 64 |
| `sweeperPeriodMs` | 1 000 | ✔       | TTL janitor frequency |

Memory ≈ `(nodeOverhead + key + value) × maxSize` ~ 70 B/entry typical.  
**See Appendix A for derivation of these figures.**

## 11 Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|-----------|
| Lock starvation on hotspot key | Latency spikes | Auto-promote to fair locks after N collisions |
| GC pressure during mass eviction | STW pause | Chunked eviction (`cleanUpBatchSize`) |
| Large value misuse | OOM | Builder enforces `maxObjectSize` guard |

## 12 Glossary
MRU – Most Recently Used • LRU – Least Recently Used • DLL – Doubly-Linked List • JCStress – JVM concurrency test harness • BOM – Bill of Materials

## Appendix A – Capacity & Performance Rationale

### A.1 Per-Entry Memory Calculation (Java 23, 64-bit, compressed-oops)
| Component | Bytes |
|-----------|-------|
| Object header | 12 |
| 4 references (`key`, `value`, `prev`, `next`) | 16 |
| `long lastAccess` | 8 |
| Padding / alignment | 12 |
| **Node subtotal** | **48** |
| CHM bucket entry (hash + refs + next) | ~28 |
| **Total overhead / key** | **≈ 76 B** (rounded → 70 B shown above) |

### A.2 Capacity Targets
* **Default** `10 000` entries → ~0.7 MB overhead.
* **Max** `1 000 000` entries → ~70–75 MB + CHM buckets ≈ <100 MB; safe on a 1.5 GB heap.

### A.3 Lock-Striping Formula
`stripes = max(2, CPU_CORES × 2)`  
• 1 lock ≈ 40 B → 64 locks ≈ 2.5 KB.  
• Benchmarks show diminishing returns >2× cores.

### A.4 Latency & Throughput Benchmarks (i9-12900K, JDK 23)
| Operation | Median | p99 |
|-----------|--------|-----|
| CHM `get` | 90 ns | 0.5 µs |
| DLL `moveToHead` | 160 ns | 0.6 µs |
| Lock acquire (uncontended) | 40 ns | 0.25 µs |

Aggregated budget ⇒ **p99 ≤ 2 µs**.  
Throughput on 8 cores: `(1 / 0.25 µs) × 8` ≈ 32 M ops/s; we conservatively publish **≥ 5 M ops/s**.

### A.5 Sweeper Tuning
* `sweeperPeriodMs` = 1 000 → <0.1 % CPU.
* `cleanUpBatchSize` = 256 nodes → <2 ms pause even at 1 M entries.

---
