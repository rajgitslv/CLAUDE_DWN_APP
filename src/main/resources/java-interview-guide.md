# Complete Java Interview Guide: JDK, JRE, JVM, JIT, Memory & GC
## For Senior Staff Engineer & Java Architect Positions

---

## 1. JDK (Java Development Kit)

### Basic Questions

**Q1: What is JDK and what does it contain?**

**Answer:**
JDK (Java Development Kit) is a software development environment used for developing Java applications. It's a superset that contains:
- **JRE (Java Runtime Environment)** - for running Java applications
- **Development Tools** - compiler (javac), debugger (jdb), archiver (jar), documentation generator (javadoc)
- **Java Libraries** - Standard class libraries and APIs
- **Tools** - monitoring tools like jconsole, jvisualvm, jstat

```
JDK Structure:
├── JRE
│   ├── JVM
│   └── Runtime Libraries
├── javac (compiler)
├── javadoc
├── jar
├── jdb (debugger)
└── Other development tools
```

### Intermediate Questions

**Q2: What's the difference between JDK, JRE, and JVM?**

**Answer:**
- **JDK** = Development platform (JRE + Development Tools)
- **JRE** = Runtime environment (JVM + Runtime Libraries)
- **JVM** = Execution engine (runs bytecode)

**Relationship:** JDK ⊃ JRE ⊃ JVM

### Advanced Questions

**Q3: Explain the role of JDK in the Java compilation and execution process.**

**Answer:**
1. **Compilation Phase (JDK)**:
   - `javac` compiles `.java` source files into `.class` bytecode
   - Bytecode is platform-independent
   
2. **Execution Phase (JRE/JVM)**:
   - ClassLoader loads `.class` files
   - Bytecode Verifier ensures security
   - JIT compiler converts bytecode to native machine code
   - Execution Engine runs the native code

```java
// Example demonstrating JDK tools usage
public class JDKExample {
    /**
     * This method demonstrates JDK functionality
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Compiled with javac");
        System.out.println("Documented with javadoc");
        System.out.println("Packaged with jar");
        
        // Get runtime information
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JVM Name: " + System.getProperty("java.vm.name"));
        System.out.println("JDK Vendor: " + System.getProperty("java.vendor"));
    }
}

/*
Commands to use JDK tools:
1. javac JDKExample.java          // Compile
2. javadoc JDKExample.java        // Generate documentation
3. jar cf example.jar *.class     // Create JAR
4. java -cp example.jar JDKExample // Run
*/
```

---

## 2. JRE (Java Runtime Environment)

### Basic Questions

**Q4: What is JRE and when do you need it?**

**Answer:**
JRE provides the minimum requirements to execute a Java application. It contains:
- JVM (Java Virtual Machine)
- Core libraries (rt.jar)
- Supporting files

You need JRE to run Java applications but NOT to develop them.

### Intermediate Questions

**Q5: Can you run Java applications without JRE?**

**Answer:**
Technically, yes, using:
1. **GraalVM Native Image** - Compiles to native executable
2. **Custom JRE (jlink)** - Create minimal runtime image
3. **Embedded JVM** - In containerized environments

```java
// Example showing JRE components in action
public class JREExample {
    public static void main(String[] args) {
        // These operations use JRE's core libraries
        
        // 1. String handling (from rt.jar)
        String message = "JRE provides runtime support";
        
        // 2. Collections (from JRE libraries)
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("JVM");
        list.add("Core Libraries");
        
        // 3. I/O operations (JRE support)
        System.out.println(message);
        
        // 4. Math operations (JRE Math library)
        double result = Math.sqrt(16);
        System.out.println("Square root: " + result);
    }
}
```

### Advanced Questions

**Q6: How would you create a custom JRE for a production application?**

**Answer:**
Use `jlink` (Java 9+) to create a minimal runtime image:

```bash
# Analyze module dependencies
jdeps --list-deps MyApplication.jar

# Create custom JRE
jlink --module-path $JAVA_HOME/jmods \
      --add-modules java.base,java.logging,java.sql \
      --output custom-jre \
      --compress=2 \
      --strip-debug

# This reduces size significantly (from ~200MB to ~50MB)
```

---

## 3. JVM (Java Virtual Machine)

### Basic Questions

