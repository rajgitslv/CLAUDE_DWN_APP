package com.claude;

// Example demonstrating JVM memory areas
public class JVMMemoryExample {
    // Stored in Method Area (Class metadata)
    private static int staticVariable = 100;

    // Instance variable (stored in Heap when object created)
    private int instanceVariable;

    public static void main(String[] args) {
        // Local variables stored in Stack
        int localVariable = 10;

        // Object created in Heap, reference in Stack
        JVMMemoryExample obj = new JVMMemoryExample();
        obj.instanceVariable = 20;

        // Method call creates a new frame in Stack
        obj.demonstrateStackFrame(localVariable);

        // Show memory usage
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Max Memory (Heap): " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Total Memory: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("Free Memory: " + runtime.freeMemory() / 1024 / 1024 + " MB");
    }

    private void demonstrateStackFrame(int parameter) {
        // New frame created on Stack with:
        // - Local variables
        // - Operand stack
        // - Frame data (return address, exception handling)
        int localInMethod = parameter * 2;
        System.out.println("Stack frame created for this method");
    }
}
