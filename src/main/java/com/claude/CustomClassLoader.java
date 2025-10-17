package com.claude;

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
        Class<?> loadedClass = loader.loadClass("com.claude.JDKExample");

        // Verify classloader
        System.out.println("Class loaded by: " + loadedClass.getClassLoader().getName());
        System.out.println("Parent loader: " + loadedClass.getClassLoader().getParent().getName());

        // Create instance
        Object instance = loadedClass.getDeclaredConstructor().newInstance();
    }
}
