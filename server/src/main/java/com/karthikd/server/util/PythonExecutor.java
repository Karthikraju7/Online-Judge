package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PythonExecutor {

    public static String runPython(String code, String input, String identifier) throws IOException, InterruptedException {
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py", identifier);
        Path inputFilePath = FileUtil.writeInputToFile(input, identifier);

        String[] command = {"python", codeFilePath.toString()};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectInput(inputFilePath.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return "Error during Python execution.\n" + output.toString();
        }

        return output.toString().trim();
    }

}
