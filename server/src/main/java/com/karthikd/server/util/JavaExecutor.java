    package com.karthikd.server.util;

    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Path;

    public class JavaExecutor {

        public static String runJava(String code, String input) throws IOException, InterruptedException {
            // 1. Write code to file
            String className = "Main";
            Path javaFilePath = FileUtil.writeCodeToFile(code, "java", className);
            Path inputFilePath = FileUtil.writeInputToFile(input);

            // 2. Compile the Java code
            Process compileProcess = new ProcessBuilder(
                    "javac",
                    "-cp", "libs/*",                      // ✅ includes jackson-core, jackson-databind, etc.
                    javaFilePath.toString()
            ).start();

            int compileExitCode = compileProcess.waitFor();

            if (compileExitCode != 0) {
                String compileError = new String(compileProcess.getErrorStream().readAllBytes());
                return "❌ Compilation Error:\n" + compileError;
            }

            // 3. Run the compiled class
            ProcessBuilder runBuilder = new ProcessBuilder(
                    "java",
                    "-cp", "libs/*;codes",               // ✅ add codes for compiled .class + libs/*
                    className
            );
            runBuilder.redirectInput(inputFilePath.toFile());
            runBuilder.redirectErrorStream(true);


            Process runProcess = runBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int runExitCode = runProcess.waitFor();

            if (runExitCode != 0) {
                return "❌ Runtime Error during Java execution.";
            }

            return output.toString();
        }
    }
