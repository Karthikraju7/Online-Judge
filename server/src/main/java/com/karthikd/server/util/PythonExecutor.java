package com.karthikd.server.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PythonExecutor {

    public static String runPython(String code, String input) throws IOException, InterruptedException {
        // 1. Save code to file
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py");

        // 2. Save input to file
        Path inputFilePath = FileUtil.writeInputToFile(input);

        // 3. Prepare command: python <filename>.py < input.txt
        String[] command = {
                "python", codeFilePath.toString()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectInput(inputFilePath.toFile());
        processBuilder.redirectErrorStream(true); // merge stderr into stdout

        Files.writeString(inputFilePath, input);
        Process process = processBuilder.start();

        // 4. Capture output
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

        return output.toString();
    }
}
