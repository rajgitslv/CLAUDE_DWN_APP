package com.claude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Example demonstrating GC types and behaviors
public class GarbageCollectorTypes {

    public static void main(String[] args) {
        printGCInfo();
        demonstrateGCBehavior();
    }

    private static void printGCInfo() {
        System.out.println("=== Garbage Collector Information ===\n");

        // Get GC beans
        List<java.lang.management.GarbageCollectorMXBean> gcBeans =
                java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();

        for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("GC Name: " + gcBean.getName());
            System.out.println("Collection Count: " + gcBean.getCollectionCount());
            System.out.println("Collection Time: " + gcBean.getCollectionTime() + " ms");
            System.out.println("Memory Pools: " + Arrays.toString(gcBean.getMemoryPoolNames()));
            System.out.println();
        }
    }

    private static void demonstrateGCBehavior() {
        Runtime runtime = Runtime.getRuntime();

        System.out.println("=== Creating Objects to Trigger GC ===");
        printMemory("Before allocation", runtime);

        // Create many objects
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new byte[1024 * 1024]); // 1MB each

            if (i % 20 == 0) {
                printMemory("After " + i + " MB", runtime);
            }
        }

        // Clear references
        list.clear();
        list = null;

        printMemory("After clearing references", runtime);

        // Suggest GC (not guaranteed to run immediately)
        System.gc();

        try {
            Thread.sleep(1000); // Give GC time to run
        } catch (InterruptedException e) {}

        printMemory("After System.gc()", runtime);
    }

    private static void printMemory(String label, Runtime runtime) {
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMB = runtime.totalMemory() / 1024 / 1024;
        long maxMB = runtime.maxMemory() / 1024 / 1024;

        System.out.printf("%s: Used=%dMB, Total=%dMB, Max=%dMB%n",
                label, usedMB, totalMB, maxMB);
    }
}

/*
==========================================
GARBAGE COLLECTOR TYPES
==========================================

1. SERIAL GC (-XX:+UseSerialGC)
   - Single-threaded
   - Best for: Small applications, single CPU
   - Pause time: Highest
   - Throughput: Low
   - Use case: Client applications, small heap

2. PARALLEL GC (-XX:+UseParallelGC) [Default in Java 8]
   - Multi-threaded for Young Gen
   - Best for: Batch processing, multi-core
   - Pause time: Medium
   - Throughput: High
   - Use case: Background processing, batch jobs

3. CMS - Concurrent Mark Sweep (-XX:+UseConcMarkSweepGC) [Deprecated in Java 9]
   - Concurrent collector
   - Best for: Low latency applications
   - Pause time: Low
   - Throughput: Medium
   - Use case: Web applications (legacy)

4. G1 GC (-XX:+UseG1GC) [Default since Java 9]
   - Region-based, predictable pause times
   - Best for: Large heaps (>4GB), balanced latency/throughput
   - Pause time: Predictable
   - Throughput: High
   - Use case: Most modern applications

5. Z GC (-XX:+UseZGC) [Java 11+]
   - Concurrent, ultra-low latency
   - Best for: Very large heaps (TB+), <10ms pauses
   - Pause time: <10ms
   - Throughput: High
   - Use case: Large-scale, latency-sensitive applications

6. SHENANDOAH GC (-XX:+UseShenandoahGC) [Java 12+]
   - Concurrent, low pause times
   - Best for: Large heaps, predictable latency
   - Pause time: <10ms
   - Throughput: High
   - Use case: Similar to ZGC

7. EPSILON GC (-XX:+UseEpsilonGC) [Java 11+]
   - No-op collector (no GC)
   - Best for: Short-lived applications, performance testing
   - Pause time: None (until OOM)
   - Throughput: Maximum
   - Use case: Testing, micro-benchmarks
*/
