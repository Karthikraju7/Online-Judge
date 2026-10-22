package com.karthikd.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class CppExecutor {

    public static File compileCpp(String code, String identifier) throws IOException, InterruptedException {
        Path codePath = FileUtil.writeCodeToFile(code, "cpp", identifier);

        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        String codeFileName = codePath.getFileName().toString();
        String exeFileName = codeFileName.replace(".cpp", "");

        WarmContainerManager.compileCpp(safeIdentifier, codeFileName, exeFileName);

        String hostDir = new File("codes/" + safeIdentifier).getAbsolutePath();
        return new File(hostDir, exeFileName);
    }

    public static String runCpp(File exeFile, String input, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        String exeFileName = exeFile.getName();
        return WarmContainerManager.runCpp(safeIdentifier, exeFileName, input);
    }

    public static List<String> runAllTestCases(File exeFile, List<String> inputs, String identifier) throws Exception {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        String exeFileName = exeFile.getName();
        return WarmContainerManager.runCppBatch(safeIdentifier, exeFileName, inputs);
    }
}