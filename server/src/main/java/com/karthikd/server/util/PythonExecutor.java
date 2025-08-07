package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PythonExecutor {

    public static String runPython(String code, String input, String identifier) throws IOException, InterruptedException {
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py", identifier);
        Path inputFilePath = FileUtil.writeInputToFile(input, identifier);

        String[] command = {"python", codeFilePath.toString()};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectInput(inputFilePath.toFile());
        processBuilder.redirectErrorStream(true);

        // Time and Memory tracking
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        Process process = processBuilder.start();

        boolean finished = process.waitFor(2, TimeUnit.SECONDS); // ⏱️ timeout check

        if (!finished) {
            process.destroyForcibly();
            return "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:-1\nMEMORY:-1\nVERDICT:❌ TLE";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.exitValue();

        long endTime = System.nanoTime();
        long afterMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        long timeUsedMs = (endTime - startTime) / 1_000_000;
        long memoryUsedKB = afterMemory - beforeMemory;

        if (exitCode != 0) {
            return String.format(
                    "OUTPUT_START\n❌ Error during Python execution\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                    timeUsedMs, memoryUsedKB
            );
        }

        return String.format(
                "OUTPUT_START\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                output.toString().trim(), timeUsedMs, memoryUsedKB
        );
    }
}
