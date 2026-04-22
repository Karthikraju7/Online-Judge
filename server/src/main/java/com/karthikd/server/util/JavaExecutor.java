package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class JavaExecutor {

    // COMPILE
    public static File compileJava(String code, String identifier) throws IOException, InterruptedException {

        String className = "Main";

        // Write code
        Path codeFilePath = FileUtil.writeCodeToFile(code, "java", identifier);
        File codeDir = codeFilePath.getParent().toFile();

        // Compile
        Process compileProcess = new ProcessBuilder("javac", className + ".java")
                .directory(codeDir)
                .start();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
        StringBuilder compileErrors = new StringBuilder();
        String line;

        while ((line = errorReader.readLine()) != null) {
            compileErrors.append(line).append("\n");
        }

        int result = compileProcess.waitFor();

        if (result != 0) {
            throw new RuntimeException("Compilation Error:\n" + compileErrors);
        }

        return codeDir; // return folder containing .class
    }

    //RUN
    public static String runJava(File codeDir, String input) throws Exception {

        String className = "Main";

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        // Run program
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", codeDir.getAbsolutePath(), className);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Sending input directly
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(input);
        writer.flush();
        writer.close();

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            return "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:-1\nMEMORY:-1";
        }

        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        long endTime = System.nanoTime();
        long afterMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        long timeUsedMs = (endTime - startTime) / 1_000_000;
        long memoryUsedKB = afterMemory - beforeMemory;

        return String.format(
                "OUTPUT_START\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                output.toString().trim(), timeUsedMs, memoryUsedKB
        );
    }
}