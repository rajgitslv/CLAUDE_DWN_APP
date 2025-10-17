package com.claude;

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
