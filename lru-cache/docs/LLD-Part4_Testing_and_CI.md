# LRU Cache – Low-Level Design (LLD)  

> Part 1 • **Testing & Continuous Integration** • _Author: Anupam Kumar_

> Completes the LLD series by specifying the quality-assurance strategy and CI/CD pipeline that guarantee the cache meets its functional and non-functional requirements.

---

## 1 Test-Pyramid Overview
| Layer | Goal | Primary Tools | Perf Budget |
|-------|------|--------------|-------------|
| **Unit** | Logic correctness in isolation | JUnit 5, AssertJ, Mockito | < 5 s total |
| **Component** | Interplay of core + decorators | JUnit 5 + embedded clocks | < 10 s |
| **Integration** | External systems (persistence, remote shard) | Testcontainers, gRPC in-process | < 60 s |
| **Concurrency** | Linearizability & race detection | **JCStress** | < 120 s |
| **Performance** | Micro-benchmarks vs baseline | **JMH** | _p99 ≤ 2 µs_ |
| **E2E / Example** | Demo app smoke tests | REST-assured | < 30 s |

Total CI wall-clock ≤ 4 minutes (parallel GH runners).

## 2 Toolchain Matrix
* **JDK**: 23 (LTS)  
* **Build**: Maven 3.9  
* **Static Analysis**: ErrorProne, SpotBugs, Checkstyle (Google style), PMD  
* **Coverage**: JaCoCo 0.8  
* **Security**: OWASP Dependency-Check, CodeQL  
* **Benchmark**: JMH 1.37  

## 3 Test Suites Detail
### 3.1 Unit Tests
* Cover Node operations, DLL pointer surgery, builder validation, policy interfaces.
* Use `@Execution(CONCURRENT)`; Mockito for stubbing `Clock`, `MetricsRecorder`.
* Target ≥ 95 % line / 90 % branch (enforced in Maven `jacoco-check`).

### 3.2 Concurrency – JCStress
| Scenario | Expectation |
|----------|-------------|
| `PutPut` same key | Last-write-wins, no lost updates |
| `GetRemove` | Never returns removed value after linearization point |
| `ExpireVsGet` | Expired entry never observable |

*Test class generates *whitebox* probes inside `LruCache` via package-private access.*

### 3.3 Performance – JMH
* Benchmarks: `getHotHit`, `getColdMiss`, `putEvict`, `mixedWorkload`.
* Run with `-prof gc,stack` on CI and compare against **jmh-baseline.json**; build fails if > 5 % regression p99.

### 3.4 Integration
* **Persistence**: Postgres container; verify `CacheWriter` flush on eviction.
* **Remote Shard**: gRPC in-process channel; simulate network latency with `GrpcTestRule`.
* **Rebalancer**: spin 4 shards, add 2, assert remap ratio < 0.15.

### 3.5 Property-Based Tests (optional)
* QuickTheories generators to fuzz LFU frequency counters.

## 4 Static-Analysis & Security Gates
| Tool | Rule Set | Fail Severity |
|------|----------|---------------|
| SpotBugs | _all_ | HIGH ≥ 1 |
| ErrorProne | `-XepAllErrorsAsWarnings` + curated › errors = fail |
| Checkstyle | Google Java Format | any violation |
| PMD | performance + basic | > 5 issues |
| OWASP | CVSS ≥ 7.0 | fail build |
| CodeQL  | default java-code-scanning | critical only |

## 5 GitHub Actions – `ci.yml` (excerpt)
```yaml
name: CI
on:
  push:
    branches: [ main ]
  pull_request:
jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [ '23' ]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v3
        with:
          distribution: temurin
          java-version: ${{ matrix.java }}
          cache: maven
      - name: Build & test
        run: mvn -B verify
      - name: Upload coverage to Codecov
        if: matrix.java == '23'
        uses: codecov/codecov-action@v4
      - name: Publish snapshot
        if: github.ref == 'refs/heads/main' && matrix.java == '23'
        run: mvn -B deploy -P ossrh
```

### CI Stages (Maven lifecycle)
1. `validate` – licence header check.  
2. `compile` – ErrorProne plugin active.  
3. `test` – unit + component (parallel).  
4. `verify` – JCStress, JMH, SpotBugs, Checkstyle, Jacoco.  
5. `deploy` – snapshot/nightly to OSSRH; tags trigger release.

## 6 Quality Gates
| Gate | Threshold | Enforcement |
|------|-----------|-------------|
| Coverage | ≥ 95 % lines | Jacoco check plugin |
| JMH Regression | ≤ 5 % p99 delta | `jmh-compare` Maven ext |
| SpotBugs | 0 HIGH | Maven spotbugs plugin |
| Dependency CVE | CVSS < 7 | OWASP dependency-check |
| JCStress | 0 violations | failsafe plugin |

## 7 Reporting & Dashboards
* **Codecov** – visual diff coverage per PR.  
* **Artifacts** – JMH `results.json`, JCStress `results.csv`, HTML SpotBugs report.  
* **Badges** – build, coverage, latest release in README.

## 8 Future Enhancements
* **Flaky-test detection** via GitHub Action rerun + statistics.  
* **Self-hosted runners** with NUMA pinning for deterministic benchmarks.  
* **Snyk** supplement OWASP scan for transitive vulns.  
* **Kubernetes e2e** once remote shard gRPC is production-ready.

---
_End of Part 4 – LLD series complete._
