package com.karthikd.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;

@Slf4j
public class CppExecutor {

    public static String runCpp(Path codePath, Path inputPath) throws IOException, InterruptedException {
        String codeFileName = codePath.getFileName().toString();
        String exeFileName = codeFileName.replace(".cpp", "");

        // Detect OS
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String exeFileNameWithExt = isWindows ? exeFileName + ".exe" : exeFileName;
        File exeFile = new File("codes", exeFileNameWithExt);
        String exeAbsolutePath = exeFile.getAbsolutePath();

        log.info("🔧 Compiling C++ file: {}", codeFileName);

        // Compile the C++ file
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

        if (compileResult != 0) {
            log.error("❌ Compilation failed for {}:\n{}", codeFileName, compileErrors.toString());
            return "Compilation failed:\n" + compileErrors.toString();
        }

        if (!exeFile.exists()) {
            log.error("❌ Compiled executable not found: {}", exeAbsolutePath);
            return "Compilation failed. Executable missing.";
        }

        log.info("🚀 Running executable at: {}", exeAbsolutePath);

        // Run the compiled executable using absolute path
        ProcessBuilder pb = new ProcessBuilder(exeAbsolutePath);
        pb.redirectInput(inputPath.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        log.info("✅ Execution finished with exit code: {}", exitCode);
        log.debug("📤 Program output:\n{}", output.toString().trim());

        return output.toString().trim();
    }
}
