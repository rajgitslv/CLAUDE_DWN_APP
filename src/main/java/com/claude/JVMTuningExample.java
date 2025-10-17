package com.claude;

// JVM Configuration Example for Production
public class JVMTuningExample {
    public static void main(String[] args) {
        printJVMSettings();
    }

    public static void printJVMSettings() {
        Runtime runtime = Runtime.getRuntime();

        System.out.println("=== JVM Memory Settings ===");
        System.out.println("Max Heap: " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Total Heap: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("Free Heap: " + runtime.freeMemory() / 1024 / 1024 + " MB");

        System.out.println("\n=== System Properties ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("GC: " + System.getProperty("java.vm.name"));

        System.out.println("\n=== Processors ===");
        System.out.println("Available Processors: " + runtime.availableProcessors());
    }
}

/*
Key JVM Tuning Parameters:

HEAP SIZING:
-Xms4g              # Initial heap size
-Xmx4g              # Maximum heap size (set equal to Xms for production)
-Xmn1g              # Young generation size

GARBAGE COLLECTION:
-XX:+UseG1GC        # Use G1 collector (default Java 9+)
-XX:+UseZGC         # Use Z Garbage Collector (Java 11+, low latency)
-XX:MaxGCPauseMillis=200  # Target GC pause time

METASPACE:
-XX:MetaspaceSize=256m      # Initial metaspace
-XX:MaxMetaspaceSize=512m   # Maximum metaspace

GC LOGGING (Java 9+):
-Xlog:gc*:file=gc.log:time,uptime,level,tags

PERFORMANCE:
-XX:+UseStringDeduplication  # Reduce String memory footprint
-XX:+OptimizeStringConcat    # Optimize String concatenation

MONITORING:
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/logs/heapdump.hprof

THREAD STACK:
-Xss1m              # Thread stack size

Example Production Command:
java -Xms4g -Xmx4g -Xmn1g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/logs/heap.hprof \
     -Xlog:gc*:file=/logs/gc.log:time \
     -jar myapp.jar
*/