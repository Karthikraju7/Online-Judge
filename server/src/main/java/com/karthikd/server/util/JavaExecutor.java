package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaExecutor {
    public static String runJava(String code, String input, String identifier) throws IOException, InterruptedException {
        String className = identifier; // Since we save as ClassName.java
        Path codeFilePath = FileUtil.writeCodeToFile(code, "java", className);
        Path inputFilePath = FileUtil.writeInputToFile(input, identifier);

        // Compile
        Process compileProcess = new ProcessBuilder("javac", codeFilePath.toString()).start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() != 0) {
            return "Compilation failed for Java.";
        }

        // Run
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", "codes", className);
        pb.redirectInput(inputFilePath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");

        process.waitFor();
        return output.toString().trim();
    }

}
