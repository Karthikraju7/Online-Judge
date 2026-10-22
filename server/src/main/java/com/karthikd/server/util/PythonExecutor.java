package com.karthikd.server.util;

import java.nio.file.Path;
import java.util.List;

public class PythonExecutor {

    public static String runPython(String code, String input, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py", identifier);
        String pyFileName = codeFilePath.getFileName().toString();

        return WarmContainerManager.runPython(safeIdentifier, pyFileName, input);
    }

    public static List<String> runAllTestCases(String code, List<String> inputs, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        Path codeFilePath = FileUtil.writeCodeToFile(code, "py", identifier);
        String pyFileName = codeFilePath.getFileName().toString();

        return WarmContainerManager.runPythonBatch(safeIdentifier, pyFileName, inputs);
    }
}