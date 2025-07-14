package com.karthikd.server.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.UUID;

public class CppExecutor {

    public static String runCpp(Path codePath, Path inputPath) throws Exception {
        System.out.println("🧠 CppExecutor Started");
        System.out.println("📄 Code Path: " + codePath);
        System.out.println("📥 Input Path: " + inputPath);

        String jobId = UUID.randomUUID().toString();
        Path outputPath = Path.of("outputs", jobId + ".out");
        System.out.println("📤 Output Path: " + outputPath);

        // Compile command
        Process compileProcess = new ProcessBuilder("g++", codePath.toString(), "-o", outputPath.toString())
                .redirectErrorStream(true)
                .start();

        int compileExit = compileProcess.waitFor();
        if (compileExit != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
            System.out.println("❌ Compilation Failed:\n" + error);
            throw new RuntimeException("Compilation Error:\n" + error);
        }

        System.out.println("✅ Compilation Successful");

        // Run command
        Process runProcess = new ProcessBuilder(outputPath.toString())
                .redirectInput(inputPath.toFile())
                .start();

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        System.out.println("🟢 Execution Output:\n" + output);
        return output.toString().trim();
    }
}
