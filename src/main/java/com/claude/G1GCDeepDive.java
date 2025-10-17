package com.claude;

import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

// G1 GC Deep Dive Example
public class G1GCDeepDive {

    private static final int MB = 1024 * 1024;

    public static void main(String[] args) throws Exception {
        System.out.println("=== G1 GC Demonstration ===\n");

        explainG1Regions();
        demonstrateG1Phases();
        showG1Tuning();
    }

    private static void explainG1Regions() {
        System.out.println("""
            G1 GC ARCHITECTURE:
            ═══════════════════════════════════════════
            
            Heap divided into equal-sized regions (1-32MB):
            
            ┌────┬────┬────┬────┬────┬────┬────┬────┐
            │ E  │ E  │ S  │ O  │ O  │ E  │ H  │ F  │
            ├────┼────┼────┼────┼────┼────┼────┼────┤
            │ E  │ O  │ S  │ E  │ O  │ E  │ O  │ F  │
            └────┴────┴────┴────┴────┴────┴────┴────┘
            
            E = Eden regions
            S = Survivor regions
            O = Old regions
            H = Humongous (large objects > 50% region size)
            F = Free regions
            
            Key Features:
            1. Predictable pause times (-XX:MaxGCPauseMillis)
            2. No fixed Young/Old generation sizes
            3. Concurrent marking
            4. Evacuation pauses (copying)
            5. Incremental compaction
            """);
    }

    private static void demonstrateG1Phases() throws Exception {
        System.out.println("""
            G1 GC PHASES:
            ═══════════════════════════════════════════
            
            1. YOUNG GC (Stop-the-World):
               - Collects Eden and Survivor regions
               - Fast (few milliseconds)
               - Frequent
            
            2. CONCURRENT MARKING:
               - Initial Mark (STW) - marks GC roots
               - Root Region Scanning (concurrent)
               - Concurrent Marking (concurrent)
               - Remark (STW) - finalize marking
               - Cleanup (STW/Concurrent)
            
            3. MIXED GC (Stop-the-World):
               - Collects Young + some Old regions
               - Based on marking information
               - Selects regions with most garbage
            
            4. FULL GC (Stop-the-World):
               - Last resort, avoid if possible
               - Compacts entire heap
               - Can take seconds
            """);

        // Monitor GC activity
        monitorG1Activity();
    }

    private static void monitorG1Activity() throws Exception {
        System.out.println("\n=== Monitoring G1 GC Activity ===\n");

        // Get memory pools
        List<MemoryPoolMXBean> pools =
                java.lang.management.ManagementFactory.getMemoryPoolMXBeans();

        for (java.lang.management.MemoryPoolMXBean pool : pools) {
            if (pool.getName().contains("G1")) {
                System.out.println("Pool: " + pool.getName());
                System.out.println("Type: " + pool.getType());

                java.lang.management.MemoryUsage usage = pool.getUsage();
                System.out.println("Used: " + usage.getUsed() / MB + " MB");
                System.out.println("Committed: " + usage.getCommitted() / MB + " MB");
                System.out.println("Max: " + usage.getMax() / MB + " MB");
                System.out.println();
            }
        }

        // Create workload
        createG1Workload();
    }

    private static void createG1Workload() throws Exception {
        List<Object> youngObjects = new ArrayList<>();
        List<Object> oldObjects = new ArrayList<>();

        System.out.println("Creating workload to demonstrate G1 phases...\n");

        // Create Young generation objects (will be collected quickly)
        for (int i = 0; i < 1000; i++) {
            youngObjects.add(new byte[10 * 1024]); // 10KB objects

            if (i % 100 == 0) {
                // Some objects survive to Old generation
                oldObjects.add(youngObjects.get(i));
            }
        }

        // Create humongous objects (> 50% of region size)
        for (int i = 0; i < 5; i++) {
            oldObjects.add(new byte[2 * MB]); // 2MB each
        }

        // Clear young objects
        youngObjects.clear();

        // Suggest GC
        System.gc();
        Thread.sleep(1000);

        printGCStats();
    }

    private static void printGCStats() {
        List<java.lang.management.GarbageCollectorMXBean> gcBeans =
                java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();

        System.out.println("=== GC Statistics ===");
        for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("\nCollector: " + gcBean.getName());
            System.out.println("Collections: " + gcBean.getCollectionCount());
            System.out.println("Time: " + gcBean.getCollectionTime() + " ms");
        }
    }

    private static void showG1Tuning() {
        System.out.println("""
            
            ═══════════════════════════════════════════
            G1 GC TUNING PARAMETERS
            ═══════════════════════════════════════════
            
            BASIC CONFIGURATION:
            -XX:+UseG1GC                          Enable G1
            -Xms4g -Xmx4g                         Heap size (keep equal)
            -XX:MaxGCPauseMillis=200              Target pause time (default 200ms)
            
            REGION SIZE:
            -XX:G1HeapRegionSize=16m              Region size (1-32MB, power of 2)
            
            YOUNG GENERATION:
            -XX:G1NewSizePercent=5                Min young gen (% of heap)
            -XX:G1MaxNewSizePercent=60            Max young gen (% of heap)
            
            MIXED GC:
            -XX:InitiatingHeapOccupancyPercent=45 Start marking at 45% heap
            -XX:G1MixedGCCountTarget=8            Target mixed GC count
            -XX:G1MixedGCLiveThresholdPercent=85  Region with >85% live not collected
            
            HUMONGOUS OBJECTS:
            -XX:G1HeapWastePercent=5              Acceptable waste in heap
            
            LOGGING (Java 9+):
            -Xlog:gc*:file=gc.log:time,uptime,level,tags
            -Xlog:gc+heap=trace
            
            LEGACY LOGGING (Java 8):
            -XX:+PrintGCDetails
            -XX:+PrintGCTimeStamps
            -XX:+PrintGCDateStamps
            -Xloggc:gc.log
            
            MONITORING:
            -XX:+UnlockDiagnosticVMOptions
            -XX:+G1SummarizeRSetStats
            -XX:G1SummarizeRSetStatsPeriod=1
            
            EXAMPLE PRODUCTION CONFIG:
            java -Xms8g -Xmx8g \\
                 -XX:+UseG1GC \\
                 -XX:MaxGCPauseMillis=100 \\
                 -XX:G1HeapRegionSize=16m \\
                 -XX:InitiatingHeapOccupancyPercent=40 \\
                 -XX:+ParallelRefProcEnabled \\
                 -Xlog:gc*:file=gc.log:time \\
                 -jar myapp.jar
            """);
    }
}