**Q7: What is JVM and why is it important?**

**Answer:**
JVM is an abstract computing machine that:
- Loads bytecode
- Verifies bytecode
- Executes bytecode
- Provides runtime environment

**Key benefit:** "Write Once, Run Anywhere" (WORA)

```
JVM Architecture:
┌─────────────────────────────────────────┐
│         Class Loader Subsystem          │
├─────────────────────────────────────────┤
│              Memory Areas               │
│  ┌────────┬─────────┬──────────────┐  │
│  │ Method │  Heap   │    Stacks    │  │
│  │  Area  │         │   (Thread)   │  │
│  └────────┴─────────┴──────────────┘  │
├─────────────────────────────────────────┤
│          Execution Engine               │
│  ┌──────────┬─────────┬────────────┐  │
│  │Interpreter│   JIT   │    GC      │  │
│  └──────────┴─────────┴────────────┘  │
└─────────────────────────────────────────┘
```

### Intermediate Questions

**Q8: Explain the JVM architecture in detail.**

**Answer:**

**1. Class Loader Subsystem:**
   - **Loading:** Bootstrap → Extension → Application class loaders
   - **Linking:** Verification → Preparation → Resolution
   - **Initialization:** Execute static initializers

**2. Memory Areas:**
   - **Method Area:** Class metadata, static variables
   - **Heap:** Object instances
   - **Stack:** Method frames, local variables
   - **PC Register:** Current instruction address
   - **Native Method Stack:** Native method information

**3. Execution Engine:**
   - **Interpreter:** Executes bytecode line-by-line
   - **JIT Compiler:** Converts hot code to native code
   - **Garbage Collector:** Manages memory

```java
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
```

### Advanced Questions

**Q9: How does ClassLoader hierarchy work? Can you write a custom ClassLoader?**

**Answer:**

**ClassLoader Hierarchy:**
```
Bootstrap ClassLoader (C++)
    ↓ parent
Extension ClassLoader (Java)
    ↓ parent
Application ClassLoader (Java)
    ↓ parent
Custom ClassLoaders (User-defined)
```

**Delegation Model:** Child delegates to parent first before loading itself.

```java
// Custom ClassLoader implementation
public class CustomClassLoader extends ClassLoader {
    
    private String classPath;
    
    public CustomClassLoader(String classPath) {
        this.classPath = classPath;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] classData = loadClassData(name);
            if (classData == null) {
                throw new ClassNotFoundException();
            }
            return defineClass(name, classData, 0, classData.length);
        } catch (Exception e) {
            throw new ClassNotFoundException("Cannot load class: " + name, e);
        }
    }
    
    private byte[] loadClassData(String className) throws Exception {
        String fileName = classPath + "/" + className.replace('.', '/') + ".class";
        java.io.InputStream inputStream = new java.io.FileInputStream(fileName);
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        
        int nextValue;
        while ((nextValue = inputStream.read()) != -1) {
            byteStream.write(nextValue);
        }
        
        inputStream.close();
        return byteStream.toByteArray();
    }
    
    public static void main(String[] args) throws Exception {
        CustomClassLoader loader = new CustomClassLoader("/custom/classes");
        
        // Load class using custom loader
        Class<?> loadedClass = loader.loadClass("com.example.MyClass");
        
        // Verify classloader
        System.out.println("Class loaded by: " + loadedClass.getClassLoader());
        System.out.println("Parent loader: " + loadedClass.getClassLoader().getParent());
        
        // Create instance
        Object instance = loadedClass.getDeclaredConstructor().newInstance();
    }
}
```

**Q10: Explain JVM tuning parameters for production systems.**

**Answer:**

```java
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
```

---

## 4. JIT (Just-In-Time Compiler)

### Basic Questions

**Q11: What is JIT compiler and why is it important?**

**Answer:**
JIT compiler converts frequently executed bytecode (hot spots) into native machine code at runtime, improving performance significantly.

**Process:**
1. Code initially interpreted
2. JVM profiles execution
3. Hot methods identified
4. JIT compiles to native code
5. Native code cached and reused

### Intermediate Questions

**Q12: What are the different JIT compilation levels?**

**Answer:**

