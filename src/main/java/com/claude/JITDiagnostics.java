package com.claude;

import java.lang.management.ManagementFactory;
import java.lang.management.CompilationMXBean;

public class JITDiagnostics {

    public static void main(String[] args) throws Exception {
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();

        System.out.println("=== JIT Compiler Information ===");
        System.out.println("Compiler Name: " + compilationBean.getName());
        System.out.println("Compilation Time Monitoring Supported: " +
                compilationBean.isCompilationTimeMonitoringSupported());

        // Perform some work
        performWork();

        if (compilationBean.isCompilationTimeMonitoringSupported()) {
            System.out.println("Total Compilation Time: " +
                    compilationBean.getTotalCompilationTime() + " ms");
        }

        // Print detailed compilation logs
        printCompilationStats();
    }

    private static void performWork() {
        long result = 0;
        for (int i = 0; i < 1_000_000; i++) {
            result += fibonacci(20);
        }
        System.out.println("Work done: " + result);
    }

    private static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    private static void printCompilationStats() {
        System.out.println("\n=== Runtime Information ===");
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Available Processors: " + runtime.availableProcessors());
        System.out.println("JVM: " + System.getProperty("java.vm.name"));
        System.out.println("Version: " + System.getProperty("java.version"));
    }
}

/*
Run with diagnostic flags:
java -XX:+PrintCompilation \
     -XX:+UnlockDiagnosticVMOptions \
     -XX:+LogCompilation \
     -XX:LogFile=jit.log \
     JITDiagnostics

Analyze with JITWatch tool: https://github.com/AdoptOpenJDK/jitwatch
*/
