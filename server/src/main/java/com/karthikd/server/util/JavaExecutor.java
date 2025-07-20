package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaExecutor {

    public static String runJava(String code, String input) throws IOException, InterruptedException {
        String className = "Main";
        Path javaFilePath = FileUtil.writeCodeToFile(code, "java", className);
        Path inputFilePath = FileUtil.writeInputToFile(input);

        // Compile
        Process compileProcess = new ProcessBuilder(
                "javac",
                javaFilePath.toString()
        ).start();

        int compileExit = compileProcess.waitFor();
        if (compileExit != 0) {
            String error = new String(compileProcess.getErrorStream().readAllBytes());
            return "❌ Compilation Error:\n" + error;
        }

        // Run
        Process runProcess = new ProcessBuilder(
                "java",
                "-cp", "codes",  // compiled .class is in codes folder
                className
        ).redirectInput(inputFilePath.toFile())
                .redirectErrorStream(true)
                .start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int runExit = runProcess.waitFor();
        if (runExit != 0) {
            return "❌ Runtime Error during Java execution.";
        }

        return output.toString().trim();
    }
}