**Tiered Compilation (Default since Java 8):**
- **Level 0:** Interpreter
- **Level 1:** C1 compiler with simple optimizations
- **Level 2:** C1 with invocation and backedge counters
- **Level 3:** C1 with full profiling
- **Level 4:** C2 compiler with aggressive optimizations

```java
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
        for (int i = 0; i < 1000; i++) {
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
```

### Advanced Questions

**Q13: Explain JIT optimization techniques.**

**Answer:**

**Key JIT Optimizations:**

1. **Method Inlining:** Replace method call with method body
2. **Dead Code Elimination:** Remove unused code
3. **Loop Unrolling:** Reduce loop overhead
4. **Escape Analysis:** Stack allocation instead of heap
5. **Lock Elision:** Remove unnecessary synchronization
6. **Intrinsics:** Replace method calls with CPU instructions

```java
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
```

**Q14: How to diagnose JIT compilation issues?**

```java
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
```

---

## 5. Java Memory Management

### Basic Questions

**Q15: Explain Java memory model and heap structure.**

**Answer:**

```
Java Heap Structure:
┌────────────────────────────────────────────┐
│              Young Generation              │
│  ┌────────┬─────────────┬───────────────┐│
│  │  Eden  │ Survivor S0 │ Survivor S1   ││
│  │  Space │             │               ││
│  └────────┴─────────────┴───────────────┘│
├────────────────────────────────────────────┤
│            Old Generation                  │
│         (Tenured Space)                    │
│                                            │
└────────────────────────────────────────────┘

Non-Heap:
┌────────────────────────────────────────────┐
│  Metaspace (Class metadata, Java 8+)      │
│  Code Cache (JIT compiled code)           │
│  Thread Stacks                             │
└────────────────────────────────────────────┘
```

### Intermediate Questions

**Q16: How does object allocation work in Java?**

**Answer:**

**Allocation Process:**
1. New objects allocated in **Eden space**
2. When Eden fills → **Minor GC** triggered
3. Surviving objects → **Survivor space** (S0/S1)
4. After multiple GC cycles → promoted to **Old Generation**
5. When Old Gen fills → **Major GC/Full GC**

```java
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
```

### Advanced Questions

**Q17: Explain memory leaks in Java and how to detect them.**

**Answer:**

```java
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
```

**Q18: Explain the difference between Stack and Heap memory.**

```java
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
```

---

## 6. Garbage Collection (GC)

### Basic Questions

**Q19: What is Garbage Collection and why is it needed?**

**Answer:**
Garbage Collection is automatic memory management in Java that:
- Identifies unused objects (no references)
- Reclaims memory occupied by dead objects
- Prevents memory leaks
- Eliminates manual memory management (no free/delete)

**Process:**
1. **Mark** - Identify live objects
2. **Sweep** - Remove dead objects
3. **Compact** - Defragment memory (optional)

### Intermediate Questions

**Q20: Explain different types of Garbage Collectors in Java.**

**Answer:**

