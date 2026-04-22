package com.karthikd.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final String CODE_DIR = "codes";
    private static final String INPUT_DIR = "inputs";

    static {
        new File(CODE_DIR).mkdirs();
        new File(INPUT_DIR).mkdirs();
    }

    public static Path writeCodeToFile(String code, String extension, String identifier) throws IOException {

        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");

        String dirPath = CODE_DIR + "/" + safeIdentifier;
        new File(dirPath).mkdirs();

        String fileName;
        if ("java".equals(extension)) {
            fileName = "Main.java"; // Java needs Main.java
        } else {
            fileName = safeIdentifier + "." + extension;
        }

        String filePath = dirPath + "/" + fileName;

        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(code);
        }

        return Paths.get(filePath);
    }

    public static Path writeInputToFile(String input, String identifier) throws IOException {

        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");

        Path inputPath = Paths.get(INPUT_DIR, "input_" + safeIdentifier + ".txt");

        Files.writeString(inputPath, input);

        return inputPath;
    }
}