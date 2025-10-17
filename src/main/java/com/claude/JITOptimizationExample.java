package com.claude;

// Example demonstrating various JIT optimizations
public class JITOptimizationExample {

    // 1. METHOD INLINING
    // Small methods are inlined by JIT
    private static int add(int a, int b) {
        return a + b;  // Will be inlined
    }

    // 2. ESCAPE ANALYSIS
    public static void escapeAnalysisExample() {
        // Object doesn't escape method - can be stack allocated
        Point p = new Point(10, 20);
        int result = p.x + p.y;
        System.out.println(result);
        // p is eligible for scalar replacement (no heap allocation)
    }

    static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // 3. LOCK ELISION
    public void lockElisionExample() {
        // JIT can remove this lock if it detects no contention
        synchronized (this) {
            int x = 10;
            int y = 20;
            System.out.println(x + y);
        }
    }

    // 4. LOOP UNROLLING
    public static long loopUnrollingExample(int n) {
        long sum = 0;
        // JIT may unroll this loop
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    // 5. DEAD CODE ELIMINATION
    public static int deadCodeExample(int x) {
        int unused = 100;  // Will be eliminated
        int y = x * 2;
        int alsoUnused = y + 10;  // Will be eliminated
        return y;  // Only this matters
    }

    // 6. INTRINSICS - System.arraycopy is replaced with native instruction
    public static void intrinsicsExample() {
        int[] src = new int[1000];
        int[] dest = new int[1000];
        // JIT replaces this with optimized native code
        System.arraycopy(src, 0, dest, 0, 1000);
    }

    public static void main(String[] args) {
        // Warm-up to trigger JIT
        for (int i = 0; i < 20_000; i++) {
            add(i, i + 1);
            escapeAnalysisExample();
            loopUnrollingExample(100);
            deadCodeExample(i);
        }

        System.out.println("JIT optimizations applied after warm-up");
    }
}

/*
To observe JIT optimizations:
java -XX:+UnlockDiagnosticVMOptions \
     -XX:+PrintInlining \
     -XX:+PrintCompilation \
     -XX:+PrintEliminateAllocations \
     JITOptimizationExample
*/