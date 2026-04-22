package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PythonExecutor {

    public static String runPython(String code, String input) throws IOException, InterruptedException {

        // Write code
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py", "temp_py");

        ProcessBuilder processBuilder = new ProcessBuilder("python", codeFilePath.toString());
        processBuilder.redirectErrorStream(true);

        // Time & memory tracking
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        Process process = processBuilder.start();

        // Send input directly
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(input);
        writer.flush();
        writer.close();

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            return "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:-1\nMEMORY:-1";
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
                    "OUTPUT_START\n❌ Runtime Error\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                    timeUsedMs, memoryUsedKB
            );
        }

        return String.format(
                "OUTPUT_START\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                output.toString().trim(), timeUsedMs, memoryUsedKB
        );
    }
}