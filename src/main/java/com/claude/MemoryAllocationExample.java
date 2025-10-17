package com.claude;

// Example demonstrating memory allocation patterns
public class MemoryAllocationExample {

    private static final int MB = 1024 * 1024;

    static class LargeObject {
        private byte[] data;
        private long timestamp;

        public LargeObject(int sizeInMB) {
            this.data = new byte[sizeInMB * MB];
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        printMemoryStats("Initial");

        // 1. Small objects - allocated in Eden
        createSmallObjects();
        printMemoryStats("After small objects");

        // 2. Large objects - may be allocated directly in Old Gen
        createLargeObjects();
        printMemoryStats("After large objects");

        // 3. Trigger GC
        System.gc();
        printMemoryStats("After GC");

        // 4. Create short-lived objects (will be collected quickly)
        for (int i = 0; i < 1000; i++) {
            createTransientObjects();
        }
        printMemoryStats("After transient objects");
    }

    private static void createSmallObjects() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            list.add("Object " + i);
        }
    }

    private static void createLargeObjects() {
        // Large objects (> PretenureSizeThreshold) go directly to Old Gen
        LargeObject large1 = new LargeObject(10);
        LargeObject large2 = new LargeObject(10);
    }

    private static void createTransientObjects() {
        // These objects die young (garbage collected quickly)
        for (int i = 0; i < 1000; i++) {
            String temp = new String("Temporary " + i);
            temp.length(); // Use it briefly
        }
    }

    private static void printMemoryStats(String phase) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / MB;
        long maxMemory = runtime.maxMemory() / MB;
        long totalMemory = runtime.totalMemory() / MB;

        System.out.println("\n=== " + phase + " ===");
        System.out.println("Used Memory:  " + usedMemory + " MB");
        System.out.println("Total Memory: " + totalMemory + " MB");
        System.out.println("Max Memory:   " + maxMemory + " MB");
    }
}

/*
Run with heap analysis:
java -Xms256m -Xmx512m \
     -XX:NewSize=128m \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     MemoryAllocationExample

Key Parameters:
-Xms<size>              Initial heap size
-Xmx<size>              Maximum heap size
-Xmn<size>              Young generation size
-XX:NewRatio=<ratio>    Old/Young generation ratio
-XX:SurvivorRatio=<ratio> Eden/Survivor ratio
*/
