package com.claude;

import java.util.ArrayList;
import java.util.List;

public class StackVsHeapExample {

    // Instance variable - stored in Heap
    private int instanceVar = 10;

    // Static variable - stored in Metaspace (Method Area)
    private static int staticVar = 20;

    public static void main(String[] args) {
        // Local primitive - stored in Stack
        int localPrimitive = 5;

        // Object reference - stored in Stack
        // Actual object - stored in Heap
        StackVsHeapExample obj = new StackVsHeapExample();

        // Demonstrate stack frames
        obj.method1(localPrimitive);

        // Arrays always in Heap (even primitive arrays)
        int[] array = new int[100];

        printMemoryLayout();
    }

    private void method1(int param) {
        // New stack frame created
        int localVar = param * 2;
        String localString = "Stack frame data";

        method2(localVar);

        // Stack frame destroyed when method returns
    }

    private void method2(int param) {
        // Another stack frame
        int result = param + 10;
        System.out.println("Result: " + result);
    }

    private static void printMemoryLayout() {
        System.out.println("\n=== Memory Layout ===");
        System.out.println("""
            Stack Memory:
            - Thread-specific
            - Stores: local variables, method parameters, return addresses
            - Faster access (LIFO structure)
            - Limited size (typically 1MB per thread)
            - Automatic cleanup when method returns
            - Throws StackOverflowError when full
            
            Heap Memory:
            - Shared across threads
            - Stores: objects, instance variables, arrays
            - Slower access than stack
            - Larger size (configured via -Xmx)
            - Managed by Garbage Collector
            - Throws OutOfMemoryError when full
            
            Metaspace (Method Area):
            - Stores: class metadata, static variables, constants
            - Native memory (not in heap)
            - Size: -XX:MaxMetaspaceSize
            """);
    }

    // Example showing stack overflow
    public static void causeStackOverflow(int depth) {
        System.out.println("Depth: " + depth);
        causeStackOverflow(depth + 1); // Infinite recursion
        // Will throw StackOverflowError
    }

    // Example showing heap overflow
    public static void causeHeapOverflow() {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024]); // 1MB each
            // Will throw OutOfMemoryError: Java heap space
        }
    }
}

/*
Stack Configuration:
-Xss<size>              Thread stack size (e.g., -Xss1m)

Heap Configuration:
-Xms<size>              Initial heap
-Xmx<size>              Maximum heap

Example:
java -Xss512k -Xms512m -Xmx2g StackVsHeapExample
*/
