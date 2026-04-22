package com.karthikd.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CppExecutor {

    public static File compileCpp(String code, String identifier) throws IOException, InterruptedException {

        Path codePath = FileUtil.writeCodeToFile(code, "cpp", identifier);

        String codeFileName = codePath.getFileName().toString();
        String exeFileName = codeFileName.replace(".cpp", "");

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String exeFileNameWithExt = isWindows ? exeFileName + ".exe" : exeFileName;

        File exeFile = new File(codePath.getParent().toFile(), exeFileNameWithExt);
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

        int result = compileProcess.waitFor();

        if (result != 0 || !exeFile.exists()) {
            throw new RuntimeException("Compilation Error:\n" + compileErrors);
        }

        return exeFile;
    }

    public static String runCpp(File exeFile, String input) throws Exception {

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long startTime = System.nanoTime();
        long beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;

        // OS check
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        ProcessBuilder pb = isWindows
                ? new ProcessBuilder("cmd.exe", "/c", exeFile.getAbsolutePath())
                : new ProcessBuilder(exeFile.getAbsolutePath());

        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Send input
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