package com.claude;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;

public class GCTroubleshooting {

    public static void main(String[] args) throws Exception {
        GCAnalyzer analyzer = new GCAnalyzer();
        analyzer.startMonitoring();

        // Simulate application workload
        simulateWorkload();

        analyzer.printReport();
        provideOptimizationRecommendations();
    }

    static class GCAnalyzer {
        private Map<String, GCStats> gcStats = new ConcurrentHashMap<>();
        private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void startMonitoring() {
            List<GarbageCollectorMXBean> gcBeans =
                    ManagementFactory.getGarbageCollectorMXBeans();

            // Initialize stats
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                gcStats.put(gcBean.getName(), new GCStats());
            }

            // Monitor every second
            scheduler.scheduleAtFixedRate(() -> {
                for (GarbageCollectorMXBean gcBean : gcBeans) {
                    GCStats stats = gcStats.get(gcBean.getName());
                    stats.update(gcBean.getCollectionCount(), gcBean.getCollectionTime());
                }
            }, 0, 1, TimeUnit.SECONDS);
        }

        public void printReport() {
            scheduler.shutdown();

            System.out.println("\n=== GC ANALYSIS REPORT ===\n");

            for (Map.Entry<String, GCStats> entry : gcStats.entrySet()) {
                System.out.println("Collector: " + entry.getKey());
                GCStats stats = entry.getValue();
                System.out.println("  Total Collections: " + stats.totalCollections);
                System.out.println("  Total Time: " + stats.totalTime + " ms");
                System.out.println("  Avg Pause: " + stats.getAveragePause() + " ms");
                System.out.println("  Max Pause: " + stats.maxPause + " ms");
                System.out.println("  GC Frequency: " + stats.getFrequency() + " /sec");
                System.out.println();
            }

            // Memory analysis
            analyzeMemory();
        }

        private void analyzeMemory() {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

            System.out.println("=== MEMORY ANALYSIS ===\n");
            System.out.println("Heap Memory:");
            printMemoryUsage(heapUsage);
            System.out.println("\nNon-Heap Memory:");
            printMemoryUsage(nonHeapUsage);

            // Check for issues
            double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            if (heapUsagePercent > 80) {
                System.out.println("\n⚠️  WARNING: High heap usage (" +
                        String.format("%.1f%%", heapUsagePercent) + ")");
            }
        }

        private void printMemoryUsage(MemoryUsage usage) {
            long MB = 1024 * 1024;
            System.out.println("  Used: " + usage.getUsed() / MB + " MB");
            System.out.println("  Committed: " + usage.getCommitted() / MB + " MB");
            System.out.println("  Max: " + usage.getMax() / MB + " MB");
            System.out.println("  Usage: " +
                    String.format("%.1f%%", (double) usage.getUsed() / usage.getMax() * 100));
        }
    }

    static class GCStats {
        long totalCollections = 0;
        long totalTime = 0;
        long maxPause = 0;
        long lastCollection = 0;
        long startTime = System.currentTimeMillis();

        void update(long collections, long time) {
            long pauseTime = time - totalTime;
            if (pauseTime > maxPause) {
                maxPause = pauseTime;
            }
            totalCollections = collections;
            totalTime = time;
        }

        double getAveragePause() {
            return totalCollections > 0 ? (double) totalTime / totalCollections : 0;
        }

        double getFrequency() {
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed > 0 ? totalCollections / (elapsed / 1000.0) : 0;
        }
    }

    private static void simulateWorkload() throws Exception {
        List<byte[]> cache = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 5000; i++) {
            // Create objects
            cache.add(new byte[random.nextInt(1024 * 100)]); // 0-100KB

            // Occasionally clear old data
            if (i % 1000 == 0) {
                cache.subList(0, cache.size() / 2).clear();
            }

            Thread.sleep(1);
        }
    }

    private static void provideOptimizationRecommendations() {
        System.out.println("""
            
            ═══════════════════════════════════════════
            GC TROUBLESHOOTING CHECKLIST
            ═══════════════════════════════════════════
            
            1. IDENTIFY THE PROBLEM:
               □ Frequent GC pauses?
               □ Long GC pauses?
               □ High GC overhead?
               □ OutOfMemoryError?
               □ Memory leak?
            
            2. COLLECT DATA:
               □ Enable GC logging
               □ Take heap dumps
               □ Monitor with JMX
               □ Use profilers (VisualVM, JProfiler)
               □ Analyze GC logs (GCViewer, GCEasy)
            
            3. ANALYZE SYMPTOMS:
            
               HIGH GC FREQUENCY:
               • Heap too small → Increase -Xmx
               • Too many short-lived objects → Optimize code
               • Young gen too small → Increase -Xmn
            
               LONG GC PAUSES:
               • Full GC occurring → Tune Old generation
               • Heap fragmentation → Use G1/ZGC
               • Large heap → Consider ZGC for <10ms pauses
            
               HIGH GC OVERHEAD (>5% CPU time in GC):
               • Heap undersized → Increase heap
               • Memory leak → Analyze heap dump
               • Wrong GC algorithm → Switch to G1/ZGC
            
               FREQUENT FULL GC:
               • Old gen filling up → Memory leak?
               • Premature promotion → Increase Young gen
               • Metaspace full → Increase -XX:MaxMetaspaceSize
            
            4. TUNING STRATEGIES:
            
               FOR LOW LATENCY (<100ms pauses):
               -XX:+UseG1GC -XX:MaxGCPauseMillis=50
               OR
               -XX:+UseZGC (Java 11+)
            
               FOR HIGH THROUGHPUT:
               -XX:+UseParallelGC
               -XX:GCTimeRatio=99
            
               FOR LARGE HEAPS (>32GB):
               -XX:+UseZGC
               -XX:+UseLargePages
            
            5. HEAP SIZING:
               • -Xms = -Xmx (avoid heap resizing)
               • Young gen = 25-50% of heap
               • Heap should be 3-4x peak live data size
            
            6. MONITORING COMMANDS:
               jstat -gc <pid> 1000      # GC stats every 1s
               jmap -heap <pid>          # Heap configuration
               jcmd <pid> GC.heap_info   # Detailed heap info
            
            7. EMERGENCY ACTIONS:
               • Take thread dump: kill -3 <pid>
               • Take heap dump: jmap -dump:file=heap.hprof <pid>
               • Force GC: jcmd <pid> GC.run
            
            8. PREVENTION:
               □ Regular heap dump analysis
               □ Monitor GC metrics in production
               □ Load testing with GC tuning
               □ Set up alerts for GC anomalies
               □ Review code for object creation patterns
            """);
    }
}

/*
REAL-WORLD GC TUNING EXAMPLES:

1. WEB APPLICATION (Low Latency):
   java -Xms4g -Xmx4g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=100 \
        -XX:+ParallelRefProcEnabled \
        -Xlog:gc*:file=gc.log:time \
        -jar webapp.jar

2. BATCH PROCESSING (High Throughput):
   java -Xms8g -Xmx8g \
        -XX:+UseParallelGC \
        -XX:ParallelGCThreads=8 \
        -XX:+UseAdaptiveSizePolicy \
        -jar batch.jar

3. MICROSERVICE (Small Heap):
   java -Xms256m -Xmx512m \
        -XX:+UseSerialGC \
        -jar microservice.jar

4. BIG DATA (Very Large Heap):
   java -Xms32g -Xmx32g \
        -XX:+UseZGC \
        -XX:+UseLargePages \
        -XX:ZCollectionInterval=120 \
        -jar dataprocessor.jar
*/
