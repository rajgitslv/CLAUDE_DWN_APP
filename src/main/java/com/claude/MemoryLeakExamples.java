package com.claude;

import java.util.*;
import java.lang.ref.*;

// Examples of common memory leak patterns
public class MemoryLeakExamples {

    // LEAK 1: Static collections never cleared
    private static List<Object> staticCache = new ArrayList<>();

    public void leakViaStaticCollection() {
        staticCache.add(new byte[1024 * 1024]); // 1MB
        // staticCache grows indefinitely if not cleared
    }

    // LEAK 2: Improper HashMap/HashSet usage
    static class LeakyKey {
        private String data;

        public LeakyKey(String data) {
            this.data = data;
        }

        // Missing hashCode() and equals() can cause leaks
        // Objects won't be found for removal
    }

    private Map<LeakyKey, String> leakyMap = new HashMap<>();

    public void leakViaHashMap() {
        LeakyKey key = new LeakyKey("data");
        leakyMap.put(key, "value");
        // Can't remove later because hashCode/equals not implemented
    }

    // LEAK 3: Unclosed resources
    public void leakViaUnclosedResources() {
        try {
            java.sql.Connection conn = null; // Get connection
            // Forgot to close - connection pool leak
        } catch (Exception e) {
            // Connection never closed
        }
    }

    // LEAK 4: Inner class holding outer class reference
    public class InnerClassLeak {
        private byte[] data = new byte[1024 * 1024];

        public void startThread() {
            // Anonymous inner class holds reference to outer class
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        // Long-running thread prevents GC of outer class
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                    }
                }
            }).start();
        }
    }

    // LEAK 5: ThreadLocal not removed
    private static ThreadLocal<List<Object>> threadLocalCache = new ThreadLocal<>();

    public void leakViaThreadLocal() {
        List<Object> cache = new ArrayList<>();
        cache.add(new byte[1024 * 1024]);
        threadLocalCache.set(cache);
        // If thread is reused (thread pool) and not cleaned, leak occurs
    }

    // PROPER SOLUTION: Use WeakReference for caches
    private Map<String, WeakReference<Object>> properCache = new WeakHashMap<>();

    public void properCacheUsage(String key, Object value) {
        properCache.put(key, new WeakReference<>(value));
        // Objects can be GC'd when no strong references exist
    }

    // Detection and Monitoring
    public static void main(String[] args) throws Exception {
        MemoryLeakExamples example = new MemoryLeakExamples();

        // Monitor memory usage
        MemoryMonitor monitor = new MemoryMonitor();
        monitor.start();

        // Simulate leak
        System.out.println("Creating memory leak...");
        for (int i = 0; i < 1000; i++) {
            example.leakViaStaticCollection();
            Thread.sleep(10);
        }

        // Try to trigger GC
        System.gc();
        Thread.sleep(1000);

        monitor.printReport();
    }

    static class MemoryMonitor {
        private long startTime;
        private long startMemory;

        public void start() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            this.startTime = System.currentTimeMillis();
            this.startMemory = runtime.totalMemory() - runtime.freeMemory();
        }

        public void printReport() {
            Runtime runtime = Runtime.getRuntime();
            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = (currentMemory - startMemory) / (1024 * 1024);
            long timeElapsed = System.currentTimeMillis() - startTime;

            System.out.println("\n=== Memory Leak Report ===");
            System.out.println("Time Elapsed: " + timeElapsed + " ms");
            System.out.println("Memory Increase: " + memoryIncrease + " MB");
            System.out.println("Current Memory: " + currentMemory / (1024 * 1024) + " MB");

            if (memoryIncrease > 100) {
                System.out.println("⚠️  WARNING: Possible memory leak detected!");
            }
        }
    }
}

/*
Memory Leak Detection Tools:

1. Heap Dump Analysis:
   jmap -dump:live,format=b,file=heap.bin <pid>

2. Memory Profiling:
   java -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=/logs/heap.hprof \
        -XX:+PrintGCDetails \
        MyApp

3. Using JVisualVM:
   - Attach to process
   - Take heap dump
   - Analyze largest objects
   - Find GC roots

4. Eclipse Memory Analyzer (MAT):
   - Open heap dump
   - Run "Leak Suspects" report
   - Analyze dominator tree

5. JProfiler / YourKit
*/
