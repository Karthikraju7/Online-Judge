package com.karthikd.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CppExecutor {

    public static String runCpp(Path codePath, Path inputPath) throws IOException, InterruptedException {
        String codeFileName = codePath.getFileName().toString();
        String exeFileName = codeFileName.replace(".cpp", "");

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String exeFileNameWithExt = isWindows ? exeFileName + ".exe" : exeFileName;
        File exeFile = new File("codes", exeFileNameWithExt);
        String exeAbsolutePath = exeFile.getAbsolutePath();

        Process compileProcess = new ProcessBuilder(
                "g++", "-std=c++17", codePath.toString(), "-o", exeAbsolutePath
        ).start();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
        StringBuilder compileErrors = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            compileErrors.append(line).append("\n");
        }

        int compileResult = compileProcess.waitFor();

        if (compileResult != 0 || !exeFile.exists()) {
            return "Compilation failed:\n" + compileErrors;
        }

        // Time and Memory tracking
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        ProcessBuilder pb = new ProcessBuilder(exeAbsolutePath);
        pb.redirectInput(inputPath.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        boolean finished = process.waitFor(2, TimeUnit.SECONDS); // ⏱️ timeout check

        if (!finished) {
            process.destroyForcibly();
            return "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:-1\nMEMORY:-1\nVERDICT:❌ TLE";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.exitValue();

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
