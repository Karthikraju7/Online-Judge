package com.karthikd.server.util;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaExecutor {

    public static File compileJava(String code, String identifier) throws IOException, InterruptedException {
        String className = extractClassName(code);
        if (className == null) {
            throw new RuntimeException("Could not find public class name in submitted code.");
        }

        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        String hostDir = new File("codes/" + safeIdentifier).getAbsolutePath();
        new File(hostDir).mkdirs();

        File javaFile = new File(hostDir, className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }

        WarmContainerManager.compileJava(safeIdentifier, className + ".java");

        return new File(hostDir);
    }

    public static String runJava(File codeDir, String input, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        return WarmContainerManager.runJava(safeIdentifier, findClassName(codeDir), input);
    }

    public static List<String> runAllTestCases(File codeDir, List<String> inputs, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        return WarmContainerManager.runJavaBatch(safeIdentifier, findClassName(codeDir), inputs);
    }

    private static String findClassName(File codeDir) {
        File[] classFiles = codeDir.listFiles((dir, name) -> name.endsWith(".class") && !name.contains("$"));
        if (classFiles == null || classFiles.length == 0) return "Main";
        for (File f : classFiles) {
            if (f.getName().equals("Main.class")) return "Main";
        }
        return classFiles[0].getName().replace(".class", "");
    }

    private static String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group(1) : null;
    }
}