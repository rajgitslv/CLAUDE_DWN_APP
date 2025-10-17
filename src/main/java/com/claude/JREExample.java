package com.claude;

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