```java
// Example demonstrating GC types and behaviors
public class GarbageCollectorTypes {
    
    public static void main(String[] args) {
        printGCInfo();
        demonstrateGCBehavior();
    }
    
    private static void printGCInfo() {
        System.out.println("=== Garbage Collector Information ===\n");
        
        // Get GC beans
        List<java.lang.management.GarbageCollectorMXBean> gcBeans = 
            java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        
        for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("GC Name: " + gcBean.getName());
            System.out.println("Collection Count: " + gcBean.getCollectionCount());
            System.out.println("Collection Time: " + gcBean.getCollectionTime() + " ms");
            System.out.println("Memory Pools: " + Arrays.toString(gcBean.getMemoryPoolNames()));
            System.out.println();
        }
    }
    
    private static void demonstrateGCBehavior() {
        Runtime runtime = Runtime.getRuntime();
        
        System.out.println("=== Creating Objects to Trigger GC ===");
        printMemory("Before allocation", runtime);
        
        // Create many objects
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new byte[1024 * 1024]); // 1MB each
            
            if (i % 20 == 0) {
                printMemory("After " + i + " MB", runtime);
            }
        }
        
        // Clear references
        list.clear();
        list = null;
        
        printMemory("After clearing references", runtime);
        
        // Suggest GC (not guaranteed to run immediately)
        System.gc();
        
        try {
            Thread.sleep(1000); // Give GC time to run
        } catch (InterruptedException e) {}
        
        printMemory("After System.gc()", runtime);
    }
    
    private static void printMemory(String label, Runtime runtime) {
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMB = runtime.totalMemory() / 1024 / 1024;
        long maxMB = runtime.maxMemory() / 1024 / 1024;
        
        System.out.printf("%s: Used=%dMB, Total=%dMB, Max=%dMB%n", 
                         label, usedMB, totalMB, maxMB);
    }
}

/*
==========================================
GARBAGE COLLECTOR TYPES
==========================================

1. SERIAL GC (-XX:+UseSerialGC)
   - Single-threaded
   - Best for: Small applications, single CPU
   - Pause time: Highest
   - Throughput: Low
   - Use case: Client applications, small heap

2. PARALLEL GC (-XX:+UseParallelGC) [Default in Java 8]
   - Multi-threaded for Young Gen
   - Best for: Batch processing, multi-core
   - Pause time: Medium
   - Throughput: High
   - Use case: Background processing, batch jobs

3. CMS - Concurrent Mark Sweep (-XX:+UseConcMarkSweepGC) [Deprecated in Java 9]
   - Concurrent collector
   - Best for: Low latency applications
   - Pause time: Low
   - Throughput: Medium
   - Use case: Web applications (legacy)

4. G1 GC (-XX:+UseG1GC) [Default since Java 9]
   - Region-based, predictable pause times
   - Best for: Large heaps (>4GB), balanced latency/throughput
   - Pause time: Predictable
   - Throughput: High
   - Use case: Most modern applications

5. Z GC (-XX:+UseZGC) [Java 11+]
   - Concurrent, ultra-low latency
   - Best for: Very large heaps (TB+), <10ms pauses
   - Pause time: <10ms
   - Throughput: High
   - Use case: Large-scale, latency-sensitive applications

6. SHENANDOAH GC (-XX:+UseShenandoahGC) [Java 12+]
   - Concurrent, low pause times
   - Best for: Large heaps, predictable latency
   - Pause time: <10ms
   - Throughput: High
   - Use case: Similar to ZGC

7. EPSILON GC (-XX:+UseEpsilonGC) [Java 11+]
   - No-op collector (no GC)
   - Best for: Short-lived applications, performance testing
   - Pause time: None (until OOM)
   - Throughput: Maximum
   - Use case: Testing, micro-benchmarks
*/
```

### Advanced Questions

**Q21: Deep dive into G1 Garbage Collector - How does it work?**

**Answer:**

```java
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
        List<java.lang.management.MemoryPoolMXBean> pools = 
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
```

**Q22: How would you troubleshoot and optimize GC for a production application?**

```java
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
```

---

## Summary: Key Interview Points

### For Senior Staff Engineer:
1. **Deep understanding** of JVM internals
2. **Experience** with GC tuning in production
3. **Ability** to diagnose and fix memory issues
4. **Knowledge** of different GC algorithms and when to use them
5. **Expertise** in JVM monitoring and profiling tools

### For Java Architect:
1. **Strategic decisions** on JVM configurations for different services
2. **Design patterns** for memory-efficient applications
3. **Capacity planning** based on JVM metrics
4. **Migration strategies** for JVM upgrades
5. **Best practices** for containerized Java applications

### Critical Metrics to Monitor:
- GC pause times and frequency
- Heap utilization
- Thread count and CPU usage
- Application throughput
- Response time percentiles (P50, P95, P99)

### Tools Every Senior Engineer Should Know:
- **jstat, jmap, jcmd** - Command-line tools
- **VisualVM, JMC** - GUI profilers
- **Eclipse MAT** - Heap dump analysis
- **GCViewer, GCEasy** - GC log analysis
- **JProfiler, YourKit** - Commercial profilers

---

## Interview Tips:

1. **Always start with basics** before diving deep
2. **Use concrete examples** from your experience
3. **Explain trade-offs** when discussing GC algorithms
4. **Show production troubleshooting** experience
5. **Demonstrate understanding** of business impact (latency, throughput)
6. **Be ready to write code** for memory-related problems
7. **Know the latest Java features** (Java 17, 21 LTS)
8. **Understand containerization** impacts on JVM (cgroups, heap sizing)
