package com.claude;

// Example showing JIT compilation impact
public class JITCompilationExample {

    private static final int ITERATIONS = 100_000;

    public static void main(String[] args) {
        // Warm-up phase - triggers JIT compilation
        System.out.println("=== Warm-up Phase ===");
        long warmupTime = benchmarkMethod(ITERATIONS);
        System.out.println("Warm-up time: " + warmupTime + " ms");

        // After JIT compilation
        System.out.println("\n=== After JIT Compilation ===");
        long optimizedTime = benchmarkMethod(ITERATIONS);
        System.out.println("Optimized time: " + optimizedTime + " ms");

        System.out.println("\nSpeedup: " + (warmupTime / (double) optimizedTime) + "x");
    }

    private static long benchmarkMethod(int iterations) {
        long start = System.currentTimeMillis();

        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += computeIntensive(i);
        }

        long end = System.currentTimeMillis();
        System.out.println("Result: " + result);
        return end - start;
    }

    // This method will be JIT compiled after sufficient invocations
    private static long computeIntensive(int n) {
        long sum = 0;
        for (int i = 0; i < 10000; i++) {
            sum += (n * i) % 997;  // Some computation
        }
        return sum;
    }
}

/*
JIT Tuning Parameters:

-XX:+PrintCompilation          # Print JIT compilation
-XX:CompileThreshold=10000     # Method invocation threshold for JIT
-XX:+TieredCompilation         # Enable tiered compilation (default)
-XX:TieredStopAtLevel=1        # Stop at C1 compiler (faster startup)
-XX:+UnlockDiagnosticVMOptions
-XX:+PrintInlining             # Show inlining decisions

Disable JIT (for testing):
-Xint                          # Interpreter only mode

Force JIT immediately:
-Xcomp                         # Compile all methods immediately
*/
