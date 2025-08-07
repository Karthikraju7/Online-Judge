package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class JavaExecutor {

    public static String runJava(String code, String input, String identifier) throws IOException, InterruptedException {
        String className = "Main"; // ✅ Always Main
        Path codeFilePath = FileUtil.writeCodeToFile(code, "java", identifier);
        Path inputFilePath = FileUtil.writeInputToFile(input, identifier);

        File codeDir = codeFilePath.getParent().toFile(); // ✅ This is codes/identifier
        System.out.println("🛠️ [JavaExecutor] Compiling Java file: " + codeFilePath);

        // ✅ Compile inside the subdirectory
        Process compileProcess = new ProcessBuilder("javac", "Main.java")
                .directory(codeDir) // 🔑 Compile inside codes/identifier
                .start();
        compileProcess.waitFor();

        if (compileProcess.exitValue() != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
            StringBuilder compileErrors = new StringBuilder();
            String errLine;
            while ((errLine = errorReader.readLine()) != null) {
                compileErrors.append(errLine).append("\n");
            }

            System.out.println("❌ [JavaExecutor] Compilation failed:\n" + compileErrors.toString().trim());
            return "OUTPUT_START\n❌ Compilation Error\n" + compileErrors.toString().trim() +
                    "\nOUTPUT_END\nVERDICT:❌ Compilation Error";
        }

        System.out.println("✅ [JavaExecutor] Compilation successful. Starting execution...");

        // ⏱️ Measure time/memory
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        // ✅ Run with correct classpath and class name
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", codeDir.getAbsolutePath(), className);
        pb.redirectInput(inputFilePath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        boolean finished = process.waitFor(2, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            System.out.println("⏱️ [JavaExecutor] TLE: Execution did not finish in 2 seconds.");
            return "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:-1\nMEMORY:-1\nVERDICT:❌ TLE";
        }

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

        System.out.println("✅ [JavaExecutor] Execution finished.");
        System.out.println("🖨️ [JavaExecutor] Output:\n" + output.toString().trim());
        System.out.println("⏱️ [JavaExecutor] Time used: " + timeUsedMs + "ms");
        System.out.println("💾 [JavaExecutor] Memory used: " + memoryUsedKB + "KB");

        return String.format(
                "OUTPUT_START\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                output.toString().trim(), timeUsedMs, memoryUsedKB
        );
    }

}
